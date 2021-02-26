package com.devries48.elitecommander.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.devries48.elitecommander.R
import com.devries48.elitecommander.databinding.ActivityMainBinding
import com.devries48.elitecommander.events.FrontierAuthNeededEvent
import com.devries48.elitecommander.fragments.CommanderViewModel
import com.devries48.elitecommander.fragments.CommanderViewModelFactory
import com.devries48.elitecommander.fragments.MainFragment
import com.devries48.elitecommander.network.CommanderNetwork
import com.devries48.elitecommander.utils.OAuthUtils.storeUpdatedTokens
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mCommanderNetwork: CommanderNetwork
    private var mCommanderViewModel: CommanderViewModel? = null
    private val mNavController by lazy { findNavController() }

    private var mIsLoggedIn by Delegates.observable(false) { _, _, newValue ->
        if (newValue) loadData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDataBinding()
        setupViewModel()
        setupNavigation()
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
                    navController.navigate(navDestinationId)
                }
            }
        }
    }

    private fun setupViewModel() {
        try {
            mCommanderNetwork = CommanderNetwork()

            val viewModelProvider = ViewModelProvider(
                mNavController.getViewModelStoreOwner(R.id.nav_graph),
                CommanderViewModelFactory(mCommanderNetwork)
            )
            mCommanderViewModel = viewModelProvider.get(CommanderViewModel::class.java)

        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private fun loadData() {
        mCommanderNetwork.loadProfile()
        mCommanderNetwork.loadCurrentJournal()

        hideRedirectFragment()
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

    private fun hideRedirectFragment() {
        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        (navHostFragment.childFragmentManager.fragments[0] as MainFragment).removeItem()
    }

    companion object {
        private const val FRONTIER_LOGIN_REQUEST_CODE = 999
        private var navDestinationId: Int = 0
    }
}

