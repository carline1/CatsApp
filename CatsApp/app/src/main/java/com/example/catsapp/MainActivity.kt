package com.example.catsapp

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.catsapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null
//    private val catImagesFragment = CatImagesFragment()
//    private val favouriteCatsFragment = FavouriteCatsFragment()
//    private val loadedCatsFragment = LoadedCatsFragment()
//    private var activeFragment: Fragment = catImagesFragment

    override fun onCreate(savedInstanceState: Bundle?) {super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        this.binding = binding

        setContentView(binding.root)

//        supportFragmentManager
//            .beginTransaction()
//            .add(R.id.nav_host_fragment, catImagesFragment, "1")
//            .commit()
//        supportFragmentManager
//            .beginTransaction()
//            .add(R.id.nav_host_fragment, favouriteCatsFragment, "2")
//            .hide(favouriteCatsFragment)
//            .commit()
//        supportFragmentManager
//            .beginTransaction()
//            .add(R.id.nav_host_fragment, loadedCatsFragment, "3")
//            .hide(loadedCatsFragment)
//            .commit()
//
//        val navigation = binding.bottomNavigationView
//        navigation.setOnItemSelectedListener { item ->
//            when(item.itemId) {
//                R.id.catImagesFragment -> {
//                    supportFragmentManager
//                        .beginTransaction()
//                        .hide(activeFragment)
//                        .show(catImagesFragment)
//                        .commit()
//                    activeFragment = catImagesFragment
//                }
//                R.id.favouriteCatsFragment -> {
//                    supportFragmentManager
//                        .beginTransaction()
//                        .hide(activeFragment)
//                        .show(favouriteCatsFragment)
//                        .commit()
//                    activeFragment = favouriteCatsFragment
//                }
//                R.id.loadedCatsFragment -> {
//                    supportFragmentManager
//                        .beginTransaction()
//                        .hide(activeFragment)
//                        .show(loadedCatsFragment)
//                        .commit()
//                    activeFragment = loadedCatsFragment
//                }
//            }
//            true
//        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = binding.bottomNavigationView
        NavigationUI.setupWithNavController(bottomNavigationView, navController)

        setSupportActionBar(binding.toolbar)
    }
}