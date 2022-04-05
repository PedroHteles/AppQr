package br.com.arcom.scanner.api.model

import com.google.gson.annotations.SerializedName;

data class Box(
    @field:SerializedName("Id") val id:Long?,
    @field:SerializedName("Box") val box:String?,
)