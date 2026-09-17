package com.example.financemanager

import android.app.Application
import com.google.firebase.FirebaseApp

class FinanceManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
