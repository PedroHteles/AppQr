package br.com.arcom.scanner

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.arcom.scanner.api.data.repository.ScannerRepository
import br.com.arcom.scanner.util.Acao
import br.com.arcom.scanner.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named


@HiltViewModel
class ScannerViewModel
@Inject internal constructor(
    val scannerRepository: ScannerRepository,
    @Named("auth") val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _status = MutableLiveData<Result>()
    val status: LiveData<Result> = _status


    private val _acao = MutableLiveData<Acao>()
    val acao: LiveData<Acao> = _acao

    private val _dadosUsuario = MutableLiveData<String>()
    val dadosUsuario: LiveData<String> = _dadosUsuario


    fun boxShared():String{
        return sharedPreferences.getString("box", "")!!
    }


    init {
        val nomeUsuario = sharedPreferences.getString("nomeUsuario", "")
        _dadosUsuario.value = nomeUsuario!!
    }

    fun deslogar() {
        sharedPreferences.edit().clear().apply()
        _status.value = Result.Unauthorized
    }

    fun verificaBox() {
        val boxShared = sharedPreferences.getString("box", "")
        if (boxShared != "") {
            _acao.value = Acao.Inicio
        }
    }

    fun observerAcao(acaoValue: Acao): String {
        return when (acaoValue) {
            Acao.Inicio -> "inicio"
            Acao.Fim -> "fim"
        }
    }

    fun liberarBox(box: String, dismiss: () -> Unit) {
        viewModelScope.launch {
            _status.value = Result.Loading

            try {
                val idUsuario = sharedPreferences.getInt("idUsuario", 0).toLong()
                val boxShared = sharedPreferences.getString("box", "")
                val token = sharedPreferences.getString("token", "")
                val editor = sharedPreferences.edit()

                if (boxShared == "") {
                    try {
//                        scannerRepository.liberarBox(
//                            usuario = idUsuario,
//                            box = box,
//                            acao = observerAcao(Acao.Inicio),
//                            token = token!!
//                        )
                        editor.putString("box", box).apply()
                        _acao.value = Acao.Inicio
                        _status.value = Result.Ok

                    } catch (e: Exception) {
                        editor.putString("box", "").apply()
                        _status.value = Result.Error(e)
                    }
                } else {
                    if (boxShared == box && _acao.value == Acao.Inicio) {
                        try {
                            scannerRepository.liberarBox(
                                usuario = idUsuario,
                                box = box,
                                situacao = "D",
                                token = token!!
                            )
                            editor.putString("box", "").apply()
                            _acao.value = Acao.Fim

                        } catch (e: Exception) {
                            _status.value = Result.Error(e)
                        }
                    }
                }

            } catch (e: Exception) {
                print("DEU ERRO AQUI ${e.message}")
            } finally {
                dismiss()

            }
        }
    }
}