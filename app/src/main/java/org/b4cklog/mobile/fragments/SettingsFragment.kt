package org.b4cklog.mobile.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import org.b4cklog.mobile.R
import org.b4cklog.mobile.activities.WelcomeActivity
import org.b4cklog.mobile.util.AuthPrefs
import androidx.core.content.edit
import androidx.navigation.Navigation
import org.b4cklog.mobile.network.ApiClient
import org.b4cklog.mobile.network.LogoutRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsFragment : Fragment() {

    private lateinit var themeSwitch: SwitchCompat
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        sharedPreferences = requireContext().getSharedPreferences("settings", 0)
        themeSwitch = view.findViewById(R.id.themeSwitch)

        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
        themeSwitch.isChecked = isDarkMode

        val accountSettingsButton = view.findViewById<View>(R.id.account)
        accountSettingsButton.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_accountSettingsFragment)
        }

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit { putBoolean("dark_mode", isChecked) }

            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )

            requireActivity().recreate()
        }

        val logoutButton = view.findViewById<View>(R.id.logout)
        logoutButton.setOnClickListener {
            val refreshToken = AuthPrefs.getRefreshToken(requireContext())
            val sessionId = AuthPrefs.getSessionId(requireContext())
            if (refreshToken != null && sessionId != null) {
                ApiClient.authApi.logout(LogoutRequest(refreshToken, sessionId)).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        AuthPrefs.clearTokens(requireContext())
                        val intent = Intent(requireContext(), WelcomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        AuthPrefs.clearTokens(requireContext())
                        val intent = Intent(requireContext(), WelcomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                })
            } else {
                AuthPrefs.clearTokens(requireContext())
                val intent = Intent(requireContext(), WelcomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        return view
    }
}
