package com.rarmash.b4cklog.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.rarmash.b4cklog.databinding.DialogReviewBinding
import com.rarmash.b4cklog.models.ReviewRequest
import com.rarmash.b4cklog.network.ApiClient
import com.rarmash.b4cklog.util.SessionManager
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
            .setTitle("Оставить отзыв")
            .setView(binding.root)
            .setPositiveButton("Отправить") { _, _ ->
                val rating = binding.ratingBar.rating.toInt()
                val comment = binding.reviewText.text.toString()

                val userId = SessionManager.userId
                if (userId == null) {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Пользователь не авторизован", Toast.LENGTH_SHORT).show()
                    }
                    return@setPositiveButton
                }

                val review = ReviewRequest(userId, gameId, rating, comment)

                ApiClient.reviewApi.submitReview(review).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (!isAdded) return
                        // всегда показываем тост
                        Toast.makeText(
                            requireContext(),
                            "Отзыв отправлен",
                            Toast.LENGTH_LONG
                        ).show()
                        // отдаем сигнал старшему фрагменту
                        parentFragmentManager.setFragmentResult("review_added", Bundle())
                        onReviewSubmitted?.invoke()
                        dismiss()
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        if (!isAdded) return
                        Toast.makeText(requireContext(), "Ошибка сети при отправке: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Отмена", null)
            .create()
    }
}
