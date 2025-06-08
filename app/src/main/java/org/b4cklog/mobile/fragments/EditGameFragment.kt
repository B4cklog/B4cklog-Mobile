package org.b4cklog.mobile.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.b4cklog.mobile.R
import org.b4cklog.mobile.models.Game
import org.b4cklog.mobile.models.Platform
import org.b4cklog.mobile.models.User
import org.b4cklog.mobile.network.ApiClient
import org.b4cklog.mobile.util.AuthPrefs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditGameFragment : Fragment() {

    private lateinit var nameEditText: EditText
    private lateinit var summaryEditText: EditText
    private lateinit var coverEditText: EditText
    private lateinit var releaseDateEditText: EditText
    private lateinit var platformContainer: LinearLayout
    private lateinit var saveButton: Button

    private var gameId: Int? = null
    private var isAdmin = false
    private var allPlatforms: List<Platform> = listOf()
    private val selectedPlatforms = mutableSetOf<Platform>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_edit_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        nameEditText = view.findViewById(R.id.etGameName)
        summaryEditText = view.findViewById(R.id.etGameSummary)
        coverEditText = view.findViewById(R.id.etGameCover)
        releaseDateEditText = view.findViewById(R.id.etGameReleaseDate)
        platformContainer = view.findViewById(R.id.platforms_checkbox_container)
        saveButton = view.findViewById(R.id.btnSaveGame)

        gameId = arguments?.getInt("gameId")

        ApiClient.profileApi.getUserProfile().enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                val user = response.body()
                isAdmin = user?.isAdmin == true
                if (!isAdmin) {
                    Toast.makeText(context, "Доступ только для администраторов", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                    return
                }

                loadPlatforms {
                    gameId?.let { loadGame(it) }
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(context, "Ошибка сети", Toast.LENGTH_SHORT).show()
            }
        })

        saveButton.setOnClickListener {
            if (validateInput()) {
                if (gameId != null) {
                    updateGame()
                } else {
                    createGame()
                }
            }
        }
    }

    private fun loadPlatforms(onLoaded: () -> Unit) {
        ApiClient.platformApi.getAllPlatforms().enqueue(object : Callback<List<Platform>> {
            override fun onResponse(call: Call<List<Platform>>, response: Response<List<Platform>>) {
                if (response.isSuccessful) {
                    allPlatforms = response.body() ?: emptyList()
                    platformContainer.removeAllViews()
                    for (platform in allPlatforms) {
                        val checkbox = CheckBox(requireContext()).apply {
                            text = "${platform.name} (${platform.releaseDate})"
                            setOnCheckedChangeListener { _, isChecked ->
                                if (isChecked) selectedPlatforms.add(platform)
                                else selectedPlatforms.remove(platform)
                            }
                        }
                        platformContainer.addView(checkbox)
                    }
                    onLoaded()
                }
            }

            override fun onFailure(call: Call<List<Platform>>, t: Throwable) {
                Toast.makeText(context, "Ошибка загрузки платформ", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadGame(id: Int) {
        ApiClient.gameApi.getGame(id).enqueue(object : Callback<Game> {
            override fun onResponse(call: Call<Game>, response: Response<Game>) {
                val game = response.body() ?: return
                nameEditText.setText(game.name)
                summaryEditText.setText(game.summary)
                coverEditText.setText(game.cover)
                releaseDateEditText.setText(game.releaseDate)

                // Отметим выбранные платформы
                selectedPlatforms.addAll(game.platforms)
                for (i in 0 until platformContainer.childCount) {
                    val checkbox = platformContainer.getChildAt(i) as CheckBox
                    val platform = allPlatforms.getOrNull(i)
                    if (platform != null && game.platforms.any { it.id == platform.id }) {
                        checkbox.isChecked = true
                    }
                }
            }

            override fun onFailure(call: Call<Game>, t: Throwable) {
                Toast.makeText(context, "Ошибка загрузки игры", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun validateInput(): Boolean {
        if (nameEditText.text.isBlank() || summaryEditText.text.isBlank() ||
            coverEditText.text.isBlank() || releaseDateEditText.text.isBlank() ||
            selectedPlatforms.isEmpty()) {
            Toast.makeText(context, "Заполните все поля и выберите платформы", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun createGame() {
        val token = AuthPrefs.getToken(requireContext()) ?: return

        val game = Game(
            id = 0,
            name = nameEditText.text.toString(),
            summary = summaryEditText.text.toString(),
            cover = coverEditText.text.toString(),
            releaseDate = releaseDateEditText.text.toString(),
            platforms = selectedPlatforms.toMutableList()
        )

        ApiClient.gameApi.addGame(game).enqueue(object : Callback<Game> {
            override fun onResponse(call: Call<Game>, response: Response<Game>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Игра добавлена", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(context, "Ошибка: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Game>, t: Throwable) {
                Toast.makeText(context, "Сеть: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateGame() {
        val token = AuthPrefs.getToken(requireContext()) ?: return

        val game = Game(
            id = gameId!!,
            name = nameEditText.text.toString(),
            summary = summaryEditText.text.toString(),
            cover = coverEditText.text.toString(),
            releaseDate = releaseDateEditText.text.toString(),
            platforms = selectedPlatforms.toMutableList()
        )

        ApiClient.gameApi.updateGame(game).enqueue(object : Callback<Game> {
            override fun onResponse(call: Call<Game>, response: Response<Game>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Игра обновлена", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(context, "Ошибка при обновлении", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Game>, t: Throwable) {
                Toast.makeText(context, "Ошибка сети", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
