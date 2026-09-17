package com.example.pict_libs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pict_libs.databinding.ActivityStoryBinding
import com.example.pict_libs.speech.PiperSpeechEngine
import com.example.pict_libs.speech.SpeechController
import com.example.pict_libs.speech.SpeechState
import com.example.pict_libs.speech.SpeechStatus
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class StoryActivity : AppCompatActivity() {

    companion object {
        const val STORY_TEXT = "STORY_TEXT"
    }

    private lateinit var binding: ActivityStoryBinding
    private var speech: SpeechController? = null
    private val voicePreferences by lazy { getSharedPreferences("speech", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storyText = intent.getStringExtra(STORY_TEXT) ?: ""
        binding.storyText.text = storyText

        binding.readAloudButton.setOnClickListener {
            val controller = speech ?: return@setOnClickListener
            if (controller.state.status == SpeechStatus.SPEAKING) controller.stop()
            else controller.speak(storyText)
        }
        binding.voiceButton.setOnClickListener {
            val controller = speech ?: return@setOnClickListener
            val state = controller.state
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.choose_voice)
                .setSingleChoiceItems(state.voices.map { it.label }.toTypedArray(),
                    state.voices.indexOfFirst { it.id == state.selectedVoiceId }) { dialog, index ->
                    val voice = state.voices[index]
                    controller.selectVoice(voice.id)
                    voicePreferences.edit().putString("voice_id", voice.id).apply()
                    dialog.dismiss()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
        binding.speechSettingsButton.setOnClickListener {
            startSpeech()
        }

        binding.playAgainButton.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }

        binding.tryAgainButton.setOnClickListener {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        startSpeech()
    }

    private fun startSpeech() {
        speech?.close()
        speech = SpeechController(PiperSpeechEngine(this),
            voicePreferences.getString("voice_id", null), ::renderSpeech)
        speech?.start()
    }

    override fun onPause() {
        speech?.stop()
        super.onPause()
    }

    override fun onStop() {
        speech?.close()
        speech = null
        super.onStop()
    }

    private fun renderSpeech(state: SpeechState) {
        binding.speechStatus.setText(when (state.status) {
            SpeechStatus.LOADING -> R.string.speech_loading
            SpeechStatus.READY -> R.string.speech_ready
            SpeechStatus.SPEAKING -> R.string.speech_reading
            SpeechStatus.UNAVAILABLE -> R.string.speech_unavailable
            SpeechStatus.ERROR -> R.string.speech_error
        })
        binding.readAloudButton.isEnabled = state.voices.isNotEmpty() && binding.storyText.text.isNotBlank()
        binding.readAloudButton.setText(if (state.status == SpeechStatus.SPEAKING)
            R.string.stop_reading else R.string.read_aloud)
        binding.voiceButton.isEnabled = state.voices.isNotEmpty() && state.status != SpeechStatus.SPEAKING
        val voice = state.voices.firstOrNull { it.id == state.selectedVoiceId }
        binding.voiceButton.text = voice?.let { getString(R.string.voice_label, it.label) }
            ?: getString(R.string.choose_voice)
    }
}
