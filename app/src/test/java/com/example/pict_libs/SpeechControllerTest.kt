package com.example.pict_libs

import com.example.pict_libs.speech.SpeechController
import com.example.pict_libs.speech.SpeechEngine
import com.example.pict_libs.speech.SpeechStatus
import com.example.pict_libs.speech.SpeechVoice
import org.junit.Assert.*
import org.junit.Test

class SpeechControllerTest {
    private val voices = listOf(SpeechVoice("one", "Voice one"), SpeechVoice("two", "Voice two"))

    @Test
    fun waitsForInitializationAndUsesSavedVoiceForExactSentence() {
        val engine = FakeEngine()
        val controller = SpeechController(engine, "two") {}
        controller.start()
        controller.speak("Too early")
        assertNull(engine.lastText)
        engine.listener.onReady(voices)
        controller.speak("The sparkly penguin can dance!")
        assertEquals("The sparkly penguin can dance!", engine.lastText)
        assertEquals("two", engine.lastVoice)
        assertEquals(SpeechStatus.SPEAKING, controller.state.status)
        engine.listener.onFinished(engine.lastRequest!!)
        assertEquals(SpeechStatus.READY, controller.state.status)
    }

    @Test
    fun stoppingAndReplayingIgnoresCallbacksFromPreviousRequest() {
        val engine = FakeEngine()
        val controller = SpeechController(engine, null) {}
        controller.start()
        engine.listener.onReady(voices)
        controller.speak("First")
        val oldRequest = engine.lastRequest!!
        controller.stop()
        assertEquals(SpeechStatus.READY, controller.state.status)
        controller.speak("Second")
        val newRequest = engine.lastRequest!!
        engine.listener.onFinished(oldRequest)
        engine.listener.onError(oldRequest)
        assertEquals(SpeechStatus.SPEAKING, controller.state.status)
        engine.listener.onFinished(newRequest)
        assertEquals(SpeechStatus.READY, controller.state.status)
    }

    @Test
    fun unavailableVoicesDisableSpeechAndMissingPreferenceFallsBack() {
        val engine = FakeEngine()
        val controller = SpeechController(engine, "removed-voice") {}
        controller.start()
        engine.listener.onReady(emptyList())
        assertEquals(SpeechStatus.UNAVAILABLE, controller.state.status)
        controller.speak("Hello")
        assertNull(engine.lastText)
        engine.listener.onReady(voices)
        assertEquals("one", controller.state.selectedVoiceId)
        controller.selectVoice("unknown")
        assertEquals("one", controller.state.selectedVoiceId)
        controller.selectVoice("two")
        assertEquals("two", controller.state.selectedVoiceId)
    }

    @Test
    fun handlesImmediateAndAsyncErrorsAndAllowsRetry() {
        val engine = FakeEngine()
        val controller = SpeechController(engine, null) {}
        controller.start()
        engine.listener.onReady(voices)
        engine.accept = false
        controller.speak("Hello")
        assertEquals(SpeechStatus.ERROR, controller.state.status)
        engine.accept = true
        controller.speak("Retry")
        assertEquals(SpeechStatus.SPEAKING, controller.state.status)
        engine.listener.onError(engine.lastRequest!!)
        assertEquals(SpeechStatus.ERROR, controller.state.status)
        controller.speak("Retry again")
        engine.listener.onFinished(engine.lastRequest!!)
        assertEquals(SpeechStatus.READY, controller.state.status)
    }

    @Test
    fun closingReleasesEngineAndIgnoresLateInitialization() {
        val engine = FakeEngine()
        var notifications = 0
        val controller = SpeechController(engine, null) { notifications++ }
        controller.start()
        controller.close()
        controller.close()
        engine.listener.onReady(voices)
        engine.listener.onUnavailable()
        controller.speak("No speech after close")
        assertEquals(1, engine.closeCount)
        assertEquals(1, notifications)
        assertNull(engine.lastText)
    }

    private class FakeEngine : SpeechEngine {
        lateinit var listener: SpeechEngine.Listener
        var lastText: String? = null
        var lastVoice: String? = null
        var lastRequest: String? = null
        var accept = true
        var closeCount = 0
        override fun initialize(listener: SpeechEngine.Listener) { this.listener = listener }
        override fun speak(text: String, voiceId: String, requestId: String): Boolean {
            lastText = text
            lastVoice = voiceId
            lastRequest = requestId
            return accept
        }
        override fun stop() = Unit
        override fun close() { closeCount++ }
    }
}
