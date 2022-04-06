package br.com.arcom.scanner.api.data
import br.com.arcom.scanner.api.model.LiberaBoxRequest
import br.com.arcom.scanner.api.model.SolicitaDeviceTokenRequest
import br.com.arcom.scanner.api.model.SolicitaDeviceTokenResponse
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import okhttp3.ResponseBody

import retrofit2.http.GET






interface ApiService{
    @POST("api/seguranca/v1/login")
    suspend fun createDeviceToken(
        @Body solicitaDeviceTokenRequest: SolicitaDeviceTokenRequest
    ): SolicitaDeviceTokenResponse


    @POST("api/estoque/v1/liberacao-box-carregamento")
    suspend fun liberarBox(@Query("box")  box: String?,@Query("usuario")  usuario: Long?,@Query("acao")  acao: String?)

    companion object {
        const val BASE_URL = "http://f9d3-189-112-215-169.ngrok.io "

        fun create(token: String?): ApiService {
            val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC}
            val gson = GsonBuilder().setLenient().setDateFormat("yyyy-MM-dd").create()

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .addInterceptor(Interceptor {
                    val request: Request = it.request()
                    val builder = request.newBuilder()
                    if( token != null ) builder.addHeader("Authorization", "Bearer " + token)
                    it.proceed(builder.build())
                })
                .build()


            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService::class.java)
        }
    }

}

