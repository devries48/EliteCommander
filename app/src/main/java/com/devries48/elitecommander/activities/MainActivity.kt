package com.devries48.elitecommander.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.devries48.elitecommander.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.commanderFragment) {
                if (!isUserLoggedIn) {
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivityForResult(intent, FRONTIER_LOGIN_REQUEST_CODE)
                } else {
                    navController.navigate(R.id.commanderFragment)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FRONTIER_LOGIN_REQUEST_CODE) {
            isUserLoggedIn = true
        }
    }

    companion object {
        private const val FRONTIER_LOGIN_REQUEST_CODE = 999
        var isUserLoggedIn = false

    }

}

