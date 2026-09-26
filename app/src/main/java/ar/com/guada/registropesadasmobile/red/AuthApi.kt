package ar.com.guada.registropesadasmobile.red

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/v1/token/")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}