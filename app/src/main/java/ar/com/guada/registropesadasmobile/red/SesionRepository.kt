package ar.com.guada.registropesadasmobile.red

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Extensión de Context: crea (una sola vez, la primera vez que se usa)
// el archivo físico donde DataStore guarda los datos.
private val Context.sesionDataStore by preferencesDataStore(name = "sesion")

data class SesionInfo(
    val token: String,
    val username: String,
    val rol: String
)

class SesionRepository(private val context: Context) {

    private object Claves {
        val TOKEN = stringPreferencesKey("token")
        val USERNAME = stringPreferencesKey("username")
        val ROL = stringPreferencesKey("rol")
    }

    // Flow: cualquiera que lo observe se entera automáticamente si la
    // sesión cambia (por ejemplo, al hacer login o logout), sin sondear.
    val sesion: Flow<SesionInfo?> = context.sesionDataStore.data.map { prefs ->
        val token = prefs[Claves.TOKEN]
        val username = prefs[Claves.USERNAME]
        val rol = prefs[Claves.ROL]
        if (token != null && username != null && rol != null) {
            SesionInfo(token, username, rol)
        } else {
            null
        }
    }

    suspend fun guardarSesion(sesion: SesionInfo) {
        context.sesionDataStore.edit { prefs ->
            prefs[Claves.TOKEN] = sesion.token
            prefs[Claves.USERNAME] = sesion.username
            prefs[Claves.ROL] = sesion.rol
        }
    }

    suspend fun cerrarSesion() {
        context.sesionDataStore.edit { prefs -> prefs.clear() }
    }

    // Útil para el interceptor de Retrofit que agrega el header
    // "Authorization" a cada request (lo armamos en el próximo paso).
    suspend fun obtenerTokenActual(): String? {
        return sesion.map { it?.token }.first()
    }
}