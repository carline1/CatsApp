package com.example.catsapp.ui.common

import android.app.Activity
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import com.example.catsapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView

object FullScreenStateChanger {
    fun fullScreen(activity: Activity, state: Boolean) {
        val navHostFragment =
            (activity as AppCompatActivity).findViewById<FragmentContainerView>(R.id.nav_host_fragment)
        val marginLayoutParams = navHostFragment.layoutParams as ViewGroup.MarginLayoutParams
        val marginBottom: Int
        val visibility: Int
        when (state) {
            true -> {
                marginBottom = 0
                visibility = View.GONE
                activity.supportActionBar?.hide()
            }
            false -> {
                val typeValue = TypedValue()
                activity.applicationContext.theme.resolveAttribute(
                    android.R.attr.actionBarSize,
                    typeValue,
                    true
                )
                marginBottom =
                    activity.applicationContext.resources.getDimensionPixelSize(typeValue.resourceId)
                visibility = View.VISIBLE
                activity.supportActionBar?.show()
            }
        }
        marginLayoutParams.setMargins(0, 0, 0, marginBottom)
        navHostFragment.requestLayout()
        val bottomNavView =
            (activity).findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNavView.visibility = visibility
    }
}