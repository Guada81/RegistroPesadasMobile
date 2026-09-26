package ar.com.guada.registropesadasmobile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ar.com.guada.registropesadasmobile.red.LoginRequest
import ar.com.guada.registropesadasmobile.red.RetrofitCliente
import ar.com.guada.registropesadasmobile.red.SesionInfo
import ar.com.guada.registropesadasmobile.red.SesionRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun PantallaLogin(
    onLoginExitoso: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sesionRepository = remember { SesionRepository(context) }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Registro de Pesadas", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Usuario") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                cargando = true
                mensajeError = null
                scope.launch {
                    try {
                        val respuesta = RetrofitCliente.authApi.login(
                            LoginRequest(username, password)
                        )
                        sesionRepository.guardarSesion(
                            SesionInfo(
                                token = respuesta.token,
                                username = respuesta.username,
                                rol = respuesta.rol
                            )
                        )
                        onLoginExitoso()
                    } catch (e: HttpException) {
                        mensajeError = when (e.code()) {
                            403 -> "Tu usuario no tiene acceso a la app móvil."
                            401 -> "Usuario o contraseña incorrectos."
                            else -> "Error del servidor (${e.code()})."
                        }
                    } catch (e: Exception) {
                        mensajeError = "No se pudo conectar: ${e.message}"
                    } finally {
                        cargando = false
                    }
                }
            },
            enabled = !cargando && username.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (cargando) "Ingresando..." else "Ingresar")
        }

        mensajeError?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}