package com.devries48.elitecommander.activities

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.devries48.elitecommander.R
import com.devries48.elitecommander.events.FrontierAuthNeededEvent
import com.devries48.elitecommander.utils.OAuthUtils.storeUpdatedTokens
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.abs

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
                if (navDestinationId == R.id.mainFragment){
                    navDestinationId = R.id.action_main_to_commander
                    navController.navigate(navDestinationId)
                }
            }
        }

        detector = GestureDetectorCompat(this, NavigationGestureListener())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FRONTIER_LOGIN_REQUEST_CODE) {
            isUserLoggedIn = true
            doNavigate(navDestinationId)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (detector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
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

    inner class NavigationGestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onFling(
            downEvent: MotionEvent?,
            moveEvent: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val diffX = moveEvent?.x?.minus(downEvent!!.x) ?: 0.0F
            val diffY = moveEvent?.y?.minus(downEvent!!.y) ?: 0.0F

            return if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        this@MainActivity.onSwipeRight()
                    } else {
                        this@MainActivity.onLeftSwipe()
                    }
                    true
                } else {
                    super.onFling(downEvent, moveEvent, velocityX, velocityY)
                }
            } else {
                if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        this@MainActivity.onSwipeTopToBottom()
                    } else {
                        this@MainActivity.onSwipeBottomToTop()
                    }
                    true
                } else {
                    super.onFling(downEvent, moveEvent, velocityX, velocityY)
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFrontierAuthNeededEvent(frontierAuthNeededEvent: FrontierAuthNeededEvent) {
        isUserLoggedIn = false
        storeUpdatedTokens(this, "", "")
    }

    private fun onSwipeTopToBottom() {
        if (navDestinationId == R.id.discoveriesFragment)
            doNavigate(R.id.action_discoveries_to_commander)
    }

    private fun onSwipeBottomToTop() {
        when (navDestinationId) {
            R.id.action_main_to_commander, R.id.commanderFragment -> doNavigate(R.id.action_commander_to_discoveries)
        }
    }

    internal fun onLeftSwipe() {
        Toast.makeText(this, "Left to right Swipe", Toast.LENGTH_LONG).show()
    }

    internal fun onSwipeRight() {
        Toast.makeText(this, "Right to left Swipe", Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val FRONTIER_LOGIN_REQUEST_CODE = 999
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100

        private lateinit var navController: NavController
        private lateinit var navHostFragment: NavHostFragment
        private lateinit var detector: GestureDetectorCompat

        var isUserLoggedIn = true
        private var navDestinationId: Int = 0


        private fun doNavigate(destinationId: Int) {
            navDestinationId = destinationId
            navController.navigate(destinationId)
        }

    }

}

