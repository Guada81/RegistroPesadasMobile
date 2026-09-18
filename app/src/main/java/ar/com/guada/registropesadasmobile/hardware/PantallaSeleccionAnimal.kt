package ar.com.guada.registropesadasmobile.hardware

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
import kotlinx.coroutines.launch

@Composable
fun PantallaSeleccionAnimal(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val database = remember { AppDatabase.obtenerInstancia(context) }
    val lector = remember { LectorCaravanaManual() }

    var animales by remember { mutableStateOf(listOf<AnimalLocal>()) }
    var animalSeleccionado by remember { mutableStateOf<AnimalLocal?>(null) }

    LaunchedEffect(Unit) {
        database.animalDao().obtenerTodos().collect { lista ->
            animales = lista
        }
    }

    // Escuchamos el Flow del lector, igual que hacíamos con la balanza.
    // Acá es donde, en el futuro, se conectará la lógica de guardado
    // de la pesada (ver un id de animal confirmado y disparar el resto
    // del flujo), sin que a esa lógica le importe de dónde vino el dato.
    LaunchedEffect(Unit) {
        lector.datosRecibidos.collect { idRecibido ->
            animalSeleccionado = animales.find { it.id.toString() == idRecibido }
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Seleccioná un animal:", style = MaterialTheme.typography.titleMedium)

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(animales) { animal ->
                Button(
                    onClick = {
                        scope.launch {
                            lector.seleccionarAnimal(animal.id)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text("Caravana: ${animal.caravana}")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        animalSeleccionado?.let { animal ->
            Text(
                "Seleccionado: ${animal.caravana} (id=${animal.id})",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}