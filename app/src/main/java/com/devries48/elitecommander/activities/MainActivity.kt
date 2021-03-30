package com.devries48.elitecommander.activities

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.devries48.elitecommander.R
import com.devries48.elitecommander.databinding.ActivityMainBinding
import com.devries48.elitecommander.events.AlertEvent
import com.devries48.elitecommander.events.FrontierAuthNeededEvent
import com.devries48.elitecommander.events.FrontierTokensEvent
import com.devries48.elitecommander.fragments.CommanderViewModel
import com.devries48.elitecommander.fragments.CommanderViewModelFactory
import com.devries48.elitecommander.fragments.MainFragment
import com.devries48.elitecommander.network.CommanderNetwork
import com.devries48.elitecommander.utils.OAuthUtils.storeUpdatedTokens
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mCommanderNetwork: CommanderNetwork

    private var mCommanderViewModel: CommanderViewModel? = null
    private val mNavController by lazy { findNavController() }
    private var mNavDestinationId: Int = 0
    private var mAlertList = ArrayList<@StringRes Int>()
    private var mAlertDialog: androidx.appcompat.app.AlertDialog? = null

    private var mIsLoggedIn by Delegates.observable(false) { _, _, newValue ->
        if (newValue) {
            setupViewModel()
            hideRedirectFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupNavigation()
        setupViewModel()
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

        if (requestCode == FRONTIER_LOGIN_REQUEST_CODE) mIsLoggedIn = true
    }

    private fun setupViewModel() {
        try {
            mCommanderNetwork = CommanderNetwork()

            val viewModelProvider = ViewModelProvider(
                mNavController.getViewModelStoreOwner(R.id.nav_graph),
                CommanderViewModelFactory(mCommanderNetwork)
            )
            mCommanderViewModel = viewModelProvider.get(CommanderViewModel::class.java)
            mCommanderViewModel!!.load()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private fun setupNavigation() {
        val navController = findNavController()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            mNavDestinationId = destination.id

            // if (!mIsLoggedIn) login() else if (mNavDestinationId == R.id.mainFragment) navController.navigate(mNavDestinationId)
            if (mNavDestinationId == R.id.mainFragment)  navController.navigate(mNavDestinationId)
        }
    }

    private fun findNavController(): NavController {
        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onFrontierTokensEvent(tokens: FrontierTokensEvent) {
        if (tokens.success) {
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivityForResult(intent, FRONTIER_LOGIN_REQUEST_CODE)
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onFrontierAuthNeededEvent(frontierAuthNeededEvent: FrontierAuthNeededEvent) {
        mIsLoggedIn = false
        storeUpdatedTokens(this, "", "")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAlertEvent(alertEvent: AlertEvent) {
        synchronized(mAlertList) {
            mAlertList.add(alertEvent.message)
            if (mAlertDialog == null) {
                val builder = MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                builder.setTitle(alertEvent.title)
                builder.setMessage(alertEvent.message)
                builder.background = ColorDrawable(ContextCompat.getColor(this, R.color.black))
                builder.setPositiveButton("OK") { _, _ ->
                    mAlertDialog?.dismiss()
                    mAlertDialog = null
                }

                builder.setCancelable(false)

                mAlertDialog = builder.create()
                mAlertDialog!!.show()
            } else {
                var message = ""
                for ((index, it) in mAlertList.withIndex()) {
                    message += "\u2022 " + this.getString(it)
                    if (index < mAlertList.count() - 1)
                        message += "\n"
                }
                mAlertDialog!!.setMessage(message)
            }
        }
    }

    private fun hideRedirectFragment() {
        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        (navHostFragment.childFragmentManager.fragments[0] as MainFragment).removeItem()
    }

    companion object {
        private const val FRONTIER_LOGIN_REQUEST_CODE = 999
    }
}
