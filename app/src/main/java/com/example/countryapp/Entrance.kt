package com.example.countryapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.Navigation



class Entrance : Fragment() {
    private lateinit var navController: NavController
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_entrance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        initFirstLaunch()
        initButton(view)

    }

    private fun initFirstLaunch(){
        sharedPreferences = requireContext().getSharedPreferences("entrance", Context.MODE_PRIVATE)
        val isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true)

        if (isFirstLaunch) {
            sharedPreferences.edit().putBoolean("isFirstLaunch", false).apply()
        }

        if (isFirstLaunch) {
            showFirstFrame()
        } else {
            navController.navigate(R.id.action_entrance_to_chooseCountry)
        }
    }
    private fun initButton(view: View) {
        val btn: Button = view.findViewById(R.id.toChoose)
        btn.setOnClickListener {
            navController.navigate(R.id.action_entrance_to_chooseCountry)
        }
    }

    private fun FragmentManager.showFragment(fragment: Fragment, containerViewId: Int) {
        beginTransaction().replace(containerViewId, fragment).commit()
    }

    private fun showFirstFrame() {
        parentFragmentManager.showFragment(this, R.id.fragmentContainerView)
    }

}


