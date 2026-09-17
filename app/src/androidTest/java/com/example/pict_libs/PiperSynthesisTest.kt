package com.example.pict_libs

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.pict_libs.speech.PiperModels
import com.example.pict_libs.speech.PiperSpeechEngine
import com.example.pict_libs.speech.SpeechEngine
import com.example.pict_libs.speech.SpeechVoice
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class PiperSynthesisTest {
    @Test
    fun bothFullQualityModelsGenerateNonSilentAudioAt22050Hz() {
        val root = PiperModels.prepare(ApplicationProvider.getApplicationContext())
        PiperModels.voices.forEach { voice ->
            val model = PiperModels.create(root, voice.id)
            try {
                val audio = model.generate("The sparkly penguin can dance!")
                assertEquals(22050, audio.sampleRate)
                assertTrue("${voice.id} should generate at least half a second", audio.samples.size > 11025)
                assertTrue(audio.samples.all { it.isFinite() })
                assertTrue("${voice.id} must not be silent", audio.samples.any { abs(it) > 0.01f })
            } finally {
                model.release()
            }
        }
    }

    @Test
    fun piperEngineSynthesizesAndPlaysBothVoicesToCompletion() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = PiperSpeechEngine(context)
        val ready = CountDownLatch(1)
        var finished = CountDownLatch(1)
        var failure: String? = null
        instrumentation.runOnMainSync {
            engine.initialize(object : SpeechEngine.Listener {
                override fun onReady(voices: List<SpeechVoice>) { ready.countDown() }
                override fun onUnavailable() { failure = "Models unavailable"; ready.countDown() }
                override fun onFinished(requestId: String) { finished.countDown() }
                override fun onError(requestId: String) { failure = requestId; finished.countDown() }
            })
        }
        try {
            assertTrue(ready.await(120, TimeUnit.SECONDS))
            assertNull(failure)
            PiperModels.voices.forEach { voice ->
                finished = CountDownLatch(1)
                instrumentation.runOnMainSync {
                    assertTrue(engine.speak("Hello, silly penguin!", voice.id, voice.id))
                }
                assertTrue("${voice.id}: synthesis/playback timed out", finished.await(120, TimeUnit.SECONDS))
                assertNull("${voice.id}: synthesis/playback failed", failure)
            }
        } finally {
            instrumentation.runOnMainSync { engine.close() }
        }
    }
}
