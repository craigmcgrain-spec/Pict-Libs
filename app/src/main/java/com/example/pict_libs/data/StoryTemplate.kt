package com.example.pict_libs.data

data class StoryTemplate(
    val id: String,
    val slots: List<WordSubcategory>,
    val fragments: List<String>
) {
    init {
        require(slots.size == 6)
        require(fragments.size == slots.size + 1)
    }

    fun render(picks: List<Word>): String {
        require(picks.size == slots.size) { "Fill all six blanks first" }
        require(picks.indices.all { picks[it].subcategory == slots[it] })
        return buildString {
            append(fragments.first())
            picks.forEachIndexed { index, word ->
                append(word.text)
                append(fragments[index + 1])
            }
        }
    }
}

object StoryTemplates {
    val all = listOf(
        StoryTemplate(
            "moon_picnic",
            listOf(WordSubcategory.APPEARANCE, WordSubcategory.ANIMALS,
                WordSubcategory.MOVEMENT, WordSubcategory.MOODS,
                WordSubcategory.FOOD, WordSubcategory.SOUNDS),
            listOf("On the moon, the ", " ", " decided to ",
                " with the ", " ", ", which suddenly began to ", "!")
        ),
        StoryTemplate(
            "school_lunch",
            listOf(WordSubcategory.MOODS, WordSubcategory.FOOD,
                WordSubcategory.SOUNDS, WordSubcategory.APPEARANCE,
                WordSubcategory.ANIMALS, WordSubcategory.MOVEMENT),
            listOf("At lunchtime, the ", " ", " began to ",
                ", so the ", " ", " had to ", " all the way to the principal’s office!")
        ),
        StoryTemplate(
            "talent_show",
            listOf(WordSubcategory.ANIMALS, WordSubcategory.MOVEMENT,
                WordSubcategory.APPEARANCE, WordSubcategory.FOOD,
                WordSubcategory.MOODS, WordSubcategory.SOUNDS),
            listOf("The ", " tried to ", " on top of the ", " ",
                ", making the ", " audience ", " until bedtime!")
        )
    )
}
