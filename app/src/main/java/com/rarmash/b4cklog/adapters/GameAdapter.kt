package com.rarmash.b4cklog.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rarmash.b4cklog.R
import com.rarmash.b4cklog.models.Game

class GameAdapter(
    private var games: List<Game>,
    private val onClick: (Game) -> Unit
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    inner class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val coverImage = itemView.findViewById<ImageView>(R.id.gameCover)
        private val nameText = itemView.findViewById<TextView>(R.id.gameName)

        fun bind(game: Game) {
            nameText.text = game.name

            Glide.with(itemView.context)
                .load(game.cover)
                .placeholder(R.drawable.avatar) //TODO: Поменять плейсхолдер
                //.error(R.drawable.placeholder_cover)
                .into(coverImage)

            itemView.setOnClickListener { onClick(game) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_game, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(games[position])
    }

    override fun getItemCount(): Int = games.size

    fun updateGames(newGames: List<Game>) {
        games = newGames
        notifyDataSetChanged()
    }
}

