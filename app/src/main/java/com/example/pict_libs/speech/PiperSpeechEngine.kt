package com.example.pict_libs.speech

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.k2fsa.sherpa.onnx.OfflineTts
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

/** Local Piper model inference through sherpa-onnx, followed by Android PCM/WAV playback. */
@Suppress("DEPRECATION") // Audio focus supports API 24 devices as well.
class PiperSpeechEngine(context: Context) : SpeechEngine {
    companion object {
        // eSpeak's process-wide state and native model lifetimes must not overlap across activities.
        private val worker = Executors.newSingleThreadExecutor()
    }

    private val context = context.applicationContext
    private val main = Handler(Looper.getMainLooper())
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val generation = AtomicLong()
    @Volatile private var closed = false
    private var listener: SpeechEngine.Listener? = null
    private var player: MediaPlayer? = null
    private var playingFile: File? = null
    private var requestId: String? = null
    // Only accessed on worker:
    private var model: OfflineTts? = null
    private var modelVoice: String? = null
    private var root: File? = null

    private val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
        if (change < 0) main.post {
            val interrupted = requestId
            stop()
            if (!closed && interrupted != null) listener?.onFinished(interrupted)
        }
    }

    override fun initialize(listener: SpeechEngine.Listener) {
        this.listener = listener
        worker.execute {
            try {
                if (closed) return@execute
                root = PiperModels.prepare(context)
                main.post { if (!closed) listener.onReady(PiperModels.voices) }
            } catch (error: Exception) {
                Log.e("PiperSpeech", "Unable to prepare bundled Piper voices", error)
                main.post { if (!closed) listener.onUnavailable() }
            }
        }
    }

    override fun speak(text: String, voiceId: String, requestId: String): Boolean {
        if (closed || text.isBlank() || PiperModels.voices.none { it.id == voiceId }) return false
        stop()
        this.requestId = requestId
        val ticket = generation.get()
        worker.execute {
            var output: File? = null
            try {
                if (cancelled(ticket)) return@execute
                if (modelVoice != voiceId) {
                    model?.release()
                    model = null
                    modelVoice = null
                    model = PiperModels.create(checkNotNull(root), voiceId)
                    modelVoice = voiceId
                }
                if (cancelled(ticket)) return@execute
                // JNI looks up invoke(float[]): Integer; an invokedynamic lambda only
                // exposes invoke(Object). An explicit function object retains that method.
                val audio = checkNotNull(model).generateWithCallback(text, sid = 0, speed = 1.0f,
                    callback = object : (FloatArray) -> Int {
                        override fun invoke(samples: FloatArray): Int = if (cancelled(ticket)) 0 else 1
                    })
                if (cancelled(ticket)) return@execute
                check(audio.samples.isNotEmpty()) { "Piper returned no audio" }
                val file = File.createTempFile("piper-", ".wav", context.cacheDir)
                output = file
                check(audio.save(file.absolutePath)) { "Unable to write Piper audio" }
                main.post {
                    if (cancelled(ticket)) file.delete() else play(file, requestId, ticket)
                }
                output = null // Ownership passed to main thread.
            } catch (error: Exception) {
                Log.e("PiperSpeech", "Piper synthesis failed", error)
                main.post { if (!cancelled(ticket)) fail(requestId) }
            } catch (error: LinkageError) {
                Log.e("PiperSpeech", "Piper native runtime could not load", error)
                main.post { if (!cancelled(ticket)) fail(requestId) }
            } finally {
                output?.delete()
            }
        }
        return true
    }

    private fun play(file: File, id: String, ticket: Long) {
        playingFile = file
        try {
            check(audioManager.requestAudioFocus(focusListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            player = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
                setDataSource(file.absolutePath)
                setOnPreparedListener { if (!cancelled(ticket)) it.start() }
                setOnCompletionListener {
                    if (!cancelled(ticket)) {
                        stop()
                        listener?.onFinished(id)
                    }
                }
                setOnErrorListener { _, _, _ ->
                    if (!cancelled(ticket)) fail(id)
                    true
                }
                prepareAsync()
            }
        } catch (error: Exception) {
            Log.e("PiperSpeech", "Piper playback failed", error)
            fail(id)
        }
    }

    private fun cancelled(ticket: Long) = closed || generation.get() != ticket

    private fun fail(id: String) {
        stop()
        if (!closed) listener?.onError(id)
    }

    override fun stop() {
        generation.incrementAndGet()
        requestId = null
        player?.release()
        player = null
        playingFile?.delete()
        playingFile = null
        audioManager.abandonAudioFocus(focusListener)
    }

    override fun close() {
        if (closed) return
        closed = true
        stop()
        listener = null
        worker.execute {
            model?.release()
            model = null
            modelVoice = null
        }
    }
}
