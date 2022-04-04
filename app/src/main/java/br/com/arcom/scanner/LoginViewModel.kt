package br.com.arcom.scanner

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.arcom.scanner.api.data.repository.ScannerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import br.com.arcom.scanner.util.Result

@HiltViewModel
class LoginViewModel
@Inject internal constructor(
    val scannerRepository: ScannerRepository,
    @Named("auth") val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _status = MutableLiveData<Result>()
    val status: LiveData<Result> = _status


    fun verificaToken() {
        viewModelScope.launch {
            val token = sharedPreferences.getString("token", null)
            if (token != null) {
                _status.value = Result.Token
            }
        }
    }

    fun logar(idUsuario: Int, senha: String) {
        viewModelScope.launch {
            _status.value = Result.Loading
            try {
                val user = scannerRepository.buscarToken(idUsuario = idUsuario, senha = senha)
                val editor = sharedPreferences.edit()
                editor.putString("token", user.token)
                editor.putString("nomeUsuario", user.nomeUsuario)
                editor.putInt("idUsuario", user.idUsuario)
                editor.apply()
                _status.value = Result.Ok
            } catch (e: Exception) {
                _status.value = Result.Error(e)
            }
        }
    }
}