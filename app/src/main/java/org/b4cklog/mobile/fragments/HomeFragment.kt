package org.b4cklog.mobile.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.b4cklog.mobile.R
import org.b4cklog.mobile.adapters.GameAdapter
import org.b4cklog.mobile.models.Game
import org.b4cklog.mobile.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var popularAdapter: GameAdapter
    private lateinit var latestAdapter: GameAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val popularRecyclerView = view.findViewById<RecyclerView>(R.id.popularGamesRecyclerView)
        val latestRecyclerView = view.findViewById<RecyclerView>(R.id.latestReleaseRecyclerView)

        popularRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        latestRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        popularAdapter = GameAdapter(emptyList()) { game ->
            val bundle = bundleOf("gameId" to game.id)
            Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_gameDetailFragment, bundle)
        }
        latestAdapter = GameAdapter(emptyList()) { game ->
            val bundle = bundleOf("gameId" to game.id)
            Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_gameDetailFragment, bundle)
        }

        popularRecyclerView.adapter = popularAdapter
        latestRecyclerView.adapter = latestAdapter

        // Full catalog button
//        view.findViewById<Button>(R.id.fullCatalogButton).setOnClickListener {
//            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_catalogFragment)
//        }

        loadGames()

        return view
    }

    private fun loadGames() {
        val api = ApiClient.gameApi

        api.getAllGames().enqueue(object : Callback<List<Game>> {
            override fun onResponse(call: Call<List<Game>>, response: Response<List<Game>>) {
                if (response.isSuccessful) {
                    val games = response.body() ?: emptyList()

                    val popularGames = games.take(10)
                    val latestGames = games.sortedByDescending { it.releaseDate }.take(10)

                    popularAdapter.updateGames(popularGames)
                    latestAdapter.updateGames(latestGames)
                }
            }

            override fun onFailure(call: Call<List<Game>>, t: Throwable) {
                Toast.makeText(context, getString(R.string.getting_data_error), Toast.LENGTH_SHORT).show()
            }
        })
    }
}
