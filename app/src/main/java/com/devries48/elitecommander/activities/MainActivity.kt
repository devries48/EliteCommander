package com.devries48.elitecommander.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.devries48.elitecommander.R

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            navDestinationId = destination.id

            if (!isUserLoggedIn) {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivityForResult(intent, FRONTIER_LOGIN_REQUEST_CODE)
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FRONTIER_LOGIN_REQUEST_CODE) {
            isUserLoggedIn = true
            if (navDestinationId == R.id.mainFragment) {
                navDestinationId = R.id.action_mainFragment_to_commanderFragment
            }
            navController.navigate(navDestinationId)
        }
    }

    companion object {
        private const val FRONTIER_LOGIN_REQUEST_CODE = 999
        var isUserLoggedIn = false
        private var navDestinationId: Int = 0
        private lateinit var navController: NavController
        private lateinit var navHostFragment: NavHostFragment

    }

}

