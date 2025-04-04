package com.rarmash.b4cklog.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.rarmash.b4cklog.adapters.GameAdapter
import com.rarmash.b4cklog.databinding.FragmentSearchBinding
import com.rarmash.b4cklog.models.GameResponse
import com.rarmash.b4cklog.network.RetrofitClient
import com.rarmash.b4cklog.network.RetrofitClient.API_KEY
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var gameAdapter: GameAdapter
    private var lastQuery: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        gameAdapter = GameAdapter(emptyList())
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = gameAdapter
        binding.searchView.clearFocus()
        binding.searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text).setHintTextColor(Color.DKGRAY);
        binding.searchView.queryHint = "Искать игру"

        savedInstanceState?.let {
            lastQuery = it.getString("lastQuery")
            binding.searchView.setQuery(lastQuery, false)

            lastQuery?.let {
                fetchGames(it)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                searchRunnable?.let { handler.removeCallbacks(it) }

                searchRunnable = Runnable {
                    newText?.let {
                        lastQuery = it
                        fetchGames(it)
                    }
                }

                if (newText.isNullOrEmpty()) {
                    binding.searchView.clearFocus() // Убираем фокус при очистке
                    binding.searchView.postDelayed({
                        hideKeyboard()
                    }, 50)
                }

                handler.postDelayed(searchRunnable!!, 500) // Задержка 500 мс перед выполнением запроса
                return false
            }
        })

        binding.retryButton.setOnClickListener {
            lastQuery?.let { fetchGames(it) }
        }  
    }

    private fun fetchGames(searchQuery: String) {
        showLoadingState()

        RetrofitClient.gameApiService.getGames(searchQuery, API_KEY).enqueue(object : Callback<GameResponse> {
            override fun onResponse(call: Call<GameResponse>, response: Response<GameResponse>) {
                Log.d("SearchFragment", "Код ответа: ${response.code()}")

                if (response.isSuccessful) {
                    val games = response.body()?.games ?: emptyList()
                    gameAdapter.updateGames(games)
                    updatePlaceholder(games.isEmpty())
                } else {
                    Log.e("SearchFragment", "Ошибка: ${response.code()} ${response.errorBody()?.string() ?: "Нет инфо"}")
                    showErrorPlaceholder()
                }
            }

            override fun onFailure(call: Call<GameResponse>, t: Throwable) {
                Toast.makeText(context, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
                showErrorPlaceholder()
            }
        })
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
    }

    private fun showLoadingState() {
        binding.recyclerView.visibility = View.GONE
        binding.placeholderLayout.visibility = View.GONE
    }

    private fun updatePlaceholder(isEmpty: Boolean) {
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.placeholderLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.placeholderText.text = "Нет результатов"
        binding.retryButton.visibility = View.GONE
    }

    private fun showErrorPlaceholder() {
        binding.recyclerView.visibility = View.GONE
        binding.placeholderLayout.visibility = View.VISIBLE
        binding.placeholderText.text = "Ошибка загрузки данных"
        binding.retryButton.visibility = View.VISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("lastQuery", lastQuery)
    }
}
