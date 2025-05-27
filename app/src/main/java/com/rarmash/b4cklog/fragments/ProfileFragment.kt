package com.rarmash.b4cklog.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rarmash.b4cklog.R
import com.rarmash.b4cklog.models.User
import com.rarmash.b4cklog.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {
    private lateinit var userName: TextView
    private lateinit var userFullNameAge: TextView
    private lateinit var userEmail: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        userName = view.findViewById(R.id.userName)
        userFullNameAge = view.findViewById(R.id.userFullNameAge)
        userEmail = view.findViewById(R.id.userEmail)

        val forzaHorizon1 = view.findViewById<ImageView>(R.id.plannedGame1)
        forzaHorizon1.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_gameDetailFragment)
        }

        val settingsButton = view.findViewById<FloatingActionButton>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_settingsFragment)
        }

        loadUserProfile()

        return view
    }

    private fun loadUserProfile() {
        ApiClient.profileApi.getUserProfile().enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        userName.text = user.username
                        userFullNameAge.text = "${user.firstName} ${user.lastName}, ${user.age}"
                        userEmail.text = user.email
                    }
                } else {
                    // Можно показать ошибку или тост
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                // Тут обработка ошибки сети
            }
        })
    }
}