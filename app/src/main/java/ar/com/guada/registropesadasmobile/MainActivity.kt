package ar.com.guada.registropesadasmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ar.com.guada.registropesadasmobile.red.RetrofitCliente
import ar.com.guada.registropesadasmobile.sync.programarSyncPeriodico
import ar.com.guada.registropesadasmobile.ui.AppPrincipal
import ar.com.guada.registropesadasmobile.ui.theme.RegistroPesadasMobileTheme

enum class Pantalla {
    PRINCIPAL,
    CONEXION_BLUETOOTH
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        RetrofitCliente.inicializar(applicationContext)
        programarSyncPeriodico(applicationContext)
        setContent {
            RegistroPesadasMobileTheme {
                AppPrincipal()
            }
        }
    }
}