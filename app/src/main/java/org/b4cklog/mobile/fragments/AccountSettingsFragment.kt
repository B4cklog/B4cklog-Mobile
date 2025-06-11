package org.b4cklog.mobile.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import org.b4cklog.mobile.R
import org.b4cklog.mobile.network.ApiClient
import kotlinx.coroutines.launch
import org.b4cklog.mobile.models.User
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
                Toast.makeText(context, getString(R.string.enter_new_email), Toast.LENGTH_SHORT).show()
            }
        }

        updatePasswordButton.setOnClickListener {
            val newPassword = passwordEditText.text.toString()
            if (newPassword.isNotBlank()) {
                updatePassword(newPassword)
            } else {
                Toast.makeText(context, getString(R.string.enter_new_password), Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(context, getString(R.string.getting_data_error), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(context, "${getString(R.string.network_error)}: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateEmail(newEmail: String) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.profileApi.updateEmail(newEmail)
                if (response.isSuccessful) {
                    Toast.makeText(context, getString(R.string.email_updated), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "${getString(R.string.update_error)}: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "${getString(R.string.network_error)}: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePassword(newPassword: String) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.profileApi.updatePassword(newPassword)
                if (response.isSuccessful) {
                    Toast.makeText(context, getString(R.string.password_updated), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "${getString(R.string.update_error)}: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "${getString(R.string.network_error)}: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
