package br.com.arcom.scanner.util

sealed class Acao {
    object Inicio : Acao()
    object Fim : Acao()
}