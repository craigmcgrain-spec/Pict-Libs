package com.example.pict_libs.data

import kotlin.random.Random

object WordRepository {
    const val POOL_SIZE = 12

    private fun words(theme: WordSubcategory, vararg entries: Pair<String, String>) =
        entries.map { (text, emoji) -> Word(text, emoji, theme) }

    val allWords: List<Word> =
        words(WordSubcategory.APPEARANCE,
            "sparkly" to "✨", "fuzzy" to "🧸", "fluffy" to "☁️", "gooey" to "🍯",
            "tiny" to "🐜", "gigantic" to "🐘", "glowing" to "💡", "frozen" to "🧊",
            "stripy" to "🦓", "spotty" to "🐆", "rainbow-colored" to "🌈", "muddy" to "🐾",
            "spiky" to "🌵", "round" to "⚽", "shiny" to "💎", "wrinkly" to "👴"
        ) + words(WordSubcategory.MOODS,
            "happy" to "😀", "grumpy" to "😠", "sleepy" to "😴", "silly" to "🤪",
            "excited" to "🤩", "shy" to "🫣", "brave" to "🦸", "curious" to "🧐",
            "confused" to "😕", "cheerful" to "😄", "surprised" to "😮", "proud" to "😎",
            "giggly" to "🤭", "calm" to "😌", "friendly" to "🤗", "mischievous" to "😏"
        ) + words(WordSubcategory.ANIMALS,
            "penguin" to "🐧", "dinosaur" to "🦕", "octopus" to "🐙", "elephant" to "🐘",
            "giraffe" to "🦒", "frog" to "🐸", "monkey" to "🐒", "panda" to "🐼",
            "turtle" to "🐢", "rabbit" to "🐰", "duck" to "🦆", "cat" to "🐱",
            "dog" to "🐶", "lion" to "🦁", "koala" to "🐨", "hamster" to "🐹"
        ) + words(WordSubcategory.FOOD,
            "pizza" to "🍕", "banana" to "🍌", "taco" to "🌮", "cupcake" to "🧁",
            "watermelon" to "🍉", "pancake" to "🥞", "pickle" to "🥒", "carrot" to "🥕",
            "sandwich" to "🥪", "doughnut" to "🍩", "cookie" to "🍪", "potato" to "🥔",
            "strawberry" to "🍓", "pineapple" to "🍍", "lollipop" to "🍭", "hot dog" to "🌭"
        ) + words(WordSubcategory.MOVEMENT,
            "dance" to "💃", "jump" to "🦘", "fly" to "🦅", "spin" to "🌀",
            "run" to "🏃", "swim" to "🏊", "crawl" to "🐛", "slide" to "🛝",
            "skate" to "⛸️", "ski" to "⛷️", "roll" to "🎳", "hop" to "🐇",
            "climb" to "🧗", "waddle" to "🐧", "wiggle" to "🪱", "march" to "🥁"
        ) + words(WordSubcategory.SOUNDS,
            "sing" to "🎤", "sneeze" to "🤧", "laugh" to "😂", "whistle" to "🎶",
            "roar" to "🦁", "squeak" to "🐭", "quack" to "🦆", "bark" to "🐶",
            "meow" to "🐱", "moo" to "🐮", "hoot" to "🦉", "croak" to "🐸",
            "chirp" to "🐥", "snore" to "💤", "hum" to "🎵", "giggle" to "🤭"
        )

    fun pool(subcategory: WordSubcategory, random: Random = Random.Default): List<Word> {
        val candidates = allWords.filter { it.subcategory == subcategory }
        require(candidates.size >= POOL_SIZE) { "Each subcategory needs at least 12 words" }
        return candidates.shuffled(random).take(POOL_SIZE)
    }

    fun find(text: String): Word = allWords.single { it.text == text }
}
