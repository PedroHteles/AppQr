package br.com.arcom.scanner.util

import android.app.Activity
import android.content.Context
import com.google.zxing.integration.android.IntentIntegrator

fun initScanner(context: Activity) {
    val integrator = IntentIntegrator(context)
    integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
    integrator.setPrompt("Aponte para um QR CODE para fazer a leitura.")
    integrator.initiateScan()
}