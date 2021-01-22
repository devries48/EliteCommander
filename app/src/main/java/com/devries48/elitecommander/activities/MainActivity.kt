package com.devries48.elitecommander.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.devries48.elitecommander.R
import com.devries48.elitecommander.events.FrontierAuthNeededEvent
import com.devries48.elitecommander.utils.OAuthUtils.storeUpdatedTokens
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


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
            } else {
                doNavigate()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FRONTIER_LOGIN_REQUEST_CODE) {
            isUserLoggedIn = true
            doNavigate()
        }
    }

    public override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    public override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFrontierAuthNeededEvent(frontierAuthNeededEvent: FrontierAuthNeededEvent) {
        isUserLoggedIn=false
        storeUpdatedTokens(this, "", "")
    }

    private fun doNavigate() {
        if (navDestinationId == R.id.mainFragment) {
            navDestinationId = R.id.action_mainFragment_to_commanderFragment
        }
        navController.navigate(navDestinationId)
    }

    companion object {
        private const val FRONTIER_LOGIN_REQUEST_CODE = 999
        var isUserLoggedIn = true
        private var navDestinationId: Int = 0
        private lateinit var navController: NavController
        private lateinit var navHostFragment: NavHostFragment
    }

}

