package com.example.pict_libs.data

data class StoryPart(
    val fragments: List<String>,
    val slots: List<WordSubcategory> = emptyList()
) {
    init {
        require(fragments.size == slots.size + 1) {
            "Fragments (${fragments.size}) must equal slots (${slots.size}) + 1"
        }
    }

    fun render(picks: List<Word>, startIndex: Int): String {
        return buildString {
            append(fragments.first())
            val partPicks = picks.slice(startIndex until startIndex + slots.size)
            partPicks.forEachIndexed { index, word ->
                append(word.text)
                append(fragments[index + 1])
            }
        }
    }
}

data class StoryTemplate(
    val id: String,
    val title: String,
    val parts: List<StoryPart>
) {
    val totalBlanks: Int get() = parts.sumOf { it.slots.size }
    val allSlots: List<WordSubcategory> get() = parts.flatMap { it.slots }

    init {
        require(totalBlanks == 6) { "Template must have exactly 6 blanks, has $totalBlanks" }
    }

    fun render(picks: List<Word>): String {
        require(picks.size == totalBlanks) { "Fill all $totalBlanks blanks first" }
        require(picks.indices.all { picks[it].subcategory == allSlots[it] })
        
        return buildString {
            var startIndex = 0
            parts.forEachIndexed { partIndex, part ->
                if (partIndex > 0) append("\n\n")
                append(part.render(picks, startIndex))
                startIndex += part.slots.size
            }
        }
    }
}

object StoryTemplates {
    val all = listOf(
        // Space Adventure: 1+2+3=6 blanks
        StoryTemplate(
            "space_adventure",
            "Space Adventure",
            listOf(
                StoryPart(
                    listOf("The ", " flew through space."),
                    listOf(WordSubcategory.ANIMALS)
                ),
                StoryPart(
                    listOf("It landed on a ", " planet with a ", " friend."),
                    listOf(WordSubcategory.APPEARANCE, WordSubcategory.MOODS)
                ),
                StoryPart(
                    listOf("They decided to ", " while eating ", " and ", " happily!"),
                    listOf(WordSubcategory.MOVEMENT, WordSubcategory.FOOD, WordSubcategory.SOUNDS)
                ),
            )
        ),

        // Jungle Safari: 2+3+1=6 blanks
        StoryTemplate(
            "jungle_safari",
            "Jungle Safari",
            listOf(
                StoryPart(
                    listOf("Deep in the jungle, a ", " tried to ", " hide."),
                    listOf(WordSubcategory.ANIMALS, WordSubcategory.MOVEMENT)
                ),
                StoryPart(
                    listOf("Behind a ", " tree, a ", " monkey watched them ", ""),
                    listOf(WordSubcategory.APPEARANCE, WordSubcategory.MOODS, WordSubcategory.SOUNDS)
                ),
                StoryPart(
                    listOf(" while eating a big ", "!"),
                    listOf(WordSubcategory.FOOD)
                ),
            )
        ),

        // Ocean Discovery: 2+0+1+3=6 blanks
        StoryTemplate(
            "ocean_discovery",
            "Ocean Discovery",
            listOf(
                StoryPart(
                    listOf("The brave ", " swam through the ", " ocean."),
                    listOf(WordSubcategory.ANIMALS, WordSubcategory.APPEARANCE)
                ),
                StoryPart(
                    listOf("It found a treasure cave."),
                    emptyList()
                ),
                StoryPart(
                    listOf("Inside was a ", " fish that loved to swim!"),
                    listOf(WordSubcategory.MOODS)
                ),
                StoryPart(
                    listOf("They decided to ", " and ", " all day with the ", ""),
                    listOf(WordSubcategory.MOVEMENT, WordSubcategory.SOUNDS, WordSubcategory.FOOD)
                ),
            )
        ),

        // Magic School: 1+1+1+1+1+1=6 blanks
        StoryTemplate(
            "magic_school",
            "Magic School",
            listOf(
                StoryPart(
                    listOf("At magic school, a ", ""),
                    listOf(WordSubcategory.ANIMALS)
                ),
                StoryPart(
                    listOf(" student cast a spell on their ", ""),
                    listOf(WordSubcategory.APPEARANCE)
                ),
                StoryPart(
                    listOf(" homework and turned it into ", ""),
                    listOf(WordSubcategory.FOOD)
                ),
                StoryPart(
                    listOf(". The teacher was very ", ""),
                    listOf(WordSubcategory.MOODS)
                ),
                StoryPart(
                    listOf(" and made everyone ", ""),
                    listOf(WordSubcategory.SOUNDS)
                ),
                StoryPart(
                    listOf(" until lunchtime!", ""),
                    listOf(WordSubcategory.MOVEMENT)
                ),
            )
        ),

        // Superhero Training: 1+2+1+1+1=6 blanks
        StoryTemplate(
            "superhero_training",
            "Superhero Training",
            listOf(
                StoryPart(
                    listOf("The ", ""),
                    listOf(WordSubcategory.ANIMALS)
                ),
                StoryPart(
                    listOf(" trained to be a ", " superhero. They had to ", ""),
                    listOf(WordSubcategory.MOODS, WordSubcategory.APPEARANCE)
                ),
                StoryPart(
                    listOf(" across the city and ", ""),
                    listOf(WordSubcategory.MOVEMENT)
                ),
                StoryPart(
                    listOf(" while eating ", ""),
                    listOf(WordSubcategory.SOUNDS)
                ),
                StoryPart(
                    listOf(" like a hero!", ""),
                    listOf(WordSubcategory.FOOD)
                ),
            )
        ),

        // Dinosaur World: 1+1+1+2+1=6 blanks
        StoryTemplate(
            "dinosaur_world",
            "Dinosaur World",
            listOf(
                StoryPart(
                    listOf("In prehistoric times, a ", ""),
                    listOf(WordSubcategory.ANIMALS)
                ),
                StoryPart(
                    listOf(" dinosaur lived on a ", ""),
                    listOf(WordSubcategory.APPEARANCE)
                ),
                StoryPart(
                    listOf(" island. It loved to eat ", ""),
                    listOf(WordSubcategory.FOOD)
                ),
                StoryPart(
                    listOf(". One day it started to ", " and ", ""),
                    listOf(WordSubcategory.MOVEMENT, WordSubcategory.SOUNDS)
                ),
                StoryPart(
                    listOf(" , making all the other dinosaurs very ", ""),
                    listOf(WordSubcategory.MOODS)
                ),
            )
        ),

        // Cooking Contest: 1+2+1+1+1=6 blanks
        StoryTemplate(
            "cooking_contest",
            "Cooking Contest",
            listOf(
                StoryPart(
                    listOf("The famous ", ""),
                    listOf(WordSubcategory.ANIMALS)
                ),
                StoryPart(
                    listOf(" entered the cooking contest. They made a ", " dish using ", ""),
                    listOf(WordSubcategory.APPEARANCE, WordSubcategory.FOOD)
                ),
                StoryPart(
                    listOf(". The judges were very ", ""),
                    listOf(WordSubcategory.MOODS)
                ),
                StoryPart(
                    listOf(" and started to ", ""),
                    listOf(WordSubcategory.SOUNDS)
                ),
                StoryPart(
                    listOf(" ! Everyone had to ", ""),
                    listOf(WordSubcategory.MOVEMENT)
                ),
            )
        ),

        // Circus Performance: 1+2+1+1+1=6 blanks
        StoryTemplate(
            "circus_performance",
            "Circus Performance",
            listOf(
                StoryPart(
                    listOf("The ", ""),
                    listOf(WordSubcategory.ANIMALS)
                ),
                StoryPart(
                    listOf(" performed at the big circus. They wore a ", " costume and ate ", ""),
                    listOf(WordSubcategory.APPEARANCE, WordSubcategory.FOOD)
                ),
                StoryPart(
                    listOf(" before going on stage. The crowd was very ", ""),
                    listOf(WordSubcategory.MOODS)
                ),
                StoryPart(
                    listOf(" and began to ", ""),
                    listOf(WordSubcategory.SOUNDS)
                ),
                StoryPart(
                    listOf(" ! The performer had to ", ""),
                    listOf(WordSubcategory.MOVEMENT)
                ),
            )
        ),

        // Snow Adventure: 1+1+1+2+1=6 blanks
        StoryTemplate(
            "snow_adventure",
            "Snow Adventure",
            listOf(
                StoryPart(
                    listOf("In the frozen north, a ", ""),
                    listOf(WordSubcategory.ANIMALS)
                ),
                StoryPart(
                    listOf(" built a house out of ", " snow."),
                    listOf(WordSubcategory.APPEARANCE)
                ),
                StoryPart(
                    listOf(" They decorated it with and ate ", ""),
                    listOf(WordSubcategory.FOOD)
                ),
                StoryPart(
                    listOf(". When the storm came, they had to ", " and ", ""),
                    listOf(WordSubcategory.MOVEMENT, WordSubcategory.SOUNDS)
                ),
                StoryPart(
                    listOf(" , feeling very ", ""),
                    listOf(WordSubcategory.MOODS)
                ),
            )
        ),

        // Robot Factory: 1+1+2+2=6 blanks
        StoryTemplate(
            "robot_factory",
            "Robot Factory",
            listOf(
                StoryPart(
                    listOf("In the robot factory, a ", ""),
                    listOf(WordSubcategory.ANIMALS)
                ),
                StoryPart(
                    listOf(" machine started to act ", ""),
                    listOf(WordSubcategory.MOODS)
                ),
                StoryPart(
                    listOf(". It made ", " noises and tried to ", ""),
                    listOf(WordSubcategory.SOUNDS, WordSubcategory.MOVEMENT)
                ),
                StoryPart(
                    listOf(". The workers were very when it ate all the ", " and ", ""),
                    listOf(WordSubcategory.APPEARANCE, WordSubcategory.FOOD)
                ),
            )
        ),

        // Beach Day: 1+2+0+1+2=6 blanks
        StoryTemplate(
            "beach_day",
            "Beach Day",
            listOf(
                StoryPart(
                    listOf("At the beach, a ", ""),
                    listOf(WordSubcategory.ANIMALS)
                ),
                StoryPart(
                    listOf(" built the biggest ", " castle ever. Then a ", " wave came."),
                    listOf(WordSubcategory.APPEARANCE, WordSubcategory.FOOD)
                ),
                StoryPart(
                    listOf(" and made everyone watch."),
                    emptyList()
                ),
                StoryPart(
                    listOf(" . The ", ""),
                    listOf(WordSubcategory.ANIMALS)
                ),
                StoryPart(
                    listOf(" dog had to ", " all the way home while ", " about the adventure!"),
                    listOf(WordSubcategory.MOVEMENT, WordSubcategory.SOUNDS)
                ),
            )
        ),

        // Mountain Climbing: 1+2+1+1+1=6 blanks
        StoryTemplate(
            "mountain_climbing",
            "Mountain Climbing",
            listOf(
                StoryPart(
                    listOf("The ", ""),
                    listOf(WordSubcategory.ANIMALS)
                ),
                StoryPart(
                    listOf(" climbed the tallest mountain. They wore ", " boots and carried ", ""),
                    listOf(WordSubcategory.APPEARANCE, WordSubcategory.FOOD)
                ),
                StoryPart(
                    listOf(". At the top, they felt very ", ""),
                    listOf(WordSubcategory.MOODS)
                ),
                StoryPart(
                    listOf(" and started to ", ""),
                    listOf(WordSubcategory.SOUNDS)
                ),
                StoryPart(
                    listOf(" ! Then they had to ", ""),
                    listOf(WordSubcategory.MOVEMENT)
                ),
            )
        ),

        // Nighttime Stories: 1+2+1+1+1=6 blanks
        StoryTemplate(
            "nighttime_stories",
            "Nighttime Stories",
            listOf(
                StoryPart(
                    listOf("At bedtime, the ", ""),
                    listOf(WordSubcategory.ANIMALS)
                ),
                StoryPart(
                    listOf(" told a story about a ", " dragon. The dragon loved to eat ", ""),
                    listOf(WordSubcategory.APPEARANCE, WordSubcategory.FOOD)
                ),
                StoryPart(
                    listOf(". It could ", ""),
                    listOf(WordSubcategory.MOVEMENT)
                ),
                StoryPart(
                    listOf(" and ", ""),
                    listOf(WordSubcategory.SOUNDS)
                ),
                StoryPart(
                    listOf(" , making everyone very ", ""),
                    listOf(WordSubcategory.MOODS)
                ),
            )
        ),

        // Magic Garden: 1+2+1+1+1=6 blanks
        StoryTemplate(
            "magic_garden",
            "Magic Garden",
            listOf(
                StoryPart(
                    listOf("In the magic garden, a ", ""),
                    listOf(WordSubcategory.ANIMALS)
                ),
                StoryPart(
                    listOf(" planted seeds that grew into ", " flowers. The ", " petals smelled great."),
                    listOf(WordSubcategory.APPEARANCE, WordSubcategory.FOOD)
                ),
                StoryPart(
                    listOf(". The flowers started to ", ""),
                    listOf(WordSubcategory.MOVEMENT)
                ),
                StoryPart(
                    listOf(" and ", ""),
                    listOf(WordSubcategory.SOUNDS)
                ),
                StoryPart(
                    listOf(" , making the butterfly very ", ""),
                    listOf(WordSubcategory.MOODS)
                ),
            )
        ),

        // Train Journey: 1+2+1+1+1=6 blanks
        StoryTemplate(
            "train_journey",
            "Train Journey",
            listOf(
                StoryPart(
                    listOf("The ", ""),
                    listOf(WordSubcategory.ANIMALS)
                ),
                StoryPart(
                    listOf(" got on a magical train. It was very ", " and carried lots of ", ""),
                    listOf(WordSubcategory.APPEARANCE, WordSubcategory.FOOD)
                ),
                StoryPart(
                    listOf(". The train started to ", ""),
                    listOf(WordSubcategory.MOVEMENT)
                ),
                StoryPart(
                    listOf(" through the countryside, making ", ""),
                    listOf(WordSubcategory.SOUNDS)
                ),
                StoryPart(
                    listOf(" . Everyone was very ", ""),
                    listOf(WordSubcategory.MOODS)
                ),
            )
        ),

        // Pirate Treasure: 1+2+1+1+1=6 blanks
        StoryTemplate(
            "pirate_treasure",
            "Pirate Treasure",
            listOf(
                StoryPart(
                    listOf("The ", ""),
                    listOf(WordSubcategory.ANIMALS)
                ),
                StoryPart(
                    listOf(" sailed the seas looking for treasure. They wore ", " hats and ate ", ""),
                    listOf(WordSubcategory.APPEARANCE, WordSubcategory.FOOD)
                ),
                StoryPart(
                    listOf(". When they found the island, they had to ", ""),
                    listOf(WordSubcategory.MOVEMENT)
                ),
                StoryPart(
                    listOf(" through the forest while ", ""),
                    listOf(WordSubcategory.SOUNDS)
                ),
                StoryPart(
                    listOf(" , feeling very ", ""),
                    listOf(WordSubcategory.MOODS)
                ),
            )
        ),

    )
}
