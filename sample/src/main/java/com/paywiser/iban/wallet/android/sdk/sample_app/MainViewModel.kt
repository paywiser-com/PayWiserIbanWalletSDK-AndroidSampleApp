package com.paywiser.iban.wallet.android.sdk.sample_app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.paywiser.iban.wallet.android.sdk.PayWiserIbanWallet
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    private val _userViewState: MutableLiveData<UserViewState> by lazy { MutableLiveData<UserViewState>() }
    val userViewState: LiveData<UserViewState> get() = _userViewState

    private val _hideProgress: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val hideProgress: LiveData<Boolean> get() = _hideProgress

    private val _userMobileNumber: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val userMobileNumber: LiveData<String> get() = _userMobileNumber

    private val _userVerificationCodeTitle: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val userVerificationCodeTitle: LiveData<String> get() = _userVerificationCodeTitle

    private val _showError: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val showError: LiveData<String> get() = _showError

    private val _showAccounts: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val showAccounts: LiveData<String> get() = _showAccounts

    init {
        setActivityViewBasedOnUserLoggedIn()
    }

    private fun setActivityViewBasedOnUserLoggedIn() {
        _userViewState.value = when(PayWiserIbanWallet.isUserLoggedIn) {
            true -> UserViewState.LOGGED_IN
            false -> UserViewState.INPUT_MOBILE_NUMBER
        }
    }

    fun sendVerificationCode(formattedMobileNumber: String, mobileNumberWithPlushSign: String) {
        viewModelScope.launch {
            val response = PayWiserIbanWallet.loginUserSendVerificationCode(mobileNumberWithPlushSign)
            when (response.statusCode) {
                0 -> {
                    _userVerificationCodeTitle.postValue("Enter the ${response.verificationCodeLength}-digit confirmation code received over SMS")
                    _userMobileNumber.postValue(formattedMobileNumber)
                    _hideProgress.postValue(true)
                    _showError.postValue("")
                    _userViewState.postValue(UserViewState.INPUT_VERIFICATION_CODE)
                }
                else -> {
                    _hideProgress.postValue(true)
                    _showError.postValue("StatusCode: ${response.statusCode} with statusDescription: ${response.statusDescription}")
                }
            }
        }
    }

    fun confirmVerificationCode(verificationCode: String) {
        viewModelScope.launch {
            val response = PayWiserIbanWallet.loginUserConfirmVerificationCode(verificationCode)
            when (response.statusCode) {
                0 -> {
                    _hideProgress.postValue(true)
                    _showError.postValue("")
                    _showAccounts.postValue("")
                    setActivityViewBasedOnUserLoggedIn()
                }
                else -> {
                    _hideProgress.postValue(true)
                    _showError.postValue("StatusCode: ${response.statusCode} with statusDescription: ${response.statusDescription}")
                }
            }
        }
    }

    fun getAccounts() {
        viewModelScope.launch {
            val response = PayWiserIbanWallet.listIbans()
            when (response.statusCode) {
                0 -> {
                    _hideProgress.postValue(true)
                    _showError.postValue("")
                    response.ibans.takeIf { !it.isNullOrEmpty() }?.let {
                        _showAccounts.postValue(Gson().toJson(it))
                    }?: _showAccounts.postValue("No accounts!")

                }
                else -> {
                    _hideProgress.postValue(true)
                    _showError.postValue("StatusCode: ${response.statusCode} with statusDescription: ${response.statusDescription}")
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            PayWiserIbanWallet.logoutUser()
            setActivityViewBasedOnUserLoggedIn()
        }
    }

}