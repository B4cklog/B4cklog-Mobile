package com.rarmash.b4cklog.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.rarmash.b4cklog.R
import com.rarmash.b4cklog.models.Game
import com.rarmash.b4cklog.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GameDetailFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gameId = arguments?.getInt("gameId") ?: return

        loadGameDetails(gameId)
    }

    private fun loadGameDetails(id: Int) {
        ApiClient.gameApi.getGame(id).enqueue(object : Callback<Game> {
            override fun onResponse(call: Call<Game>, response: Response<Game>) {
                if (response.isSuccessful) {
                    val game = response.body() ?: return

                    val nameView = view?.findViewById<TextView>(R.id.game_name)
                    val summaryView = view?.findViewById<TextView>(R.id.game_summary)
                    val releaseDateView = view?.findViewById<TextView>(R.id.game_release_date)
                    val platformsView = view?.findViewById<TextView>(R.id.game_platforms)
                    val coverView = view?.findViewById<ImageView>(R.id.game_cover)

                    nameView?.text = game.name
                    summaryView?.text = game.summary
                    releaseDateView?.text = "Дата выхода: ${game.releaseDate}"
                    platformsView?.text = "Платформы: ${
                        game.platforms.joinToString(", ") { it.name }
                    }"

                    Glide.with(requireContext())
                        .load(game.cover)
                        .into(coverView!!)
                }
            }

            override fun onFailure(call: Call<Game>, t: Throwable) {
                Toast.makeText(context, "Ошибка загрузки игры", Toast.LENGTH_SHORT).show()
            }
        })
    }

}