package br.com.arcom.scanner.api.model

import com.google.gson.annotations.SerializedName


data class LiberaBoxRequest (
    @field:SerializedName("idUsuario") val idUsuario: Int,
    @field:SerializedName("box") val box: String?,

    )