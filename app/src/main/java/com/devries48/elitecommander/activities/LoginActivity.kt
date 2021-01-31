package com.devries48.elitecommander.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.devries48.elitecommander.R
import com.devries48.elitecommander.events.FrontierTokensEvent
import com.devries48.elitecommander.network.retrofit.FrontierAuthSingleton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val button: Button = findViewById(R.id.frontierLoginButton)

        // Check step (back from browser or just opened)
        if (intent != null && intent!!.action != null &&
            intent!!.action == Intent.ACTION_VIEW
        ) {
            val uri = intent!!.data

            val code = uri?.getQueryParameter("code")
            val state = uri?.getQueryParameter("state")

            if (code != null && state != null) {
                button.isVisible = false
                launchTokensStep(code, state)
                return
            }
        }

        button.setOnClickListener {
            button.isEnabled = false
            launchAuthCodeStep()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFrontierTokensEvent(tokens: FrontierTokensEvent) {
        if (tokens.success) {
            val t = Toast.makeText(this, R.string.login_success, Toast.LENGTH_LONG)
            t.show()
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            val dialog = MaterialAlertDialogBuilder(this)
                .setTitle(R.string.login_dialog_error_title)
                .setMessage(R.string.login_dialog_error_text)
                .setOnCancelListener { finish() }
                .setOnDismissListener { finish() }
                .setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                .setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
                .create()

            dialog.show()
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

    private fun launchAuthCodeStep() {
        // Authorization step
        val url = FrontierAuthSingleton.getInstance()
            ?.getAuthorizationUrl(applicationContext)

        if (url != null) {
            launchBrowserIntent(url)
        }
        finish()
    }

    private fun launchTokensStep(authCode: String, state: String) {
        // Tokens exchange step
        FrontierAuthSingleton.getInstance()?.sendTokensRequest(applicationContext, authCode, state)
    }

    private fun launchBrowserIntent(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW)
        browserIntent.flags = browserIntent.flags or Intent.FLAG_ACTIVITY_NO_HISTORY
        browserIntent.data = Uri.parse(url)
        startActivity(browserIntent)
    }

    override fun getIntent(): Intent? {
        val intent = super.getIntent()
        return if (intent != null && intent.action == "org.chromium.arc.intent.action.VIEW") {
            Intent(intent).setAction(Intent.ACTION_VIEW)
        } else intent
    }

    override fun onNewIntent(intent: Intent?) {
        if (intent != null && intent.action == "org.chromium.arc.intent.action.VIEW") {
            super.onNewIntent(Intent(intent).setAction(Intent.ACTION_VIEW))
        } else {
            super.onNewIntent(intent)
        }
    }

}
