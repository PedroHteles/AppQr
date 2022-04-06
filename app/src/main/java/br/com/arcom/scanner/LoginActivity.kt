package br.com.arcom.scanner

import br.com.arcom.scanner.util.Result
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import br.com.arcom.scanner.databinding.LoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        setContentView(R.layout.login)
        binding = LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val idUsuario = binding.inputMatricula
        val senha = binding.inputSenha
        viewModel.verificaToken()
        binding.btnLogin.setOnClickListener {
            if (idUsuario.text != null && senha.text != null) {
                viewModel.logar(idUsuario.text.toString().toInt(), senha.text.toString())
            } else {
                Toast.makeText(
                    this,
                    "Erro ao processar a requisicao $it!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        viewModel.status.observe(this) {

            fun Intent.clearStack() {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }


            when (it) {
                is Result.Ok -> {
                    Toast.makeText(this, "Saldo com sucesso!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    intent.clearStack()
                }
                is Result.Error -> {
                    Toast.makeText(
                        this,
                        "Erro ao processar a requisicao $it!",
                        Toast.LENGTH_LONG
                    ).show()
                    // desabilitar o loading progress
                }
                is Result.Loading -> {
                }// habilitar o progress
                is Result.Token -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    intent.clearStack()
                }
            }
        }
    }

}