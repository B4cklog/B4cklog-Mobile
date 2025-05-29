package com.rarmash.b4cklog.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.rarmash.b4cklog.R
import com.rarmash.b4cklog.network.ApiClient
import kotlinx.coroutines.launch
import com.rarmash.b4cklog.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountSettingsFragment : Fragment() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var updateEmailButton: Button
    private lateinit var updatePasswordButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_account_settings, container, false)

        emailEditText = view.findViewById(R.id.editTextEmail)
        passwordEditText = view.findViewById(R.id.editTextPassword)
        updateEmailButton = view.findViewById(R.id.buttonUpdateEmail)
        updatePasswordButton = view.findViewById(R.id.buttonUpdatePassword)

        loadUserProfile()

        updateEmailButton.setOnClickListener {
            val newEmail = emailEditText.text.toString()
            if (newEmail.isNotBlank()) {
                updateEmail(newEmail)
            } else {
                Toast.makeText(context, "Введите новый email", Toast.LENGTH_SHORT).show()
            }
        }

        updatePasswordButton.setOnClickListener {
            val newPassword = passwordEditText.text.toString()
            if (newPassword.isNotBlank()) {
                updatePassword(newPassword)
            } else {
                Toast.makeText(context, "Введите новый пароль", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun loadUserProfile() {
        ApiClient.profileApi.getUserProfile().enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    emailEditText.setText(user?.email ?: "")
                } else {
                    Toast.makeText(context, "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(context, "Ошибка сети: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateEmail(newEmail: String) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.profileApi.updateEmail(newEmail)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Email обновлён", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Ошибка обновления: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Сетевая ошибка: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePassword(newPassword: String) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.profileApi.updatePassword(newPassword)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Пароль обновлён", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Ошибка обновления: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Сетевая ошибка: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
