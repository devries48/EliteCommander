package com.devries48.elitecommander.network.retrofit

import android.content.Context
import android.util.Base64
import com.devries48.elitecommander.BuildConfig
import com.devries48.elitecommander.R
import com.devries48.elitecommander.events.FrontierTokensEvent
import com.devries48.elitecommander.models.FrontierAccessTokenRequestBody
import com.devries48.elitecommander.models.FrontierAccessTokenResponse
import com.devries48.elitecommander.utils.OAuthUtils
import com.devries48.elitecommander.utils.OAuthUtils.getAuthorizationCodeRequestBody
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.internal.EverythingIsNonNull
import java.io.Serializable
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom


// Singleton safe from serialization/reflection...
// From https://medium.com/exploring-code/how-to-make-the-perfect-singleton-de6b951dfdb0
open class FrontierAuthSingleton private constructor() : Serializable {
    private var codeVerifier: String? = null
    private var codeChallenge: String? = null
    private var requestState: String? = null

    //Make singleton from serialize and deserialize operation.
    protected fun readResolve(): FrontierAuthSingleton? {
        return getInstance()
    }

    private fun generateCodeVerifierAndChallenge() {
        val sr = SecureRandom()
        val code = ByteArray(32)
        sr.nextBytes(code)
        codeVerifier = Base64.encodeToString(
            code,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
        codeChallenge = try {
            val bytes = codeVerifier?.toByteArray(StandardCharsets.US_ASCII)
            val md = MessageDigest.getInstance("SHA-256")
            if (bytes != null) {
                md.update(bytes, 0, bytes.size)
            }
            val digest = md.digest()
            Base64.encodeToString(
                digest,
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun generateState() {
        val sr = SecureRandom()
        val code = ByteArray(8)
        sr.nextBytes(code)
        requestState = Base64.encodeToString(
            code,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
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

    fun sendTokensRequest(ctx: Context, authCode: String?, state: String) {
        // Check if same state
        if (state != requestState) {
            EventBus.getDefault().post(
                FrontierTokensEvent(
                    false,
                    "", ""
                )
            )
        }
        val retrofit: RetrofitSingleton? = RetrofitSingleton.getInstance()
        val frontierAuth = retrofit?.getFrontierAuthRetrofit(ctx)

        val callback: Callback<FrontierAccessTokenResponse> =
            object : Callback<FrontierAccessTokenResponse> {
                @EverythingIsNonNull
                override fun onResponse(
                    call: Call<FrontierAccessTokenResponse>,
                    response: Response<FrontierAccessTokenResponse>
                ) {
                    val body: FrontierAccessTokenResponse? = response.body()
                    if (!response.isSuccessful || body == null) {
                        onFailure(call, Exception("Invalid response"))
                    } else {
                        OAuthUtils.storeUpdatedTokens(ctx, body.accessToken!!, body.refreshToken!!)
                        EventBus.getDefault().post(
                            FrontierTokensEvent(
                                true,
                                body.accessToken!!, body.refreshToken!!
                            )
                        )
                    }
                }

                @EverythingIsNonNull
                override fun onFailure(call: Call<FrontierAccessTokenResponse>, t: Throwable?) {
                    EventBus.getDefault().post(
                        FrontierTokensEvent(
                            false,
                            "", ""
                        )
                    )
                }
            }

        val requestBody: FrontierAccessTokenRequestBody =
            getAuthorizationCodeRequestBody(
                codeVerifier, authCode
            )
        frontierAuth?.getAccessToken(requestBody)?.enqueue(callback)
    }

    companion object {
        @Volatile
        private var instance: FrontierAuthSingleton? = null
        fun getInstance(): FrontierAuthSingleton? {
            if (instance == null) {
                synchronized(FrontierAuthSingleton::class.java) {
                    if (instance == null) instance =
                        FrontierAuthSingleton()
                }
            }
            return instance
        }
    }

    // Private constructor.
    init {

        // Prevent form the reflection api.
        if (instance != null) {
            throw RuntimeException("Use getInstance() method to get an instance of this class.")
        }
    }
}
