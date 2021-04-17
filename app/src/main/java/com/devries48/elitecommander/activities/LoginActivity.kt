package com.devries48.elitecommander.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.devries48.elitecommander.R
import com.devries48.elitecommander.databinding.ActivityLoginBinding
import com.devries48.elitecommander.events.FrontierTokensEvent
import com.devries48.elitecommander.network.FrontierAuthNetwork
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check step (back from browser or just opened)
        if (intent != null && intent!!.action != null &&
            intent!!.action == Intent.ACTION_VIEW
        ) {
            val uri = intent!!.data
            val code = uri?.getQueryParameter("code")
            val state = uri?.getQueryParameter("state")

            if (code != null && state != null) {
                switchUi(true)
                launchTokensStep(code, state)
                return
            }
        }

        binding.frontierLoginButton.setOnClickListener {
            binding.frontierLoginButton.isEnabled = false
            launchAuthCodeStep()
        }
    }

    private fun switchUi(isRedirect: Boolean) {
        binding.loginImageView.scaleX = if (isRedirect) 0.5f else 1f
        binding.loginImageView.scaleY = 0.5f

        binding.frontierLoginButton.isVisible = !isRedirect
        binding.loginTextView.isVisible = !isRedirect
        binding.redirectTextView.isVisible = !isRedirect
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFrontierTokensEvent(tokens: FrontierTokensEvent) {
        if (tokens.success) {
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            val dialog = MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.login_dialog_error_title)
                .setMessage(R.string.login_dialog_error_text)
                .setPositiveButton(android.R.string.ok) { d, _ ->
                    switchUi(false)
                    d.dismiss()
                }
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    finishAffinity()
                    finish()
                }
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
        val url = FrontierAuthNetwork.getInstance()?.getAuthorizationUrl(this)
        if (url != null) launchBrowserIntent(url)
        finish()
    }

    private fun launchTokensStep(authCode: String, state: String) {
        FrontierAuthNetwork.getInstance()?.sendTokensRequest(this, authCode, state)
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
        if (intent != null && intent.action == "org.chromium.arc.intent.action.VIEW") super.onNewIntent(
            Intent(intent).setAction(
                Intent.ACTION_VIEW
            )
        ) else super.onNewIntent(intent)
    }

}
