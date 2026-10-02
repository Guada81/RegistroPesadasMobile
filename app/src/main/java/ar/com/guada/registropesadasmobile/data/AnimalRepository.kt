package ar.com.guada.registropesadasmobile.data

import android.util.Log
import ar.com.guada.registropesadasmobile.red.AnimalApi
import ar.com.guada.registropesadasmobile.red.AnimalResponse
import ar.com.guada.registropesadasmobile.red.SesionRepository
import java.io.IOException

enum class ResultadoSyncAnimales {
    OK,
    SIN_CONEXION,
    SESION_EXPIRADA,
    ERROR_SERVIDOR
}

class AnimalRepository(
    private val animalDao: AnimalDao,
    private val animalApi: AnimalApi,
    private val sesionRepository: SesionRepository
) {

    suspend fun sincronizar(): ResultadoSyncAnimales {
        return try {
            val response = animalApi.obtenerAnimales()

            when {
                response.isSuccessful -> {
                    val body = response.body() ?: return ResultadoSyncAnimales.ERROR_SERVIDOR
                    animalDao.insertarTodos(body.map { it.toLocal() })
                    ResultadoSyncAnimales.OK
                }
                response.code() == 401 -> {
                    sesionRepository.cerrarSesion()
                    ResultadoSyncAnimales.SESION_EXPIRADA
                }
                else -> ResultadoSyncAnimales.ERROR_SERVIDOR
            }
        } catch (e: IOException) {
            Log.e("AnimalRepository", "Error de red al sincronizar animales", e)
            ResultadoSyncAnimales.SIN_CONEXION
        }
    }

    private fun AnimalResponse.toLocal() = AnimalLocal(
        id = id,
        caravana = nro_identificacion,
        activo = activo,
        sexo = sexo
    )
}
