package com.cotf

import android.app.Application
import com.cotf.network.AuthApi
import com.cotf.network.RetrofitClient
import com.cotf.session.UserSession

class CotfApp : Application() {
    val userSession by lazy { UserSession(this) }

    val authApi: AuthApi by lazy {
        RetrofitClient.create(AuthApi::class.java, userSession)
    }
}
