package ar.com.guada.registropesadasmobile.red

import retrofit2.Response
import retrofit2.http.GET

interface AnimalApi {
    @GET("api/v1/animales/")
    suspend fun obtenerAnimales(): Response<List<AnimalResponse>>
}