package ar.com.guada.registropesadasmobile.ui

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ar.com.guada.registropesadasmobile.hardware.EstadoConexion
import ar.com.guada.registropesadasmobile.hardware.LectorBalanzaClassic
import kotlinx.coroutines.launch

@Composable
fun PantallaConexionBluetooth(
    lectorBalanza: LectorBalanzaClassic,
    onConectado: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val estadoConexion by lectorBalanza.estadoConexion.collectAsState()

    var tienePermiso by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) ==
                        PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    var dispositivosEmparejados by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var conectando by remember { mutableStateOf(false) }

    val permisoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultados ->
        tienePermiso = resultados.values.all { it }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("Conexión Bluetooth", style = MaterialTheme.typography.titleLarge)
        Text(
            when (estadoConexion) {
                EstadoConexion.CONECTADO -> "Estado: 🔵 Conectado"
                EstadoConexion.ERROR -> "Estado: ⚠ Conexión perdida"
                EstadoConexion.DESCONECTADO -> "Estado: ⚪ Sin conectar"
            },
            style = MaterialTheme.typography.bodyMedium
        )

        TextButton(onClick = onVolver) {
            Text("← Volver")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!tienePermiso) {
            Button(onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    permisoLauncher.launch(
                        arrayOf(
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN
                        )
                    )
                }
            }) {
                Text("Pedir permiso de Bluetooth")
            }
        } else {
            Button(onClick = {
                val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
                dispositivosEmparejados = bluetoothManager?.adapter?.bondedDevices
                    ?.map { it.name to it.address } ?: emptyList()
            }) {
                Text("Buscar dispositivos emparejados")
            }

            Spacer(modifier = Modifier.height(8.dp))

            dispositivosEmparejados.forEach { (nombre, mac) ->
                Button(onClick = {
                    conectando = true
                    scope.launch {
                        try {
                            lectorBalanza.conectar(mac)   // ya no hace falta withContext(Dispatchers.IO) acá
                            onConectado()
                        } catch (e: Exception) {
                            mensajeError = "Error de balanza: ${e.message}"
                        } finally {
                            conectando = false
                        }
                    }
                }) {
                    Text("Conectar a $nombre")
                }
            }

            if (conectando) {
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator()
            }

            mensajeError?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}