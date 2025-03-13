package com.rarmash.b4cklog.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rarmash.b4cklog.R

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val forzaHorizon1 = view.findViewById<ImageView>(R.id.plannedGame1)
        forzaHorizon1.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_gameDetailFragment)
        }

        val settingsButton = view.findViewById<FloatingActionButton>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_settingsFragment)
        }

        return view
    }
}