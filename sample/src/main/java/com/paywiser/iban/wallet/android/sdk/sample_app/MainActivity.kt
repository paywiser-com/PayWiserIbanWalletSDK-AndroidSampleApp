package com.paywiser.iban.wallet.android.sdk.sample_app

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.github.razir.progressbutton.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        if (savedInstanceState == null) {
            setCurrentCountryToDropDown()
        }

        ccpPhoneNumber.registerCarrierNumberEditText(etPhoneNumber.editText)
        setObservers()
        setClickListeners()
        setButtonProgressAnimation()
    }

    private fun setObservers() {
        mainViewModel.userViewState.observe(this@MainActivity, {
            when(it) {
                UserViewState.LOGGED_IN -> {
                    groupLoggedIn.visibility = View.VISIBLE
                    groupPhoneNumberInput.visibility = View.GONE
                    groupVerificationCode.visibility = View.GONE
                    cvIBan.visibility = View.VISIBLE
                    tvAccounts.hideKeyboard()
                }
                UserViewState.INPUT_MOBILE_NUMBER -> {
                    groupLoggedIn.visibility = View.GONE
                    groupPhoneNumberInput.visibility = View.VISIBLE
                    groupVerificationCode.visibility = View.GONE
                    cvIBan.visibility = View.GONE
                    etPhoneNumber.editText?.focusAndShowKeyboard()
                }
                UserViewState.INPUT_VERIFICATION_CODE -> {
                    groupLoggedIn.visibility = View.GONE
                    groupPhoneNumberInput.visibility = View.GONE
                    groupVerificationCode.visibility = View.VISIBLE
                    cvIBan.visibility = View.GONE
                    etPhoneNumberConfirmationCode.editText?.focusAndShowKeyboard()
                }
                else -> { }
            }
        })

        mainViewModel.hideProgress.observe(this@MainActivity, {
            btnLogoutUser.hideProgress("Logout")
            btnSendVerificationCode.hideProgress("Send verification code")
            btnConfirmVerificationCode.hideProgress("Confirm code")
            btnListAccounts.hideProgress("Get accounts data")
        })

        mainViewModel.userMobileNumber.observe(this@MainActivity, {
            tvPhoneNumberVerificationOtpPhoneNumber.text = it
        })

        mainViewModel.userVerificationCodeTitle.observe(this@MainActivity, {
            tvPhoneNumberVerificationOtpTitle.text = it
        })

        mainViewModel.showError.observe(this@MainActivity, { showError ->
            when(showError.isNotEmpty()) {
                true -> {
                    cvError.visibility = View.VISIBLE
                    tvError.text = showError
                }
                false -> {
                    cvError.visibility = View.GONE
                    tvError.text = ""
                }
            }
        })

        mainViewModel.showAccounts.observe(this@MainActivity, {
            tvAccounts.text = it
        })

    }

    private fun setClickListeners() {
        ccpPhoneNumber.setPhoneNumberValidityChangeListener {
            btnSendVerificationCode.isEnabled = it
        }

        ccpPhoneNumber.setOnCountryChangeListener {
            setCurrentCountryToDropDown()
            etPhoneNumber.editText?.focusAndShowKeyboard()
        }

        etPhoneNumberCountryCode.setStartIconOnClickListener {
            ccpPhoneNumber.launchCountrySelectionDialog()
        }

        etPhoneNumberCountryCode.setEndIconOnClickListener {
            ccpPhoneNumber.launchCountrySelectionDialog()
        }

        (etPhoneNumberCountryCode.editText as? AutoCompleteTextView)?.setOnClickListener {
            ccpPhoneNumber.launchCountrySelectionDialog()
        }

        etPhoneNumber.editText?.setOnFocusChangeListener { _, hasFocus ->  ccpPhoneNumber.setHintExampleNumberEnabled(hasFocus) }

        btnSendVerificationCode.setOnClickListener {
            if (!btnSendVerificationCode.isProgressActive()) {
                val originalWidth = btnSendVerificationCode.width
                btnSendVerificationCode.showProgress {
                    progressColor = themeColor(R.attr.colorOnPrimary)
                    gravity = DrawableButton.GRAVITY_CENTER
                }
                btnSendVerificationCode.width = originalWidth
                mainViewModel.sendVerificationCode(ccpPhoneNumber.formattedFullNumber,
                    ccpPhoneNumber.fullNumberWithPlus)
            }
        }

        btnConfirmVerificationCode.setOnClickListener {
            if (!btnConfirmVerificationCode.isProgressActive()) {
                val originalWidth = btnConfirmVerificationCode.width
                btnConfirmVerificationCode.showProgress {
                    progressColor = themeColor(R.attr.colorOnPrimary)
                    gravity = DrawableButton.GRAVITY_CENTER
                }
                btnConfirmVerificationCode.width = originalWidth
                mainViewModel.confirmVerificationCode(etPhoneNumberConfirmationCode.editText?.text.toString())
            }
        }

        btnListAccounts.setOnClickListener {
            if (!btnListAccounts.isProgressActive()) {
                val originalWidth = btnListAccounts.width
                btnListAccounts.showProgress {
                    progressColor = themeColor(R.attr.colorOnPrimary)
                    gravity = DrawableButton.GRAVITY_CENTER
                }
                btnListAccounts.width = originalWidth
                mainViewModel.getAccounts()
            }
        }

        btnLogoutUser.setOnClickListener {
            if (!btnLogoutUser.isProgressActive()) {
                val originalWidth = btnLogoutUser.width
                btnLogoutUser.showProgress {
                    progressColor = themeColor(R.attr.colorOnPrimary)
                    gravity = DrawableButton.GRAVITY_CENTER
                }
                btnLogoutUser.width = originalWidth
                mainViewModel.logout()
            }
        }
    }

    private fun setButtonProgressAnimation() {
        this.bindProgressButton(btnLogoutUser)
        this.bindProgressButton(btnSendVerificationCode)
        this.bindProgressButton(btnConfirmVerificationCode)
        this.bindProgressButton(btnListAccounts)

        btnLogoutUser.attachTextChangeAnimator {
            fadeInMills = 300
            fadeOutMills = 300
        }
        btnSendVerificationCode.attachTextChangeAnimator {
            fadeInMills = 300
            fadeOutMills = 300
        }
        btnConfirmVerificationCode.attachTextChangeAnimator {
            fadeInMills = 300
            fadeOutMills = 300
        }
        btnListAccounts.attachTextChangeAnimator {
            fadeInMills = 300
            fadeOutMills = 300
        }
    }

    private fun setCurrentCountryToDropDown() {
        etPhoneNumberCountryCode.setStartIconDrawable(ccpPhoneNumber.selectedCountryFlagResourceId)
        (etPhoneNumberCountryCode.editText as? AutoCompleteTextView)?.setText(ccpPhoneNumber.selectedCountryCodeWithPlus)
        (etPhoneNumberCountryCode.editText as? AutoCompleteTextView)?.dismissDropDown()
    }
}