package org.b4cklog.mobile.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.b4cklog.mobile.R
import org.b4cklog.mobile.models.User
import org.b4cklog.mobile.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.b4cklog.mobile.util.SessionManager

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)

        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        val navBar = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        navBar.setupWithNavController(navController)

        ApiClient.profileApi.getUserProfile().enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    user?.let {
                        SessionManager.userId = it.id  // Сохраняем
                    }
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                // Обработка ошибки
            }
        })
    }
}