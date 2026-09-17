package com.example.pict_libs.speech

/** A voice ID belongs to its engine; it is not a recording or a voice sample. */
data class SpeechVoice(val id: String, val label: String)

/** Playback boundary shared by device TTS and a future sample-based voice provider. */
interface SpeechEngine {
    interface Listener {
        fun onReady(voices: List<SpeechVoice>)
        fun onUnavailable()
        fun onFinished(requestId: String)
        fun onError(requestId: String)
    }

    /** All listener callbacks must arrive on the UI thread. */
    fun initialize(listener: Listener)
    fun speak(text: String, voiceId: String, requestId: String): Boolean
    fun stop()
    fun close()
}
