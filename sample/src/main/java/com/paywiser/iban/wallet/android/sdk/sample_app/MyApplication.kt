package com.paywiser.iban.wallet.android.sdk.sample_app

import android.app.Application
import com.paywiser.iban.wallet.android.sdk.PayWiserIbanWallet

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        PayWiserIbanWallet.initialize("https://iban-dev.paywiser.eu/mobile.integration", "inter", "gration")
    }
}