package com.insecure.m3.ui.login

import android.app.Activity
import android.os.Looper
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.insecure.m3.R
import com.insecure.m3.data.LoginRepository
import com.insecure.m3.data.Result
import khttp.get
import khttp.post
import java.util.logging.Handler
import android.os.AsyncTask




class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, password: String) {
        // can be launched in a separate asynchronous job
        val result = loginRepository.login(username, password)

        if (result is Result.Success) {
            _loginResult.value =
                LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
    }

    fun loginDataChanged(username: String, password: String) {
        //1

        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        }
//3
        val statusIsTrue: (Int)->Unit = { code ->

            _loginForm.value = LoginFormState(isDataValid = code == 200)
            print(code)

        }

//2
        userExists(username, password, statusIsTrue)
        /*
        if ( isUserNameValid(username) && isPasswordValid(password) && userExists()) {
            _loginForm.value = LoginFormState(isDataValid = true)
        } else {
            _loginForm.value = LoginFormState(isDataValid = false)
        }*/
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    private fun userExists(username: String, password: String, statusIsTrue:(Int)->Unit ) {

        Thread(Runnable {
            var statusCode = 401
            statusCode = post("http://http-login.badssl.com/submit/", data = mapOf(username to password)).statusCode

            statusIsTrue(statusCode)

        }).start()

    }
}
