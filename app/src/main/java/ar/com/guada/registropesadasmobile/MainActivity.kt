package ar.com.guada.registropesadasmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ar.com.guada.registropesadasmobile.hardware.PantallaRegistroPesada
import ar.com.guada.registropesadasmobile.hardware.PantallaSeleccionAnimal
import ar.com.guada.registropesadasmobile.ui.theme.RegistroPesadasMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RegistroPesadasMobileTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PantallaRegistroPesada(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}