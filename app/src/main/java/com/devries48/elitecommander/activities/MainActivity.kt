package com.devries48.elitecommander.activities

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.devries48.elitecommander.R
import com.devries48.elitecommander.databinding.ActivityMainBinding
import com.devries48.elitecommander.events.FrontierAuthNeededEvent
import com.devries48.elitecommander.fragments.CommanderViewModel
import com.devries48.elitecommander.fragments.CommanderViewModelFactory
import com.devries48.elitecommander.network.CommanderNetwork
import com.devries48.elitecommander.utils.OAuthUtils.storeUpdatedTokens
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.math.abs
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private val navController by lazy { findNavController() }

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mCommanderNetwork: CommanderNetwork
    private var mCommanderViewModel: CommanderViewModel? = null

    private var mIsLoggedIn by Delegates.observable(false) { _, _, newValue ->
        if (newValue) loadData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDataBinding()
        setupViewModel()
        setupNavigation()

        detector = GestureDetectorCompat(this, NavigationGestureListener())
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FRONTIER_LOGIN_REQUEST_CODE) {
            mIsLoggedIn = true
            doNavigate(navDestinationId)
        }
    }

    private fun setupDataBinding() {
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mBinding.root
        setContentView(view)
    }

    private fun setupNavigation() {
        val navController = findNavController()

        //TODO: check connection (refresh?) and set IsUserLoggedIn

        navController.addOnDestinationChangedListener { _, destination, _ ->
            navDestinationId = destination.id

            if (!mIsLoggedIn) {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivityForResult(intent, FRONTIER_LOGIN_REQUEST_CODE)
            } else {
                if (navDestinationId == R.id.mainFragment) {
                    navDestinationId =  R.id.discoveriesFragment // R.id.action_main_to_commander
                    navController.navigate(navDestinationId)
                }
            }
        }
    }

    private fun setupViewModel() {
        try {
            mCommanderNetwork = CommanderNetwork()

            val viewModelProvider = ViewModelProvider(
                navController.getViewModelStoreOwner(R.id.nav_graph),
                CommanderViewModelFactory(mCommanderNetwork)
            )
            mCommanderViewModel = viewModelProvider.get(CommanderViewModel::class.java)

        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private fun loadData(){
        mCommanderNetwork.loadProfile()
        // TODO: iterate through journals from today to the journal's last 'Docked' event.

        mCommanderNetwork.loadJournal()
    }

    private fun findNavController(): NavController {
        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFrontierAuthNeededEvent(frontierAuthNeededEvent: FrontierAuthNeededEvent) {
        mIsLoggedIn = false
        storeUpdatedTokens(this, "", "")
    }

    private fun onSwipeBottomToTop() {
        when (navDestinationId) {
            R.id.action_main_to_commander, R.id.commanderFragment -> doNavigate(R.id.action_commander_to_discoveries)
        }
    }

    internal fun onSwipeRightToLeft() {
        Toast.makeText(this, "Right to left Swipe", Toast.LENGTH_LONG).show()
    }

    internal fun onSwipeLeftToRight() {
        Toast.makeText(this, "Left to right Swipe", Toast.LENGTH_LONG).show()
    }

    private fun onSwipeTopToBottom() {
        if (navDestinationId == R.id.discoveriesFragment)
            doNavigate(R.id.action_discoveries_to_commander)
    }

    private fun doNavigate(destinationId: Int) {
        navDestinationId = destinationId
        val navController = findNavController()
        navController.navigate(destinationId)
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
                        this@MainActivity.onSwipeLeftToRight()
                    } else {
                        this@MainActivity.onSwipeRightToLeft()
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

    companion object {
        private const val FRONTIER_LOGIN_REQUEST_CODE = 999
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100

        private lateinit var detector: GestureDetectorCompat

        private var navDestinationId: Int = 0
    }

}

