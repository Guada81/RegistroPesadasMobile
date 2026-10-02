package ar.com.guada.registropesadasmobile.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.com.guada.registropesadasmobile.Pantalla
import ar.com.guada.registropesadasmobile.data.ResultadoSyncAnimales

@Composable
fun AppPrincipal() {
    val viewModel: MainViewModel = viewModel()

    val sesion by viewModel.sesion.collectAsState()
    val sesionCargada by viewModel.sesionCargada.collectAsState()
    val pantallaActual by viewModel.pantallaActual.collectAsState()
    val actualizandoAnimales by viewModel.actualizandoAnimales.collectAsState()

    val lectorBalanza = viewModel.lectorBalanza
    val estadoConexion by lectorBalanza.estadoConexion.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Cada vez que el ViewModel avisa que terminó una sincronización,
    // mostramos el mensaje que corresponda.
    LaunchedEffect(Unit) {
        viewModel.resultadosSync.collect { resultado ->
            when (resultado) {
                ResultadoSyncAnimales.OK ->
                    snackbarHostState.showSnackbar("Animales actualizados")
                ResultadoSyncAnimales.SIN_CONEXION ->
                    snackbarHostState.showSnackbar("Sin conexión: no se pudo actualizar la lista de animales")
                ResultadoSyncAnimales.ERROR_SERVIDOR ->
                    snackbarHostState.showSnackbar("Error del servidor al actualizar animales")
                ResultadoSyncAnimales.SESION_EXPIRADA -> { }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            !sesionCargada -> {
                // Evita un parpadeo mostrando el login por un
                // instante mientras DataStore todavía no contestó.
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            sesion == null -> {
                PantallaLogin(
                    onLoginExitoso = { viewModel.sincronizarAnimales() },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            else -> {
                when (pantallaActual) {
                    Pantalla.PRINCIPAL -> PantallaRegistroPesada(
                        lectorBalanza = lectorBalanza,
                        estadoConexion = estadoConexion,
                        onIrAConexion = { viewModel.irAPantalla(Pantalla.CONEXION_BLUETOOTH) },
                        onCerrarSesion = { viewModel.cerrarSesion() },
                        onActualizarAnimales = { viewModel.sincronizarAnimales() },
                        actualizandoAnimales = actualizandoAnimales,
                        snackbarHostState = snackbarHostState,
                        modifier = Modifier.padding(innerPadding)
                    )
                    Pantalla.CONEXION_BLUETOOTH -> PantallaConexionBluetooth(
                        lectorBalanza = lectorBalanza,
                        onConectado = { viewModel.irAPantalla(Pantalla.PRINCIPAL) },
                        onVolver = { viewModel.irAPantalla(Pantalla.PRINCIPAL) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}