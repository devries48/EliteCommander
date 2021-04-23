package com.devries48.elitecommander.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("LOGIN")
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.frontierLoginButton.setOnClickListener { launchAuthCodeStep() }

        binding.webView.webViewClient = object : WebViewClient() {
            var authComplete = false
            override fun onReceivedClientCertRequest(view: WebView?, request: ClientCertRequest?) {
                super.onReceivedClientCertRequest(view, request)
                println("WEB --- onReceivedClientCertRequest")
            }

            override fun onReceivedHttpAuthRequest(
                view: WebView?,
                handler: HttpAuthHandler?,
                host: String?,
                realm: String?
            ) {
                super.onReceivedHttpAuthRequest(view, handler, host, realm)
                println("WEB --- onReceivedHttpAuthRequest")
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                println("WEB --- onPageFinished")
                if (url.contains("?code=") && !authComplete) {
                    val uri = Uri.parse(url)
                    val code = uri?.getQueryParameter("code")
                    val state = uri?.getQueryParameter("state")

                    println("WEB --- launchTokenStep")
                    authComplete = true
                    if (code != null && state != null) launchTokensStep(code, state)
                }
            }
        }
        // use cookies to remember a logged in status
        CookieManager.getInstance().flush()
        binding.webView.settings.javaScriptEnabled = true
    }

    private fun switchUi(isRedirect: Boolean) {
        binding.frontierLoginButton.isVisible = !isRedirect
        binding.loginTextView.isVisible = !isRedirect
        binding.redirectTextView.isVisible = isRedirect
        binding.webView.visibility = if (isRedirect) VISIBLE else GONE
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onFrontierTokensEvent(tokens: FrontierTokensEvent) {
        if (tokens.success) {
            EventBus.getDefault().unregister(this)
            val returnIntent = intent
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        } else {
            val dialog = MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.login_dialog_error_title)
                .setMessage(R.string.login_dialog_error_text)
                .setBackground(ColorDrawable(ContextCompat.getColor(this, R.color.black)))
                .setPositiveButton(android.R.string.ok) { d, _ ->
                    binding.frontierLoginButton.isEnabled = true
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
        if (url != null) {
            switchUi(true)
            binding.webView.loadUrl(url)
        }
    }

    private fun launchTokensStep(authCode: String, state: String) {
        FrontierAuthNetwork.getInstance()?.sendTokensRequest(this, authCode, state)
    }
}
