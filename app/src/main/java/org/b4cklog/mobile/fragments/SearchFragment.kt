package org.b4cklog.mobile.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import org.b4cklog.mobile.R
import org.b4cklog.mobile.adapters.GameAdapter
import org.b4cklog.mobile.adapters.SearchHistoryAdapter
import org.b4cklog.mobile.databinding.FragmentSearchBinding
import org.b4cklog.mobile.models.Game
import org.b4cklog.mobile.network.ApiClient
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.navigation.findNavController
import androidx.core.content.edit

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var gameAdapter: GameAdapter
    private lateinit var historyAdapter: SearchHistoryAdapter
    private var lastQuery: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private val SEARCH_DELAY = 2000L
    private val HISTORY_PREF = "search_history"
    private val MAX_HISTORY_SIZE = 10
    private var isShowingHistory = true
    private var restoringState = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        gameAdapter = GameAdapter(emptyList()) { game ->
            val bundle = bundleOf("gameId" to game.id)
            requireView().findNavController().navigate(R.id.action_searchFragment_to_gameDetailFragment, bundle)
        }
        historyAdapter = SearchHistoryAdapter(loadHistory()) { selectedQuery ->
            binding.searchView.setQuery(selectedQuery, false)
            saveToHistory(selectedQuery)
            fetchGames(selectedQuery)
            hideHistory()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = gameAdapter

        binding.searchView.clearFocus()
        binding.searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            .setHintTextColor(android.graphics.Color.DKGRAY)
        binding.searchView.queryHint = getString(R.string.search_game)

        // Restore state if there is a saved request
        savedInstanceState?.let {
            restoringState = true
            lastQuery = it.getString("lastQuery")
            binding.searchView.setQuery(lastQuery, false)
            lastQuery?.let { fetchGames(it) }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus && loadHistory().isNotEmpty() && !restoringState) {
                showHistory()
            } else {
                hideHistory()
            }
        }

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchRunnable?.let { handler.removeCallbacks(it) }

                query?.let {
                    saveToHistory(it)
                    fetchGames(it)
                    hideHistory()

                    binding.searchView.clearFocus()
                    binding.searchView.postDelayed({
                        hideKeyboard()
                    }, 100)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchRunnable?.let { handler.removeCallbacks(it) }

                if (newText.isNullOrEmpty()) {
                    if (loadHistory().isNotEmpty()) {
                        showHistory()
                    }

                    binding.searchView.clearFocus()
                    binding.searchView.postDelayed({
                        hideKeyboard()
                    }, 100)

                    return false
                }

                searchRunnable = Runnable {
                    saveToHistory(newText)
                    fetchGames(newText)
                    hideHistory()
                }
                handler.postDelayed(searchRunnable!!, SEARCH_DELAY)

                return false
            }
        })

        binding.retryButton.setOnClickListener {
            lastQuery?.let { fetchGames(it) }
        }

        binding.clearHistoryButton.setOnClickListener {
            clearHistory()
            hideHistory()
        }

        restoringState = false
    }

    private fun fetchGames(searchQuery: String) {
        if (searchQuery == lastQuery) return

        lastQuery = searchQuery
        showLoadingState()

        ApiClient.gameApi.searchGames(searchQuery).enqueue(object : Callback<List<Game>> {
            override fun onResponse(call: Call<List<Game>>, response: Response<List<Game>>) {
                if (response.isSuccessful) {
                    val games = response.body() ?: emptyList()
                    gameAdapter.updateGames(games)
                    binding.progressBar.visibility = View.GONE
                    updatePlaceholder(games.isEmpty())
                } else {
                    showErrorPlaceholder()
                }
            }

            override fun onFailure(call: Call<List<Game>>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "${getString(R.string.network_error)}: ${t.message}", Toast.LENGTH_SHORT).show()
                showErrorPlaceholder()
            }
        })
    }

    private fun showLoadingState() {
        binding.recyclerView.visibility = View.GONE
        binding.placeholderLayout.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun updatePlaceholder(isEmpty: Boolean) {
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.placeholderLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.placeholderText.text = getString(R.string.no_results)
        binding.retryButton.visibility = View.GONE
    }

    private fun showErrorPlaceholder() {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.placeholderLayout.visibility = View.VISIBLE
        binding.placeholderText.text = getString(R.string.getting_data_error)
        binding.retryButton.visibility = View.VISIBLE
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
    }

    private fun saveToHistory(query: String) {
        val prefs = requireContext().getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
        val history = loadHistory().toMutableList()

        history.remove(query)
        history.add(0, query)
        val limited = history.take(MAX_HISTORY_SIZE)

        val jsonArray = JSONArray()
        for (item in limited) {
            jsonArray.put(item)
        }

        prefs.edit { putString(HISTORY_PREF, jsonArray.toString()) }
    }

    private fun loadHistory(): List<String> {
        val prefs = requireContext().getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString(HISTORY_PREF, null) ?: return emptyList()

        return try {
            val jsonArray = JSONArray(json)
            List(jsonArray.length()) { i -> jsonArray.getString(i) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun clearHistory() {
        val prefs = requireContext().getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
        prefs.edit { remove(HISTORY_PREF) }
    }

    private fun showHistory() {
        val history = loadHistory()
        if (history.isNotEmpty()) {
            isShowingHistory = true
            binding.recyclerView.adapter = historyAdapter
            historyAdapter.update(history)
            binding.recyclerView.visibility = View.VISIBLE
            binding.clearHistoryButton.visibility = View.VISIBLE
            binding.placeholderLayout.visibility = View.GONE
        }
    }

    private fun hideHistory() {
        isShowingHistory = false
        binding.recyclerView.adapter = gameAdapter
        binding.clearHistoryButton.visibility = View.GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("lastQuery", lastQuery)
    }
}
