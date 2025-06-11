package org.b4cklog.mobile.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import org.b4cklog.mobile.R
import org.b4cklog.mobile.databinding.DialogReviewBinding
import org.b4cklog.mobile.models.ReviewRequest
import org.b4cklog.mobile.network.ApiClient
import org.b4cklog.mobile.util.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReviewDialogFragment(
    private val gameId: Int,
    private val onReviewSubmitted: (() -> Unit)? = null
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogReviewBinding.inflate(LayoutInflater.from(context))

        return AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.leave_review))
            .setView(binding.root)
            .setPositiveButton(getString(R.string.send)) { _, _ ->
                val rating = binding.ratingBar.rating.toInt()
                val comment = binding.reviewText.text.toString()

                val userId = SessionManager.userId
                if (userId == null) {
                    if (isAdded) {
                        Toast.makeText(requireContext(), getString(R.string.user_is_not_authorized), Toast.LENGTH_SHORT).show()
                    }
                    return@setPositiveButton
                }

                val review = ReviewRequest(userId, gameId, rating, comment)

                ApiClient.reviewApi.submitReview(review).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (!isAdded) return
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.review_sent),
                            Toast.LENGTH_LONG
                        ).show()

                        parentFragmentManager.setFragmentResult("review_added", Bundle())
                        onReviewSubmitted?.invoke()
                        dismiss()
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        if (!isAdded) return
                        Toast.makeText(requireContext(), "${getString(R.string.network_error)}: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
    }
}
