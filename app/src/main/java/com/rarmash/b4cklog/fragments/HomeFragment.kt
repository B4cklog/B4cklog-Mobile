package com.rarmash.b4cklog.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.rarmash.b4cklog.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val forzaHorizon1 = view.findViewById<ImageView>(R.id.popularGame1)
        forzaHorizon1.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_gameDetailFragment)
        }

        return view
    }
}