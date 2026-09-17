package com.example.pict_libs

import android.content.Intent
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.not
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StorySpeechTest {
    @Test
    fun piperVoicesInitializeAndSupportPlaybackAndRotation() {
        // Long enough to exercise stop while the device engine is speaking.
        val sentence = "On the moon, the sparkly penguin decided to dance with the grumpy banana, which suddenly began to whistle!"
        val intent = Intent(ApplicationProvider.getApplicationContext(), StoryActivity::class.java)
            .putExtra(StoryActivity.STORY_TEXT, sentence)
        ActivityScenario.launch<StoryActivity>(intent).use { scenario ->
            awaitInitialization(scenario)
            onView(withId(R.id.speechStatus)).check(matches(withText(R.string.speech_ready)))
                onView(withId(R.id.readAloudButton)).perform(scrollTo(), click())
                    .check(matches(withText(R.string.stop_reading)))
                onView(withId(R.id.voiceButton)).check(matches(not(isEnabled())))
                onView(withId(R.id.readAloudButton)).perform(click())
                    .check(matches(withText(R.string.read_aloud)))
                onView(withId(R.id.voiceButton)).perform(scrollTo(), click())
                onView(withText(R.string.choose_voice)).check(matches(isDisplayed()))
                onView(withText("Lessac High · English (US)")).check(matches(isDisplayed()))
                onView(withText("Cori High · English (UK)")).perform(click())
                onView(withId(R.id.readAloudButton)).perform(scrollTo(), click())
                scenario.recreate()
                awaitInitialization(scenario)
                onView(withId(R.id.readAloudButton)).check(matches(withText(R.string.read_aloud)))
                onView(withId(R.id.speechStatus)).check(matches(withText(R.string.speech_ready)))
        }
    }

    private fun awaitInitialization(scenario: ActivityScenario<StoryActivity>) {
        val deadline = System.nanoTime() + 120_000_000_000L
        var initialized = false
        while (!initialized && System.nanoTime() < deadline) {
            scenario.onActivity {
                initialized = it.findViewById<TextView>(R.id.speechStatus).text != it.getString(R.string.speech_loading)
            }
            if (!initialized) Thread.sleep(100)
        }
        assertTrue("Bundled Piper models should finish preparing", initialized)
    }
}
