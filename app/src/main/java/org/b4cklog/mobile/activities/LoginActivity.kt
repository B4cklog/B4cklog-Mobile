package org.b4cklog.mobile.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.b4cklog.mobile.R
import org.b4cklog.mobile.network.ApiClient
import org.b4cklog.mobile.network.AuthResponse
import org.b4cklog.mobile.network.LoginRequest
import org.b4cklog.mobile.util.AuthPrefs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var loginButton: Button
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginButton = findViewById(R.id.login_button)
        etUsername = findViewById(R.id.editTextUsername)
        etPassword = findViewById(R.id.editTextPassword)

        loginButton.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                loginUser(username, password)
            } else {
                Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(username: String, password: String) {
        val call = ApiClient.authApi.login(LoginRequest(username, password))
        call.enqueue(object: Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token
                    AuthPrefs.saveToken(this@LoginActivity, token)
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@LoginActivity, getString(R.string.login_error), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "${getString(R.string.network_error)}: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}