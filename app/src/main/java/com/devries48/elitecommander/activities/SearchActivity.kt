package com.devries48.elitecommander.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.devries48.elitecommander.R

class SearchActivity : AppCompatActivity() {

    private var mCurrentDestinationId: Int = 0
    private val mNavController by lazy { findNavController() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setContentView(R.layout.activity_search)
        setupNavigation()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    private fun setupNavigation() {
        val navController = findNavController()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.searchFragment && mCurrentDestinationId != destination.id) {
                mCurrentDestinationId = destination.id
                navController.navigate(destination.id)
            }
        }
    }

    private fun findNavController(): NavController {
        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.searchNavHost) as NavHostFragment
        return navHostFragment.navController
    }
}