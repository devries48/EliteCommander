package com.devries48.elitecommander.activities

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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
import com.devries48.elitecommander.fragments.CommanderViewModel
import com.devries48.elitecommander.network.CommanderClient
import com.devries48.elitecommander.utils.OAuthUtils.storeUpdatedTokens
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private var mCurrentDestinationId: Int = 0
    private lateinit var binding: ActivityMainBinding
    private lateinit var mCommanderClient: CommanderClient

    private var mCommanderViewModel: CommanderViewModel? = null
    private val mNavController by lazy { findNavController() }
    private var mAlertList = ArrayList<@StringRes Int>()
    private var mAlertDialog: androidx.appcompat.app.AlertDialog? = null

    private var mIsLoggedIn: Boolean? by Delegates.observable(null) { _, _, newValue ->
        if (newValue == true) {
            EventBus.getDefault().register(this)
            setupViewModel()
        } else
            EventBus.getDefault().unregister(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val exitIntent = Intent(Intent.ACTION_MAIN)
        exitIntent.addCategory(Intent.CATEGORY_HOME)
        exitIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(exitIntent)
    }

    private fun setupViewModel() {
        try {
            mCommanderClient = CommanderClient()

            val viewModelProvider = ViewModelProvider(
                mNavController.getViewModelStoreOwner(R.id.nav_graph),
                CommanderViewModel.Factory(mCommanderClient)
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
            if (destination.id == R.id.mainFragment && mCurrentDestinationId != destination.id) {
                mCurrentDestinationId = destination.id
                navController.navigate(destination.id)
            }
        }
    }

    private fun findNavController(): NavController {
        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFrontierAuthNeededEvent(frontierAuthNeededEvent: FrontierAuthNeededEvent) {
        if (mIsLoggedIn != false) {
            mIsLoggedIn = false
            storeUpdatedTokens(this, "", "")
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivityForResult(intent, FRONTIER_LOGIN_REQUEST_CODE)
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

    companion object {
        private const val FRONTIER_LOGIN_REQUEST_CODE = 999
    }
}
