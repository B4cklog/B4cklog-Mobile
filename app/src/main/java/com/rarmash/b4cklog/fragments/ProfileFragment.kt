package com.rarmash.b4cklog.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rarmash.b4cklog.R
import com.rarmash.b4cklog.adapters.GameAdapter
import com.rarmash.b4cklog.models.Game
import com.rarmash.b4cklog.models.User
import com.rarmash.b4cklog.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {
    private lateinit var userName: TextView
    private lateinit var userFullNameAge: TextView
    private lateinit var userEmail: TextView

    private lateinit var recyclerWantToPlay: RecyclerView
    private lateinit var recyclerPlaying: RecyclerView
    private lateinit var recyclerPlayed: RecyclerView
    private lateinit var recyclerCompleted: RecyclerView
    private lateinit var recyclerCompleted100: RecyclerView

    private lateinit var adapterWantToPlay: GameAdapter
    private lateinit var adapterPlaying: GameAdapter
    private lateinit var adapterPlayed: GameAdapter
    private lateinit var adapterCompleted: GameAdapter
    private lateinit var adapterCompleted100: GameAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        userName = view.findViewById(R.id.userName)
        userFullNameAge = view.findViewById(R.id.userFullNameAge)
        userEmail = view.findViewById(R.id.userEmail)

        recyclerWantToPlay = view.findViewById(R.id.recyclerWantToPlay)
        recyclerPlaying = view.findViewById(R.id.recyclerPlaying)
        recyclerPlayed = view.findViewById(R.id.recyclerPlayed)
        recyclerCompleted = view.findViewById(R.id.recyclerCompleted)
        recyclerCompleted100 = view.findViewById(R.id.recyclerCompleted100)

        // Настройка RecyclerView и адаптеров
        adapterWantToPlay = GameAdapter(emptyList(), ::onGameClick)
        adapterPlaying = GameAdapter(emptyList(), ::onGameClick)
        adapterPlayed = GameAdapter(emptyList(), ::onGameClick)
        adapterCompleted = GameAdapter(emptyList(), ::onGameClick)
        adapterCompleted100 = GameAdapter(emptyList(), ::onGameClick)

        recyclerWantToPlay.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerPlaying.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerPlayed.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerCompleted.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerCompleted100.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        recyclerWantToPlay.adapter = adapterWantToPlay
        recyclerPlaying.adapter = adapterPlaying
        recyclerPlayed.adapter = adapterPlayed
        recyclerCompleted.adapter = adapterCompleted
        recyclerCompleted100.adapter = adapterCompleted100

        val settingsButton = view.findViewById<FloatingActionButton>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_settingsFragment)
        }

        loadProfile()

        return view
    }

    private fun loadProfile() {
        ApiClient.profileApi.getUserProfile().enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    userName.text = user.username
                    userFullNameAge.text = "${user.firstName} ${user.lastName}, ${user.age}"
                    userEmail.text = user.email

                    adapterWantToPlay.updateGames(user.backlogWantToPlay)
                    adapterPlaying.updateGames(user.backlogPlaying)
                    adapterPlayed.updateGames(user.backlogPlayed)
                    adapterCompleted.updateGames(user.backlogCompleted)
                    adapterCompleted100.updateGames(user.backlogCompleted100)

                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(requireContext(), "Сеть: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun onGameClick(game: Game) {
        val bundle = bundleOf("gameId" to game.id)
        Navigation.findNavController(requireView()).navigate(R.id.action_profileFragment_to_gameDetailFragment, bundle)
    }
}
