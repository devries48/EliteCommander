package com.devries48.elitecommander.network

import android.content.Context
import android.util.Base64
import com.devries48.elitecommander.BuildConfig
import com.devries48.elitecommander.R
import com.devries48.elitecommander.declarations.enqueueWrap
import com.devries48.elitecommander.events.FrontierTokensEvent
import com.devries48.elitecommander.models.httpRequest.FrontierAccessTokenRequestBody
import com.devries48.elitecommander.models.response.frontier.AccessTokenResponse
import com.devries48.elitecommander.utils.OAuthUtils
import com.devries48.elitecommander.utils.OAuthUtils.getAuthorizationCodeRequestBody
import org.greenrobot.eventbus.EventBus
import java.io.Serializable
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom

// Singleton safe from serialization/reflection...
// From https://medium.com/exploring-code/how-to-make-the-perfect-singleton-de6b951dfdb0
open class FrontierAuthNetwork private constructor() : Serializable {
    private var codeVerifier: String? = null
    private var codeChallenge: String? = null
    private var requestState: String? = null

    private fun generateCodeVerifierAndChallenge() {
        val sr = SecureRandom()
        val code = ByteArray(32)
        sr.nextBytes(code)
        codeVerifier = getEncodedString(code)
        codeChallenge = try {
            val bytes = codeVerifier?.toByteArray(StandardCharsets.US_ASCII)
            val md = MessageDigest.getInstance("SHA-256")
            if (bytes != null) md.update(bytes, 0, bytes.size)
            getEncodedString(md.digest())
        } catch (e: Exception) {
            null
        }
    }

    private fun generateState() {
        val sr = SecureRandom()
        val code = ByteArray(8)
        sr.nextBytes(code)
        requestState = getEncodedString(code)
    }

    private fun getEncodedString(code: ByteArray): String? {
        return Base64.encodeToString(code, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    fun getAuthorizationUrl(ctx: Context): String {
        generateCodeVerifierAndChallenge()
        generateState()
        return ctx.getString(R.string.frontier_auth_base) + "auth" +
                "?audience=all" +
                "&scope=capi" +
                "&response_type=code" +
                "&state=" + requestState +
                "&client_id=" + BuildConfig.FRONTIER_AUTH_CLIENT_ID +
                "&code_challenge=" + codeChallenge +
                "&code_challenge_method=S256" +
                "&redirect_uri=elitecommander://oauth"
    }

    fun sendTokensRequest(ctx: Context, authCode: String?, state: String?) {
        if (state != requestState) EventBus.getDefault().post(FrontierTokensEvent(false, "", ""))

        val retrofit: RetrofitClient? = RetrofitClient.getInstance()
        val frontierAuth = retrofit?.getFrontierAuthRetrofit(ctx)
        val requestBody: FrontierAccessTokenRequestBody = getAuthorizationCodeRequestBody(codeVerifier, authCode)

        frontierAuth?.getAccessToken(requestBody)?.enqueueWrap {
            onResponse = {
                val body: AccessTokenResponse? = it.body()
                if (!it.isSuccessful || body == null)
                    onFailure?.let { it1 -> it1(Exception("Invalid response")) }
                else {
                    OAuthUtils.storeUpdatedTokens(ctx, body.accessToken!!, body.refreshToken!!)
                    EventBus.getDefault().post(
                        FrontierTokensEvent(true, body.accessToken!!, body.refreshToken!!)
                    )
                }
            }
            onFailure = {
                EventBus.getDefault().post(FrontierTokensEvent(false, "", ""))
            }
        }
    }

    companion object {
        @Volatile
        private var instance: FrontierAuthNetwork? = null
        fun getInstance(): FrontierAuthNetwork? {
            if (instance == null)
                synchronized(FrontierAuthNetwork::class.java) {
                    if (instance == null) instance =
                        FrontierAuthNetwork()
                }
            return instance
        }
    }

    init {
        // Prevent form the reflection api.
        if (instance != null) {
            throw RuntimeException("Use getInstance() method to get an instance of this class.")
        }
    }
}
