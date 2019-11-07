package com.insecure.m3.ui.login

import android.os.Handler
import android.os.Looper
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.insecure.m3.R
import com.insecure.m3.data.LoginRepository
import com.insecure.m3.data.Result
import khttp.post


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

            var handler = Handler(Looper.getMainLooper())

            handler.post({
                 run {
                     var result =  code == 200

                    _loginForm.value = LoginFormState(isDataValid = result)
                    println("AQUI: "+code)
                }
            })

        }

        // 2
        if (isUserNameValid(username) && isPasswordValid(password) ){
            userExists(username, password, statusIsTrue)
        }
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
            statusCode = post("http://10.127.40.163:8090/auth", data = mapOf(username to password)).statusCode

            statusIsTrue(statusCode)

        }).start()

    }
}
