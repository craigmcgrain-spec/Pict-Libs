package com.example.pict_libs.data

import kotlin.random.Random

class GameSession(
    val template: StoryTemplate = StoryTemplates.all.random(),
    private val random: Random = Random.Default,
    picks: List<Word> = emptyList(),
    pool: List<Word>? = null
) {
    private val chosen = picks.toMutableList()
    val picks: List<Word> get() = chosen.toList()
    val isComplete: Boolean get() = chosen.size == template.slots.size
    val currentSlot: WordSubcategory? get() = template.slots.getOrNull(chosen.size)
    var pool: List<Word> = pool ?: newPool()
        private set

    init {
        require(chosen.size <= template.slots.size)
        require(chosen.indices.all { chosen[it].subcategory == template.slots[it] })
        require(if (isComplete) this.pool.isEmpty() else
            this.pool.size == WordRepository.POOL_SIZE &&
                this.pool.distinct().size == WordRepository.POOL_SIZE &&
                this.pool.all { it.subcategory == currentSlot })
    }

    fun choose(word: Word) {
        check(!isComplete) { "All six blanks are already filled" }
        require(word in pool) { "Choose from the current pool" }
        chosen.add(word)
        pool = newPool()
    }

    fun undo() {
        if (chosen.isNotEmpty()) {
            chosen.removeAt(chosen.lastIndex)
            pool = newPool()
        }
    }

    fun story(): String = template.render(picks)

    private fun newPool(): List<Word> =
        currentSlot?.let { WordRepository.pool(it, random) } ?: emptyList()
}
