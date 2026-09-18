package ar.com.guada.registropesadasmobile.hardware

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ar.com.guada.registropesadasmobile.data.AnimalLocal
import ar.com.guada.registropesadasmobile.data.AppDatabase
import ar.com.guada.registropesadasmobile.data.PesadaLocal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ar.com.guada.registropesadasmobile.PESO_MINIMO_KG
import ar.com.guada.registropesadasmobile.PESO_MAXIMO_KG
import ar.com.guada.registropesadasmobile.pesoFueraDeRango

@SuppressLint("MissingPermission")
@Composable
fun PantallaRegistroPesada(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.obtenerInstancia(context) }

    // --- Estado de la balanza ---
    var tienePermiso by remember { mutableStateOf(false) }
    var dispositivosEmparejados by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    val lectorBalanza = remember { LectorBalanzaClassic(scope, context) }
    var pesoActual by remember { mutableStateOf<PesoLeido?>(null) }

    // --- Estado del animal ---
    var animales by remember { mutableStateOf(listOf<AnimalLocal>()) }
    val lectorAnimal = remember { LectorCaravanaManual() }
    var animalSeleccionado by remember { mutableStateOf<AnimalLocal?>(null) }

    // --- Estado de guardado ---
    var mensajeGuardado by remember { mutableStateOf("") }

    val permisoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultados ->
        tienePermiso = resultados.values.all { it }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            tienePermiso = true
        }
        database.animalDao().obtenerTodos().collect { lista -> animales = lista }
    }

    LaunchedEffect(Unit) {
        lectorAnimal.datosRecibidos.collect { idRecibido ->
            animalSeleccionado = animales.find { it.id.toString() == idRecibido }
        }
    }

    // El botón "Guardar" solo se habilita con las dos condiciones de la
    // regla de negocio: peso estable Y animal identificado.
    val puedeGuardar = pesoActual?.estable == true && animalSeleccionado != null

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        // --- Sección 1: Balanza ---
        Text("1. Peso", style = MaterialTheme.typography.titleMedium)
        if (!tienePermiso) {
            Button(onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    permisoLauncher.launch(
                        arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
                    )
                }
            }) {
                Text("Pedir permiso de Bluetooth")
            }
        } else {
            if (dispositivosEmparejados.isEmpty()) {
                Button(onClick = {
                    val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
                    dispositivosEmparejados = bluetoothManager?.adapter?.bondedDevices
                        ?.map { it.name to it.address } ?: emptyList()
                }) {
                    Text("Buscar dispositivos emparejados")
                }
            } else {
                dispositivosEmparejados.forEach { (nombre, mac) ->
                    Button(onClick = {
                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) { lectorBalanza.conectar(mac) }
                                lectorBalanza.datosRecibidos.collect { linea ->
                                    val peso = parsearTramaBalanza(linea)
                                    if (peso == null) {
                                        Log.d("RegistroPesada", "Trama no parseada: '$linea'")
                                    }
                                    pesoActual = peso
                                }
                            } catch (e: Exception) {
                                mensajeGuardado = "Error de balanza: ${e.message}"
                            }
                        }
                    }) {
                        Text("Conectar a $nombre")
                    }
                }
            }

            pesoActual?.let { peso ->
                Text(
                    "${peso.pesoTexto} kg — ${if (peso.estable) "ESTABLE" else "inestable"}",
                    style = MaterialTheme.typography.headlineSmall
                )
                if (pesoFueraDeRango(peso.pesoKg)) {
                    Text(
                        "⚠ Peso fuera del rango esperado ($PESO_MINIMO_KG–$PESO_MAXIMO_KG kg)",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Sección 2: Animal ---
        Text("2. Animal", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(animales) { animal ->
                Button(
                    onClick = { scope.launch { lectorAnimal.seleccionarAnimal(animal.id) } },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                ) {
                    Text("Caravana: ${animal.caravana}")
                }
            }
        }
        animalSeleccionado?.let { animal ->
            Text("Seleccionado: ${animal.caravana}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Sección 3: Guardar ---
        Button(
            onClick = {
                scope.launch {
                    val pesada = PesadaLocal(
                        animalId = animalSeleccionado?.id,
                        caravanaDesconocida = null,
                        pesoKg = pesoActual!!.pesoKg
                    )
                    database.pesadaDao().insertar(pesada)
                    mensajeGuardado = "Pesada guardada: ${pesada.pesoKg} kg"
                    animalSeleccionado = null
                }
            },
            enabled = puedeGuardar
        ) {
            Text("Guardar pesada")
        }

        if (mensajeGuardado.isNotEmpty()) {
            Text(mensajeGuardado, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

