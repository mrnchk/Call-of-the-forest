package com.cotf

import android.app.Application
import com.cotf.session.UserSession

class CotfApp : Application() {
    val userSession by lazy { UserSession(this) }
}
