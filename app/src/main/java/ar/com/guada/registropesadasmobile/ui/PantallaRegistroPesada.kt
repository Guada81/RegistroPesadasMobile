package ar.com.guada.registropesadasmobile.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import ar.com.guada.registropesadasmobile.PESO_MAXIMO_KG
import ar.com.guada.registropesadasmobile.PESO_MINIMO_KG
import ar.com.guada.registropesadasmobile.data.AnimalLocal
import ar.com.guada.registropesadasmobile.data.AppDatabase
import ar.com.guada.registropesadasmobile.data.PesadaLocal
import ar.com.guada.registropesadasmobile.hardware.EstadoConexion
import ar.com.guada.registropesadasmobile.hardware.LectorBalanzaClassic
import ar.com.guada.registropesadasmobile.hardware.LectorCaravanaManual
import ar.com.guada.registropesadasmobile.hardware.PesoLeido
import ar.com.guada.registropesadasmobile.hardware.parsearTramaBalanza
import ar.com.guada.registropesadasmobile.pesoFueraDeRango
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun PantallaRegistroPesada(
    lectorBalanza: LectorBalanzaClassic,
    estadoConexion: EstadoConexion,
    onIrAConexion: () -> Unit,
    onCerrarSesion: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.obtenerInstancia(context) }

    // --- Estado de la balanza ---
    var pesoActual by remember { mutableStateOf<PesoLeido?>(null) }

    // --- Estado del animal ---
    var animales by remember { mutableStateOf(listOf<AnimalLocal>()) }
    val lectorAnimal = remember { LectorCaravanaManual() }
    var animalSeleccionado by remember { mutableStateOf<AnimalLocal?>(null) }
    var mostrarSelectorAnimal by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        database.animalDao().obtenerTodos().collect { lista -> animales = lista }
    }

    LaunchedEffect(Unit) {
        lectorAnimal.datosRecibidos.collect { idRecibido ->
            animalSeleccionado = animales.find { it.id.toString() == idRecibido }
        }
    }

    LaunchedEffect(estadoConexion) {
        if (estadoConexion == EstadoConexion.CONECTADO) {
            lectorBalanza.datosRecibidos.collect { linea ->
                val peso = parsearTramaBalanza(linea)
                if (peso == null) {
                    Log.d("RegistroPesada", "Trama no parseada: '$linea'")
                }
                pesoActual = peso
            }
        }
    }

    val puedeGuardar = pesoActual?.estable == true && animalSeleccionado != null

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("1. Peso", style = MaterialTheme.typography.titleMedium)
            Row {
                TextButton(onClick = onIrAConexion) {
                    val (texto, color) = when (estadoConexion) {
                        EstadoConexion.CONECTADO -> "🔵 Conectado" to MaterialTheme.colorScheme.primary
                        EstadoConexion.ERROR -> "⚠ Conexión perdida" to MaterialTheme.colorScheme.error
                        EstadoConexion.DESCONECTADO -> "⚪ Sin conectar" to MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(texto, color = color)
                }
                TextButton(onClick = onCerrarSesion) {
                    Text("Salir")
                }
            }
        }

        if (estadoConexion == EstadoConexion.CONECTADO) {
            pesoActual?.let { peso ->
                Text(
                    "${peso.pesoTexto} kg — ${if (peso.estable) "ESTABLE" else "inestable"}",
                    style = MaterialTheme.typography.headlineSmall
                )
                if (pesoFueraDeRango(peso.pesoKg)) {
                    Text(
                        "⚠ Peso fuera del rango esperado (${PESO_MINIMO_KG}–${PESO_MAXIMO_KG} kg)",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Sección 2: Animal ---
        Text("2. Animal", style = MaterialTheme.typography.titleMedium)
        Button(onClick = { mostrarSelectorAnimal = true }) {
            Text(animalSeleccionado?.let { "Caravana: ${it.caravana}" } ?: "Elegir animal")
        }

        if (mostrarSelectorAnimal) {
            ModalBottomSheet(onDismissRequest = { mostrarSelectorAnimal = false }) {
                PantallaSeleccionAnimal(
                    animales = animales,
                    onAnimalElegido = { animal ->
                        animalSeleccionado = animal
                        mostrarSelectorAnimal = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Sección 3: Guardar ---
        Button(
            onClick = {
                val peso = pesoActual ?: return@Button
                scope.launch {
                    val pesada = PesadaLocal(
                        animalId = animalSeleccionado?.id,
                        caravanaDesconocida = null,
                        pesoKg = peso.pesoKg
                    )
                    database.pesadaDao().insertar(pesada)
                    snackbarHostState.showSnackbar("Pesada guardada: ${pesada.pesoKg} kg")
                    animalSeleccionado = null
                }
            },
            enabled = puedeGuardar
        ) {
            Text("Guardar pesada")
        }
    }
}