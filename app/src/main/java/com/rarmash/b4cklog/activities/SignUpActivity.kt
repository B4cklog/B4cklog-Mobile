package com.rarmash.b4cklog.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rarmash.b4cklog.R
import com.rarmash.b4cklog.network.ApiClient
import com.rarmash.b4cklog.network.AuthResponse
import com.rarmash.b4cklog.network.RegisterRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {
    lateinit var signup_button: Button
    lateinit var etLogin: EditText
    lateinit var etPassword: EditText
    lateinit var etEmail: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        signup_button = findViewById(R.id.signup_button)
        etLogin = findViewById(R.id.editTextUsername)
        etPassword = findViewById(R.id.editTextPassword)
        etEmail = findViewById(R.id.editTextEmail)

        signup_button.setOnClickListener {
            val login = etLogin.text.toString()
            val password = etPassword.text.toString()
            val email = etEmail.text.toString()
            if (login.isNotEmpty() && password.isNotEmpty() && email.isNotEmpty()) {
                registerUser(login, password, email)
            } else {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(login: String, password: String, email: String) {
        val call = ApiClient.authApi.register(RegisterRequest(login, password, email))
        call.enqueue(object: Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@SignUpActivity, "Ошибка регистрации", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(this@SignUpActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.d("SignUpActivity", "ERROR: ${t.message.toString()}")
            }
        })
    }
}