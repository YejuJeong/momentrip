import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.momenttrip.ui.screen.login.LoginScreen
import com.example.momenttrip.ui.screen.login.LoginViewModel

@Composable
fun LoginEntryPoint(
    onLoginSuccess: () -> Unit,
    onSignUpClick: () -> Unit
) {
    val viewModel: LoginViewModel = viewModel()
    val errorMessage = remember { mutableStateOf("") }

    LoginScreen(
        email = viewModel.email.value,
        password = viewModel.password.value,
        errorMessage = errorMessage.value,
        onEmailChange = { viewModel.email.value = it },
        onPasswordChange = { viewModel.password.value = it },
        onLoginClick = {
            viewModel.login(
                email = viewModel.email.value,
                password = viewModel.password.value,
                onSuccess = {
                    errorMessage.value = ""
                    onLoginSuccess()
                },
                onError = { msg ->
                    errorMessage.value = msg
                }
            )
        },
        onSignUpClick = onSignUpClick,
        onNaverLogin = { /* TODO */ },
        onKakaoLogin = { /* TODO */ }
    )
}
