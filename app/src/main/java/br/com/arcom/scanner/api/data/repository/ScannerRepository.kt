package br.com.arcom.scanner.api.data.repository


import br.com.arcom.scanner.api.data.ApiService
import br.com.arcom.scanner.api.model.SolicitaDeviceTokenRequest
import br.com.arcom.scanner.api.model.SolicitaDeviceTokenResponse
import retrofit2.Call
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScannerRepository @Inject constructor() {

    suspend fun buscarToken(idUsuario:Int, senha:String): SolicitaDeviceTokenResponse {
        val service = ApiService.create(null)
        val token = service.createDeviceToken(SolicitaDeviceTokenRequest(idUsuario = idUsuario, senha = senha))
        return token
    }
    suspend fun liberarBox(box:String, usuario:Long,token:String) {
        val service = ApiService.create(token)
        service.liberarBox(box, usuario)

    }

}