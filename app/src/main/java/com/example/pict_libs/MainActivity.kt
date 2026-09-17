package com.example.pict_libs

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.pict_libs.adapters.WordAdapter
import com.example.pict_libs.data.GameSession
import com.example.pict_libs.data.StoryTemplates
import com.example.pict_libs.data.WordCategory
import com.example.pict_libs.data.WordRepository
import com.example.pict_libs.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: WordAdapter
    private lateinit var game: GameSession

    private val storyLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) game = GameSession() else game.undo()
        renderRound()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        game = if (savedInstanceState == null) GameSession() else GameSession(
            template = StoryTemplates.all.single { it.id == savedInstanceState.getString("template") },
            picks = savedInstanceState.getStringArrayList("picks")!!.map(WordRepository::find),
            pool = savedInstanceState.getStringArrayList("pool")!!.map(WordRepository::find)
        )

        adapter = WordAdapter { word ->
            game.choose(word)
            renderRound()
            if (game.isComplete) showStory()
        }
        binding.wordGrid.layoutManager = GridLayoutManager(this, 3)
        binding.wordGrid.adapter = adapter
        binding.undoButton.setOnClickListener {
            game.undo()
            renderRound()
        }
        binding.generateButton.setOnClickListener { showStory() }
        renderRound()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("template", game.template.id)
        outState.putStringArrayList("picks", ArrayList(game.picks.map { it.text }))
        outState.putStringArrayList("pool", ArrayList(game.pool.map { it.text }))
        super.onSaveInstanceState(outState)
    }

    private fun renderRound() {
        binding.selectionCount.text = if (game.isComplete) getString(R.string.all_words_chosen)
            else getString(R.string.round_count, game.picks.size + 1)
        binding.roundPrompt.text = game.currentSlot?.let { slot ->
            val category = getString(when (slot.category) {
                WordCategory.ADJECTIVE -> R.string.category_adjective
                WordCategory.NOUN -> R.string.category_noun
                WordCategory.VERB -> R.string.category_verb
            })
            getString(R.string.round_prompt, category, slot.label)
        } ?: getString(R.string.story_ready)
        binding.chosenWords.text = game.picks.joinToString("  •  ") { "${it.emoji} ${it.text}" }
        binding.chosenWords.visibility = if (game.picks.isEmpty()) View.GONE else View.VISIBLE
        binding.undoButton.isEnabled = game.picks.isNotEmpty()
        binding.generateButton.visibility = if (game.isComplete) View.VISIBLE else View.GONE
        adapter.submitList(game.pool)
        binding.wordGrid.scrollToPosition(0)
    }

    private fun showStory() {
        storyLauncher.launch(Intent(this, StoryActivity::class.java).apply {
            putExtra(StoryActivity.STORY_TEXT, game.story())
        })
    }
}
