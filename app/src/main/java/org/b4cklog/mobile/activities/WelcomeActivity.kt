package org.b4cklog.mobile.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import org.b4cklog.mobile.R
import org.b4cklog.mobile.util.AuthPrefs

class WelcomeActivity : AppCompatActivity() {
    lateinit var loginButton: Button
    lateinit var signUpButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 31) {
            DynamicColors.applyToActivitiesIfAvailable(application)
        }

        val sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)

        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        val accessToken = AuthPrefs.getAccessToken(this)
        val refreshToken = AuthPrefs.getRefreshToken(this)
        if (!accessToken.isNullOrEmpty() && !refreshToken.isNullOrEmpty()) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_welcome)

        loginButton = findViewById(R.id.login_button)
        signUpButton = findViewById(R.id.signup_button)

        loginButton.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        signUpButton.setOnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}