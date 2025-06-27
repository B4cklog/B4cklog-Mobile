package org.b4cklog.mobile.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import org.b4cklog.mobile.R
import org.b4cklog.mobile.models.Game
import org.b4cklog.mobile.models.ReviewResponse
import org.b4cklog.mobile.models.User
import org.b4cklog.mobile.network.ApiClient
import org.b4cklog.mobile.util.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.b4cklog.mobile.models.UserProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.b4cklog.mobile.adapters.ScreenshotAdapter
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.viewpager2.widget.ViewPager2
import org.b4cklog.mobile.adapters.ScreenshotPagerAdapter
import org.b4cklog.mobile.models.Screenshot

class GameDetailFragment : Fragment() {
    private var currentListName: String? = null
    private var currentUserId: Int? = null
    private var gameId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener("review_added", viewLifecycleOwner) { _, _ ->
            reloadFragment()
        }

        gameId = arguments?.getInt("gameId") ?: return

        loadGameDetails(gameId)
        loadGameRating(gameId)
        loadUserReview(gameId)

        val addToBacklogButton = view.findViewById<Button>(R.id.add_to_backlog_button)
        val removeFromBacklogButton = view.findViewById<ImageButton>(R.id.remove_from_backlog_button)
        val listTextView = view.findViewById<TextView>(R.id.game_list_info)

        addToBacklogButton.setOnClickListener {
            showAddToListDialog(gameId)
        }

        val reviewButton = view.findViewById<Button>(R.id.write_review_button)
        reviewButton.setOnClickListener {
            ReviewDialogFragment(gameId){
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(50)
                    loadGameRating(gameId)
                    loadUserReview(gameId)
                }
            }.show(childFragmentManager, "ReviewDialog")
        }

        loadCurrentUserAndCheckList(gameId, removeFromBacklogButton, listTextView)

        val screenshotsRecycler = view.findViewById<RecyclerView>(R.id.game_screenshots_recycler)
        screenshotsRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun loadCurrentUserAndCheckList(gameId: Int, removeButton: ImageButton, listText: TextView) {
        ApiClient.profileApi.getUserProfileWithGames().enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (!response.isSuccessful) return
                val profileResponse = response.body() ?: return
                val user = profileResponse.user
                val games = profileResponse.games
                currentUserId = user.id

                currentListName = when {
                    games["want_to_play"]?.any { it.id == gameId } == true -> getString(R.string.want_to_play)
                    games["playing"]?.any { it.id == gameId } == true -> getString(R.string.playing)
                    games["played"]?.any { it.id == gameId } == true -> getString(R.string.played)
                    games["completed"]?.any { it.id == gameId } == true -> getString(R.string.completed)
                    games["completed_100"]?.any { it.id == gameId } == true -> getString(R.string.completed_100)
                    else -> null
                }

                currentListName?.let { list ->
                    listText.text = "${getString(R.string.in_a_list)}: $list"
                    listText.visibility = View.VISIBLE
                    removeButton.visibility = View.VISIBLE
                    removeButton.setOnClickListener {
                        currentUserId?.let { uid ->
                            ApiClient.profileApi.removeGameFromAllLists(uid, gameId)
                                .enqueue(object : Callback<Void> {
                                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                        if (response.isSuccessful) {
                                            Toast.makeText(requireContext(), getString(R.string.removed_from_all_lists), Toast.LENGTH_SHORT).show()
                                            removeButton.visibility = View.GONE
                                            listText.visibility = View.GONE
                                        } else {
                                            Toast.makeText(requireContext(), getString(R.string.deleting_error), Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                    override fun onFailure(call: Call<Void>, t: Throwable) {
                                        Toast.makeText(requireContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show()
                                    }
                                })
                        }
                    }
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Toast.makeText(context, getString(R.string.getting_data_error), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadGameDetails(id: Int) {
        ApiClient.gameApi.getGame(id).enqueue(object : Callback<Game> {
            override fun onResponse(call: Call<Game>, response: Response<Game>) {
                if (response.isSuccessful) {
                    val game = response.body() ?: return

                    view?.findViewById<TextView>(R.id.game_name)?.text = game.name
                    view?.findViewById<TextView>(R.id.game_summary)?.text = game.summary
                    view?.findViewById<TextView>(R.id.game_release_date)?.text = "${getString(R.string.release_date)}: ${game.getReleaseDate()}"
                    view?.findViewById<TextView>(R.id.game_platforms)?.text =
                        "${getString(R.string.platforms)}: ${game.platforms?.joinToString(", ") { it.name } ?: "—"}"
                    view?.findViewById<TextView>(R.id.game_genres)?.text =
                        "${getString(R.string.genres)}: " + (game.genres?.joinToString(", ") { it.name } ?: "—")

                    val coverView = view?.findViewById<ImageView>(R.id.game_cover)
                    Glide.with(requireContext())
                        .load(game.getCoverUrl())
                        .placeholder(R.drawable.cover_placeholder)
                        .error(R.drawable.cover_placeholder)
                        .into(coverView!!)

                    val screenshotsRecycler = view?.findViewById<RecyclerView>(R.id.game_screenshots_recycler)
                    if (game.screenshots != null && game.screenshots.isNotEmpty()) {
                        val adapter = ScreenshotAdapter(game.screenshots) { screenshot ->
                            showScreenshotDialog(game.screenshots, game.screenshots.indexOf(screenshot))
                        }
                        screenshotsRecycler?.adapter = adapter
                        screenshotsRecycler?.visibility = View.VISIBLE
                        view?.findViewById<TextView>(R.id.game_screenshots_label)?.visibility = View.VISIBLE
                    } else {
                        screenshotsRecycler?.visibility = View.GONE
                        view?.findViewById<TextView>(R.id.game_screenshots_label)?.visibility = View.GONE
                    }
                }
            }

            override fun onFailure(call: Call<Game>, t: Throwable) {
                Toast.makeText(context, getString(R.string.getting_data_error), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadGameRating(gameId: Int) {
        ApiClient.reviewApi.getAverageRating(gameId).enqueue(object : Callback<Double> {
            override fun onResponse(call: Call<Double>, response: Response<Double>) {
                if (response.isSuccessful) {
                    val rating = response.body() ?: 0.0
                    view?.findViewById<TextView>(R.id.game_average_rating)?.text =
                        "${getString(R.string.average_rating)}: %.1f".format(rating)
                }
            }

            override fun onFailure(call: Call<Double>, t: Throwable) {
                Toast.makeText(context, getString(R.string.getting_data_error), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadUserReview(gameId: Int) {
        val userId = SessionManager.userId ?: return

        ApiClient.reviewApi.getUserReview(userId, gameId)
            .enqueue(object : Callback<ReviewResponse> {
                override fun onResponse(call: Call<ReviewResponse>, response: Response<ReviewResponse>) {
                    if (response.isSuccessful) {
                        val review = response.body() ?: return
                        val reviewTextView = view?.findViewById<TextView>(R.id.user_review_text)
                        reviewTextView?.visibility = View.VISIBLE
                        reviewTextView?.text = "${getString(R.string.your_rating)}: ${review.rating}/5\n${getString(R.string.review)}: ${review.comment ?: "—"}"
                    }
                }

                override fun onFailure(call: Call<ReviewResponse>, t: Throwable) {
                    // Nothing to do
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
            .setTitle(getString(R.string.add_to_backlog))
            .setItems(listNames) { _, which ->
                val listKey = listKeys[which]
                addGameToBacklog(gameId, listKey)
            }
            .setNegativeButton(getString(R.string.cancel), null)
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
                                    Toast.makeText(requireContext(), getString(R.string.added_to_list), Toast.LENGTH_SHORT).show()
                                    view?.post {
                                        val removeFromBacklogButton = view?.findViewById<ImageButton>(R.id.remove_from_backlog_button)
                                        val listTextView = view?.findViewById<TextView>(R.id.game_list_info)
                                        if (removeFromBacklogButton != null && listTextView != null) {
                                            loadCurrentUserAndCheckList(gameId, removeFromBacklogButton, listTextView)
                                        }
                                    }
                                } else {
                                    Toast.makeText(requireContext(), getString(R.string.addition_error), Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Toast.makeText(requireContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show()
                            }
                        })
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(requireContext(), getString(R.string.getting_data_error), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun reloadFragment() {
        val args = bundleOf("gameId" to gameId)
        findNavController().popBackStack(R.id.gameDetailFragment, true)
        findNavController().navigate(R.id.gameDetailFragment, args)
    }

    private fun showScreenshotDialog(screenshots: List<Screenshot>, startIndex: Int) {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_fullscreen_image)
        val window = dialog.window
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        
        val viewPager = dialog.findViewById<ViewPager2>(R.id.fullscreen_viewpager)
        
        // Add touch listener to dialog root to handle pinch-to-zoom
        val dialogRoot = dialog.findViewById<View>(android.R.id.content)
        dialogRoot.setOnTouchListener { v, event ->
            // If we have multiple pointers (pinch gesture), disable ViewPager2
            if (event.pointerCount > 1) {
                viewPager.isUserInputEnabled = false
                // Re-enable after pinch completes
                viewPager.postDelayed({
                    viewPager.isUserInputEnabled = true
                }, 1000)
            }
            false
        }
        
        viewPager.isUserInputEnabled = true // Enable native ViewPager2 swipe
        val adapter = ScreenshotPagerAdapter(screenshots, { dialog.dismiss() }, viewPager)
        viewPager.adapter = adapter
        viewPager.setCurrentItem(startIndex, false)
        
        val indicator = dialog.findViewById<TextView>(R.id.fullscreen_indicator)
        indicator.text = "${startIndex + 1}/${screenshots.size}"
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                indicator.text = "${position + 1}/${screenshots.size}"
            }
            
            override fun onPageScrollStateChanged(state: Int) {
                // Reset any dragged state when scrolling between pages
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    // Find current PhotoView and reset its state
                    val currentItem = viewPager.getChildAt(0)
                    if (currentItem is androidx.recyclerview.widget.RecyclerView) {
                        val currentViewHolder = currentItem.findViewHolderForAdapterPosition(viewPager.currentItem)
                        if (currentViewHolder is ScreenshotPagerAdapter.ViewHolder) {
                            currentViewHolder.imageView.translationY = 0f
                            currentViewHolder.imageView.translationX = 0f
                            currentViewHolder.imageView.alpha = 1f
                        }
                    }
                }
            }
        })
        dialog.show()
    }
}
