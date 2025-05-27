package com.rarmash.b4cklog.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.rarmash.b4cklog.R
import com.rarmash.b4cklog.models.Game
import com.rarmash.b4cklog.models.User
import com.rarmash.b4cklog.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GameDetailFragment : Fragment() {
    private var currentListName: String? = null
    private var currentUserId: Int? = null

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

        val addToBacklogButton = view.findViewById<Button>(R.id.add_to_backlog_button)
        val removeFromBacklogButton = view.findViewById<ImageButton>(R.id.remove_from_backlog_button)

        val listTextView = view.findViewById<TextView>(R.id.game_list_info)

        addToBacklogButton.setOnClickListener {
            showAddToListDialog(gameId)
        }

        loadCurrentUserAndCheckList(gameId, removeFromBacklogButton, listTextView)
    }

    private fun loadCurrentUserAndCheckList(gameId: Int, removeButton: ImageButton, listText: TextView) {
        ApiClient.profileApi.getUserProfile().enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (!response.isSuccessful) return
                val user = response.body() ?: return
                currentUserId = user.id

                currentListName = when {
                    user.backlogWantToPlay.any { it.id == gameId } -> getString(R.string.want_to_play)
                    user.backlogPlaying.any { it.id == gameId } -> getString(R.string.playing)
                    user.backlogPlayed.any { it.id == gameId } -> getString(R.string.played)
                    user.backlogCompleted.any { it.id == gameId } -> getString(R.string.completed)
                    user.backlogCompleted100.any { it.id == gameId } -> getString(R.string.completed_100)
                    else -> null
                }

                currentListName?.let { list ->
                    listText.text = "В списке: $list"
                    listText.visibility = View.VISIBLE
                    removeButton.visibility = View.VISIBLE
                    removeButton.setOnClickListener {
                        currentUserId?.let { uid ->
                            ApiClient.profileApi.removeGameFromAllLists(uid, gameId)
                                .enqueue(object : Callback<Void> {
                                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                        if (response.isSuccessful) {
                                            Toast.makeText(context, "Удалено из всех списков", Toast.LENGTH_SHORT).show()
                                            // Можно обновить UI: скрыть кнопку и текст
                                            removeButton.visibility = View.GONE
                                            listText.visibility = View.GONE
                                        } else {
                                            Toast.makeText(context, "Ошибка удаления", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                    override fun onFailure(call: Call<Void>, t: Throwable) {
                                        Toast.makeText(context, "Ошибка сети", Toast.LENGTH_SHORT).show()
                                    }
                                })
                        }
                    }

                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(context, "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show()
            }
        })
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

    private fun showAddToListDialog(gameId: Int) {
        val listNames = arrayOf(
            getString(R.string.want_to_play),
            getString(R.string.playing),
            getString(R.string.played),
            getString(R.string.completed),
            getString(R.string.completed_100),
        )

        val listKeys = arrayOf(
            "wantToPlay",
            "playing",
            "played",
            "completed",
            "completed100"
        )

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Добавить в список")
            .setItems(listNames) { _, which ->
                val listKey = listKeys[which]
                addGameToBacklog(gameId, listKey)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun addGameToBacklog(gameId: Int, listName: String) {
        ApiClient.profileApi.getUserProfile().enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val userId = response.body()?.id ?: return

                    ApiClient.profileApi.addGameToList(userId, gameId, listName)
                        .enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Добавлено в список", Toast.LENGTH_SHORT).show()
                                    val removeFromBacklogButton = view?.findViewById<ImageButton>(R.id.remove_from_backlog_button)
                                    val listTextView = view?.findViewById<TextView>(R.id.game_list_info)
                                    if (removeFromBacklogButton != null && listTextView != null) {
                                        loadCurrentUserAndCheckList(gameId, removeFromBacklogButton, listTextView)
                                    }
                                } else {
                                    Toast.makeText(context, "Ошибка добавления", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Toast.makeText(context, "Ошибка сети", Toast.LENGTH_SHORT).show()
                            }
                        })
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(context, "Не удалось получить профиль", Toast.LENGTH_SHORT).show()
            }
        })
    }
}