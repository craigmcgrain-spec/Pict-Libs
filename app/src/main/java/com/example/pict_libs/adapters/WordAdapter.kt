package com.example.pict_libs.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.pict_libs.R
import com.example.pict_libs.data.Word
import com.example.pict_libs.data.WordCategory
import com.google.android.material.card.MaterialCardView

class WordAdapter(private val onWordClicked: (Word) -> Unit) :
    RecyclerView.Adapter<WordAdapter.WordViewHolder>() {
    private var words: List<Word> = emptyList()

    fun submitList(newWords: List<Word>) {
        words = newWords
        notifyDataSetChanged()
    }

    class WordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.wordCard)
        val emoji: TextView = view.findViewById(R.id.wordEmoji)
        val text: TextView = view.findViewById(R.id.wordText)
        val category: TextView = view.findViewById(R.id.wordCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = WordViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_word, parent, false)
    )

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val word = words[position]
        holder.emoji.text = word.emoji
        holder.text.text = word.text
        holder.category.text = word.subcategory.label
        holder.category.setTextColor(ContextCompat.getColor(holder.itemView.context,
            when (word.category) {
                WordCategory.ADJECTIVE -> R.color.bright_pink
                WordCategory.NOUN -> R.color.bright_blue
                WordCategory.VERB -> R.color.bright_green
            }
        ))
        holder.card.setOnClickListener {
            // Ignore stale holders while the next round is being laid out.
            val index = holder.bindingAdapterPosition
            if (index != RecyclerView.NO_POSITION && words.getOrNull(index) == word) {
                onWordClicked(word)
            }
        }
    }

    override fun getItemCount(): Int = words.size
}
