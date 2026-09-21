package ar.com.guada.registropesadasmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ar.com.guada.registropesadasmobile.hardware.LectorBalanzaClassic
import ar.com.guada.registropesadasmobile.ui.PantallaConexionBluetooth
import ar.com.guada.registropesadasmobile.ui.PantallaRegistroPesada
import ar.com.guada.registropesadasmobile.ui.theme.RegistroPesadasMobileTheme

enum class Pantalla {
    PRINCIPAL,
    CONEXION_BLUETOOTH
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RegistroPesadasMobileTheme {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()

                val lectorBalanza = remember { LectorBalanzaClassic(scope, context) }
                val estadoConexion by lectorBalanza.estadoConexion.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }
                var pantallaActual by remember { mutableStateOf(Pantalla.PRINCIPAL) }


                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    when (pantallaActual) {
                        Pantalla.PRINCIPAL -> PantallaRegistroPesada(
                            lectorBalanza = lectorBalanza,
                            estadoConexion = estadoConexion,
                            onIrAConexion = { pantallaActual = Pantalla.CONEXION_BLUETOOTH },
                            snackbarHostState = snackbarHostState,
                            modifier = Modifier.padding(innerPadding)
                        )
                        Pantalla.CONEXION_BLUETOOTH -> PantallaConexionBluetooth(
                            lectorBalanza = lectorBalanza,
                            onConectado = { pantallaActual = Pantalla.PRINCIPAL },
                            onVolver = { pantallaActual = Pantalla.PRINCIPAL },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}