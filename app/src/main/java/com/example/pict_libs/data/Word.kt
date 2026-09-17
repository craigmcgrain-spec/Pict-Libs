package com.example.pict_libs.data

data class Word(
    val text: String,
    val emoji: String,
    val subcategory: WordSubcategory
) {
    val category: WordCategory get() = subcategory.category
}

enum class WordCategory {
    ADJECTIVE,
    NOUN,
    VERB
}

enum class WordSubcategory(val category: WordCategory, val label: String) {
    APPEARANCE(WordCategory.ADJECTIVE, "Looks & textures"),
    MOODS(WordCategory.ADJECTIVE, "Moods & personalities"),
    ANIMALS(WordCategory.NOUN, "Animals"),
    FOOD(WordCategory.NOUN, "Food"),
    MOVEMENT(WordCategory.VERB, "Movement"),
    SOUNDS(WordCategory.VERB, "Sounds")
}
