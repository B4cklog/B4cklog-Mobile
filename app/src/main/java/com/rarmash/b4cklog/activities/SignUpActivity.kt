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
    private lateinit var signupButton: Button
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etEmail: EditText
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etAge: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        signupButton = findViewById(R.id.signup_button)
        etUsername = findViewById(R.id.editTextUsername)
        etPassword = findViewById(R.id.editTextPassword)
        etEmail = findViewById(R.id.editTextEmail)
        etFirstName = findViewById(R.id.editTextFirstName)
        etLastName = findViewById(R.id.editTextLastName)
        etAge = findViewById(R.id.editTextAge)

        signupButton.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            val email = etEmail.text.toString()
            val firstName = etFirstName.text.toString()
            val lastName = etLastName.text.toString()
            val age = etAge.text.toString().toInt()

            if (
                username.isNotEmpty()
                && password.isNotEmpty()
                && email.isNotEmpty()
                && firstName.isNotEmpty()
                && lastName.isNotEmpty()) {
                registerUser(username, password, email, firstName, lastName, age)
            } else {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(username: String, password: String, email: String, firstName: String, lastName: String, age: Int) {
        val call = ApiClient.authApi.register(RegisterRequest(username, email, password, firstName, lastName, age))
        call.enqueue(object: Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                    startActivity(intent)
                } else {
                    //TODO: Добавить больше вариантов ошибок
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