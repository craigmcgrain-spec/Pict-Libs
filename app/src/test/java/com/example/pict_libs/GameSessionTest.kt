package com.example.pict_libs

import com.example.pict_libs.data.GameSession
import com.example.pict_libs.data.StoryTemplates
import com.example.pict_libs.data.WordRepository
import com.example.pict_libs.data.WordSubcategory
import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

class GameSessionTest {
    @Test
    fun everyTemplateOffersTwelveMatchingWordsForEachOfSixBlanks() {
        StoryTemplates.all.forEach { template ->
            repeat(30) { seed ->
                val game = GameSession(template, Random(seed))
                repeat(6) { round ->
                    assertFalse(game.isComplete)
                    assertEquals(round, game.picks.size)
                    assertEquals(template.allSlots[round], game.currentSlot)
                    assertEquals(12, game.pool.size)
                    assertEquals(12, game.pool.map { it.text }.distinct().size)
                    assertTrue(game.pool.all { it.subcategory == template.allSlots[round] })
                    assertTrue(game.pool.all { it.category == template.allSlots[round].category })
                    assertTrue(game.pool.all { it.emoji.isNotBlank() })
                    game.choose(game.pool[seed % 12])
                }
                assertTrue(game.isComplete)
                assertTrue(game.pool.isEmpty())
                assertNull(game.currentSlot)
                val sentence = game.story()
                var previousPosition = -1
                game.picks.forEach { word ->
                    val position = sentence.indexOf(word.text, previousPosition + 1)
                    assertTrue("${word.text} must appear in slot order", position > previousPosition)
                    previousPosition = position
                }
            }
        }
    }

    @Test
    fun poolsHaveEnoughVarietyToRandomizeAndNoDuplicateWords() {
        assertEquals(WordRepository.allWords.size, WordRepository.allWords.map { it.text }.distinct().size)
        WordSubcategory.entries.forEach { theme ->
            assertTrue(WordRepository.allWords.count { it.subcategory == theme } > 12)
            assertNotEquals(WordRepository.pool(theme, Random(1)), WordRepository.pool(theme, Random(2)))
        }
    }

    @Test
    fun invalidPicksAndPrematureStoriesAreRejected() {
        val game = GameSession(StoryTemplates.all.first(), Random(0))
        assertThrows(IllegalArgumentException::class.java) { game.story() }
        val wrongWord = WordRepository.allWords.first { it.subcategory != game.currentSlot }
        assertThrows(IllegalArgumentException::class.java) { game.choose(wrongWord) }
        assertTrue(game.picks.isEmpty())
        repeat(6) { game.choose(game.pool.first()) }
        assertThrows(IllegalStateException::class.java) { game.choose(wrongWord) }
    }

    @Test
    fun restoreAndUndoPreserveEarlierPicksAndRequiredCategory() {
        val game = GameSession(StoryTemplates.all.first(), Random(1))
        repeat(3) { game.choose(game.pool.first()) }
        val restored = GameSession(game.template, Random(2), game.picks, game.pool)
        assertEquals(game.picks, restored.picks)
        assertEquals(game.pool, restored.pool)
        restored.undo()
        assertEquals(game.picks.take(2), restored.picks)
        assertEquals(game.template.allSlots[2], restored.currentSlot)
        repeat(4) { restored.choose(restored.pool.first()) }
        val completed = GameSession(restored.template, picks = restored.picks, pool = restored.pool)
        assertEquals(restored.story(), completed.story())
        completed.undo()
        assertEquals(restored.picks.take(5), completed.picks)
        assertEquals(restored.template.allSlots.last(), completed.currentSlot)
        assertEquals(12, completed.pool.size)
    }
}
