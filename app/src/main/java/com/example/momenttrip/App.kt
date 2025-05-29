package com.example.momenttrip

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.momenttrip.ui.screen.login.LoginScreen
import com.example.momenttrip.ui.screen.login.LoginViewModel
import com.example.momenttrip.ui.screen.main.MainScreen
import com.example.momenttrip.ui.screen.signup.SignUpEntryPoint
import com.example.momenttrip.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppEntryPoint() {
    val context = LocalContext.current
    val isLoggedIn = remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }
    val isSignUpMode = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf("") }

    if (isLoggedIn.value) {
        MainScreen(onLogout = { isLoggedIn.value = false })
    } else {
        if (isSignUpMode.value) {
            val userViewModel: UserViewModel = viewModel()
            SignUpEntryPoint(
                onSuccess = {
                    isSignUpMode.value = false
                    isLoggedIn.value = true
                },
                onCancel = {
                    isSignUpMode.value = false
                }
            )
        } else {
            val loginViewModel: LoginViewModel = viewModel()
            LoginScreen(
                email = loginViewModel.email.value,
                password = loginViewModel.password.value,
                errorMessage = errorMessage.value,
                onEmailChange = { loginViewModel.email.value = it },
                onPasswordChange = { loginViewModel.password.value = it },
                onLoginClick = {
                    loginViewModel.login(
                        email = loginViewModel.email.value,
                        password = loginViewModel.password.value,
                        onSuccess = { isLoggedIn.value = true },
                        onError = { errorMessage.value = it }
                    )
                },
                onSignUpClick = { isSignUpMode.value = true },
                onNaverLogin = { Toast.makeText(context, "네이버 로그인 준비 중", Toast.LENGTH_SHORT).show() },
                onKakaoLogin = { Toast.makeText(context, "카카오 로그인 준비 중", Toast.LENGTH_SHORT).show() }
            )
        }
    }
}
