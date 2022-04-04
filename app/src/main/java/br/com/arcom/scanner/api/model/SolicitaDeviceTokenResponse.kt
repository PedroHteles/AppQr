package br.com.arcom.scanner.api.model

import com.google.gson.annotations.SerializedName

data class SolicitaDeviceTokenResponse(
    @field:SerializedName("token") val token: String,
    @field:SerializedName("nomeUsuario") val nomeUsuario: String,
    @field:SerializedName("idUsuario") val idUsuario: Int,
)
