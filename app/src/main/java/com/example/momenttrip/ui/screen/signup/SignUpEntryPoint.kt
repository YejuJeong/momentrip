package com.example.momenttrip.ui.screen.signup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.momenttrip.viewmodel.UserViewModel

@Composable
fun SignUpEntryPoint(
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val userViewModel: UserViewModel = viewModel()

    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    val name = remember { mutableStateOf("") }
    val nickname = remember { mutableStateOf("") }
    val phoneNumber = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf("") }

    val emailCheckMessage = remember { mutableStateOf("") }
    val nicknameCheckMessage = remember { mutableStateOf("") }
    val phoneCheckMessage = remember { mutableStateOf("") }

    SignUpScreen(
        email = email.value,
        password = password.value,
        confirmPassword = confirmPassword.value,
        name = name.value,
        nickname = nickname.value,
        phoneNumber = phoneNumber.value,
        errorMessage = errorMessage.value,
        emailCheckMessage = emailCheckMessage.value,
        nicknameCheckMessage = nicknameCheckMessage.value,
        phoneCheckMessage = phoneCheckMessage.value,
        onEmailChange = {
            email.value = it
            userViewModel.checkEmailExists(it) { exists ->
                emailCheckMessage.value = if (exists) "이미 사용 중인 이메일입니다" else "사용 가능한 이메일입니다"
            }
        },
        onPasswordChange = { password.value = it },
        onConfirmPasswordChange = { confirmPassword.value = it },
        onNameChange = { name.value = it },
        onNicknameChange = {
            nickname.value = it
            userViewModel.checkNicknameExistsForSignup(it) { exists ->
                nicknameCheckMessage.value = if (exists) "이미 사용 중인 닉네임입니다" else "사용 가능"
            }
        },
        onPhoneNumberChange = {
            phoneNumber.value = it
            userViewModel.checkPhoneExists(it) { exists ->
                phoneCheckMessage.value = if (exists) "이미 등록된 전화번호입니다" else ""
            }
        },
        onSignUpClick = {
            if (password.value != confirmPassword.value) {
                errorMessage.value = "비밀번호가 일치하지 않습니다"
                return@SignUpScreen
            }

            userViewModel.registerUser(
                email = email.value,
                password = password.value,
                name = name.value,
                nickname = nickname.value,
                phone = phoneNumber.value,
                callback = { success, message ->
                    if (success) {
                        errorMessage.value = ""
                        onSuccess()
                    } else {
                        errorMessage.value = message ?: "알 수 없는 오류"
                    }
                }
            )
        }
    )
}


