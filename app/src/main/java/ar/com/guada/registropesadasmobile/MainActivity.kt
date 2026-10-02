package ar.com.guada.registropesadasmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.platform.LocalContext
import ar.com.guada.registropesadasmobile.hardware.LectorBalanzaClassic
import ar.com.guada.registropesadasmobile.red.RetrofitCliente
import ar.com.guada.registropesadasmobile.red.SesionInfo
import ar.com.guada.registropesadasmobile.red.SesionRepository
import ar.com.guada.registropesadasmobile.sync.programarSyncPeriodico
import ar.com.guada.registropesadasmobile.sync.sincronizarAhora
import ar.com.guada.registropesadasmobile.ui.PantallaConexionBluetooth
import ar.com.guada.registropesadasmobile.ui.PantallaLogin
import ar.com.guada.registropesadasmobile.ui.PantallaRegistroPesada
import ar.com.guada.registropesadasmobile.ui.theme.RegistroPesadasMobileTheme
import kotlinx.coroutines.launch
import ar.com.guada.registropesadasmobile.data.AnimalRepository
import ar.com.guada.registropesadasmobile.data.AppDatabase
import ar.com.guada.registropesadasmobile.data.ResultadoSyncAnimales
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
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val sesionRepository = remember { SesionRepository(context) }
                val animalRepository = remember {
                    AnimalRepository(
                        AppDatabase.obtenerInstancia(context).animalDao(),
                        RetrofitCliente.animalApi,
                        RetrofitCliente.obtenerSesionRepository()
                    )
                }

                // null = "todavía no sabemos" (cargando desde DataStore).
                // Una vez que DataStore responde la primera vez, pasa a
                // tener un valor real: SesionInfo si hay sesión, o un
                // Unit especial para decir "confirmado: no hay sesión".
                var sesion by remember { mutableStateOf<SesionInfo?>(null) }
                var sesionCargada by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    sesionRepository.sesion.collect {
                        sesion = it
                        sesionCargada = true
                    }
                }

                val lectorBalanza = remember { LectorBalanzaClassic(scope, context) }
                val estadoConexion by lectorBalanza.estadoConexion.collectAsState()
                var pantallaActual by remember { mutableStateOf(Pantalla.PRINCIPAL) }
                val snackbarHostState = remember { SnackbarHostState() }
                var actualizandoAnimales by remember { mutableStateOf(false) }
                val sincronizarAnimales: () -> Unit = {
                    if (!actualizandoAnimales) {
                        scope.launch {
                            actualizandoAnimales = true
                            val resultado = animalRepository.sincronizar()
                            actualizandoAnimales = false
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
                                onLoginExitoso = { sincronizarAnimales() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        else -> {
                            when (pantallaActual) {
                                Pantalla.PRINCIPAL -> PantallaRegistroPesada(
                                    lectorBalanza = lectorBalanza,
                                    estadoConexion = estadoConexion,
                                    onIrAConexion = { pantallaActual = Pantalla.CONEXION_BLUETOOTH },
                                    onCerrarSesion = {
                                        scope.launch { sesionRepository.cerrarSesion() }
                                    },
                                    onActualizarAnimales = sincronizarAnimales,
                                    actualizandoAnimales = actualizandoAnimales,
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
    }
}