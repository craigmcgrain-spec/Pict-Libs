package com.example.pict_libs.speech

enum class SpeechStatus { LOADING, READY, SPEAKING, UNAVAILABLE, ERROR }

data class SpeechState(
    val status: SpeechStatus = SpeechStatus.LOADING,
    val voices: List<SpeechVoice> = emptyList(),
    val selectedVoiceId: String? = null
)

/** Owns playback state independently of Android so stop/retry races can be tested. */
class SpeechController(
    private val engine: SpeechEngine,
    private val preferredVoiceId: String?,
    private val onStateChanged: (SpeechState) -> Unit
) : SpeechEngine.Listener {
    var state = SpeechState()
        private set
    private var closed = false
    private var sequence = 0
    private var activeRequest: String? = null

    fun start() {
        check(!closed)
        onStateChanged(state)
        engine.initialize(this)
    }

    override fun onReady(voices: List<SpeechVoice>) {
        if (closed) return
        if (voices.isEmpty()) {
            onUnavailable()
            return
        }
        val selected = voices.firstOrNull { it.id == preferredVoiceId } ?: voices.first()
        update(SpeechState(SpeechStatus.READY, voices, selected.id))
    }

    override fun onUnavailable() {
        if (!closed) update(SpeechState(SpeechStatus.UNAVAILABLE))
    }

    fun selectVoice(id: String) {
        if (closed || state.voices.none { it.id == id }) return
        stop()
        update(state.copy(selectedVoiceId = id, status = SpeechStatus.READY))
    }

    fun speak(text: String) {
        if (closed || text.isBlank() || state.status !in listOf(SpeechStatus.READY, SpeechStatus.ERROR)) return
        val voiceId = state.selectedVoiceId ?: return
        val requestId = "sentence-${++sequence}"
        activeRequest = requestId
        update(state.copy(status = SpeechStatus.SPEAKING))
        if (!engine.speak(text, voiceId, requestId)) onError(requestId)
    }

    fun stop() {
        if (closed) return
        activeRequest = null
        engine.stop()
        if (state.voices.isNotEmpty()) update(state.copy(status = SpeechStatus.READY))
    }

    override fun onFinished(requestId: String) {
        if (closed || activeRequest != requestId) return
        activeRequest = null
        update(state.copy(status = SpeechStatus.READY))
    }

    override fun onError(requestId: String) {
        if (closed || activeRequest != requestId) return
        activeRequest = null
        engine.stop()
        update(state.copy(status = SpeechStatus.ERROR))
    }

    fun close() {
        if (closed) return
        closed = true
        activeRequest = null
        engine.close()
    }

    private fun update(newState: SpeechState) {
        state = newState
        onStateChanged(state)
    }
}
