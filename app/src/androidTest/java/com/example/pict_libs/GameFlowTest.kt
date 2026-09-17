package com.example.pict_libs

import android.widget.TextView
import com.example.pict_libs.adapters.WordAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameFlowTest {
    @Test
    fun sixSeparatePoolsLeadToSentenceAndFreshReplay() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            val chosen = mutableListOf<String>()
            onView(withId(R.id.undoButton)).check(matches(not(isEnabled())))
            repeat(6) { round ->
                onView(withId(R.id.selectionCount)).check(matches(withText("Pick ${round + 1} of 6")))
                onView(withId(R.id.generateButton)).check(matches(not(isDisplayed())))
                var pool = emptyList<String>()
                scenario.onActivity { activity -> pool = readPool(activity) }
                if (round == 2) {
                    scenario.recreate()
                    onView(withId(R.id.selectionCount)).check(matches(withText("Pick 3 of 6")))
                    scenario.onActivity { activity -> assertEquals(pool, readPool(activity)) }
                    chosen.forEach { word ->
                        onView(withId(R.id.chosenWords)).check(matches(withText(containsString(word))))
                    }
                }
                chosen.add(pool.first())
                selectFirstWord()
            }
            // The sixth tap opens the result automatically, with every chosen word inserted.
            onView(withId(R.id.storyText)).check(matches(isDisplayed()))
            chosen.forEach { word ->
                onView(withId(R.id.storyText)).check(matches(withText(containsString(word))))
            }
            onView(withId(R.id.tryAgainButton)).perform(scrollTo(), click())
            onView(withId(R.id.selectionCount)).check(matches(withText("Pick 6 of 6")))
            chosen.take(5).forEach { word ->
                onView(withId(R.id.chosenWords)).check(matches(withText(containsString(word))))
            }
            selectFirstWord()
            onView(withId(R.id.playAgainButton)).perform(scrollTo(), click())
            onView(withId(R.id.selectionCount)).check(matches(withText("Pick 1 of 6")))
            onView(withId(R.id.undoButton)).check(matches(not(isEnabled())))
        }
    }

    @Test
    fun undoReturnsToPreviousRequiredCategory() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            var prompt = ""
            scenario.onActivity { prompt = it.findViewById<TextView>(R.id.roundPrompt).text.toString() }
            selectFirstWord()
            onView(withId(R.id.selectionCount)).check(matches(withText("Pick 2 of 6")))
            onView(withId(R.id.undoButton)).perform(click())
            onView(withId(R.id.selectionCount)).check(matches(withText("Pick 1 of 6")))
            onView(withId(R.id.roundPrompt)).check(matches(withText(prompt)))
            scenario.onActivity { readPool(it) }
        }
    }

    private fun readPool(activity: MainActivity): List<String> {
        val grid = activity.findViewById<RecyclerView>(R.id.wordGrid)
        val adapter = grid.adapter as WordAdapter
        assertEquals(12, adapter.itemCount)
        val prompt = activity.findViewById<TextView>(R.id.roundPrompt).text.toString()
        return (0 until adapter.itemCount).map { position ->
            val holder = adapter.createViewHolder(grid, adapter.getItemViewType(position))
            adapter.bindViewHolder(holder, position)
            val theme = holder.itemView.findViewById<TextView>(R.id.wordCategory).text.toString()
            assertTrue(prompt.contains(theme))
            holder.itemView.findViewById<TextView>(R.id.wordText).text.toString()
        }.also { assertEquals(12, it.distinct().size) }
    }

    private fun selectFirstWord() {
        onView(withId(R.id.wordGrid))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
    }
}
