package com.devries48.elitecommander.activities

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
import com.devries48.elitecommander.network.CommanderClient
import com.devries48.elitecommander.utils.OAuthUtils.storeUpdatedTokens
import com.devries48.elitecommander.viewModels.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {

    private var mCurrentDestinationId: Int = 0
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mCommanderClient: CommanderClient

    private val mNavController by lazy { findNavController() }
    private var mMainViewModel: MainViewModel? = null
    private var mAlertList = ArrayList<@StringRes Int>()
    private var mAlertDialog: androidx.appcompat.app.AlertDialog? = null

    private var mIsLoggedIn: Boolean? by Delegates.observable(null) { _, _, newValue ->
        if (newValue == true) {
            startEventBus()
            setupViewModel()
        } else {
            stopEventBus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(mBinding.root)
        setSupportActionBar(mBinding.mainToolbar)
        setupNavigation()
        setupViewModel()
        setupRefresh()
    }

    public override fun onStart() {
        super.onStart()
        startEventBus()
    }

    public override fun onStop() {
        super.onStop()
        stopEventBus()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mIsLoggedIn = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_search -> {
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        val exitIntent = Intent(Intent.ACTION_MAIN)
        exitIntent.addCategory(Intent.CATEGORY_HOME)
        exitIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(exitIntent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    private fun startEventBus() {
        synchronized(this) {
            if (!EventBus.getDefault().isRegistered(this))
                EventBus.getDefault().register(this)
        }
    }

    private fun stopEventBus() {
        synchronized(this) {
            if (EventBus.getDefault().isRegistered(this))
                EventBus.getDefault().unregister(this)
        }
    }

    private fun setupViewModel() {
        try {
            mCommanderClient = CommanderClient()

            val viewModelProvider = ViewModelProvider(
                mNavController.getViewModelStoreOwner(R.id.nav_main),
                MainViewModel.Factory(mCommanderClient)
            )
            mMainViewModel = viewModelProvider.get(MainViewModel::class.java)
            mMainViewModel!!.load()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private fun setupNavigation() {
        val navController = findNavController()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.mainFragment && mCurrentDestinationId != destination.id) {
                mCurrentDestinationId = destination.id
                navController.navigate(destination.id)
            }
        }
    }

    private fun setupRefresh() {
        val swipeRefresh = mBinding.swipeLayout
        swipeRefresh.setDistanceToTriggerSync(200)
        swipeRefresh.setOnRefreshListener {
            if (mMainViewModel != null) {
                mMainViewModel!!.load()
                swipeRefresh.post {
                    swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    private fun findNavController(): NavController {
        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.mainNavHost) as NavHostFragment
        return navHostFragment.navController
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onFrontierAuthNeededEvent(auth: FrontierAuthNeededEvent) {
        if (auth.needed && mIsLoggedIn != false) {
            mIsLoggedIn = false
            storeUpdatedTokens(this, "", "")

            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivityForResult(intent, 0)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAlertEvent(alertEvent: AlertEvent) {
        synchronized(mAlertList) {
            if (mIsLoggedIn == true) {
                mAlertList.clear()
                return@synchronized
            }

            mAlertList.add(alertEvent.message)
            if (mAlertDialog == null)
                showAlertDialog(alertEvent)
            else {
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

    private fun showAlertDialog(alertEvent: AlertEvent) {
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
    }
}
