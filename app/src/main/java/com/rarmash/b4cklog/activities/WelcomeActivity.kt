package com.rarmash.b4cklog.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.rarmash.b4cklog.R

class WelcomeActivity : AppCompatActivity() {
    lateinit var login_button: Button
    lateinit var signup_button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        login_button = findViewById(R.id.login_button)
        signup_button = findViewById(R.id.signup_button)

        login_button.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        signup_button.setOnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}