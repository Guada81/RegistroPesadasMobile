package ar.com.guada.registropesadasmobile.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ar.com.guada.registropesadasmobile.data.AnimalLocal

@Composable
fun PantallaSeleccionAnimal(
    animales: List<AnimalLocal>,
    onAnimalElegido: (AnimalLocal) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Seleccioná un animal:", style = MaterialTheme.typography.titleMedium)

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(animales) { animal ->
                Button(
                    onClick = { onAnimalElegido(animal) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text("Caravana: ${animal.caravana}")
                }
            }
        }
    }
}