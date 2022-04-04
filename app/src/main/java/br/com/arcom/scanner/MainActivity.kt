package br.com.arcom.scanner
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import br.com.arcom.scanner.api.model.Box
import br.com.arcom.scanner.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.ViewModelProvider
import br.com.arcom.scanner.util.Acao
import br.com.arcom.scanner.util.Result


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ScannerViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnScan.setOnClickListener { initScanner() }
        viewModel = ViewModelProvider(this)[ScannerViewModel::class.java]
        viewModel.verificaBox()
        viewModel.dadosUsuario.observe(this) {
            binding.nomeUsuario.text = it
        }
        viewModel.acao.observe(this) {
            when(it) {
                is Acao.Inicio -> {
                    binding.btnScan.text = "Terminar tarefa"
                }
                is Acao.Fim -> {
                    binding.btnScan.text = "Iniciar nova tarefa"
                }

            }
        }



        viewModel.status.observe(this) {
            fun Intent.clearStack() {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            when(it) {
                is Result.Ok -> {
                    Toast.makeText(this, "Tarefa iniciada!", Toast.LENGTH_SHORT).show()
                    binding.btnScan.text = "Terminar tarefa"
                }
                is Result.Finalizada -> {
                    Toast.makeText(this, "Tarefa Finalizada!", Toast.LENGTH_SHORT).show()
                    binding.btnScan.text = "Iniciar nova tarefa"
                }
                is Result.Error -> {
                    Toast.makeText(this, "Erro ao processar a requisicao $it!", Toast.LENGTH_LONG).show()
                    // desabilitar o loading progress
                }
                is Result.Loading -> { }// habilitar o progress
                is Result.Token -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    intent.clearStack()
                }
            }
        }
    }

    private fun initScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Aponte para um QR CODE para fazer a leitura.")
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        val UPCScanned = scanResult.contents
        if (UPCScanned != null) {
            openDialog(UPCScanned)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun openDialog(result: String) {
        try {
            val list = Gson().fromJson(result, Box::class.java)
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.dialog)
            val texto = dialog.findViewById(R.id.numero_box) as TextView
            texto.text = list.box
            if (!list.box.isNullOrEmpty()) {
                dialog.show()
                var btnConfirma = dialog.findViewById(R.id.btn_confirmar) as Button
                var loading = dialog.findViewById(R.id.loading) as ProgressBar
                btnConfirma.setOnClickListener{
                    btnConfirma.visibility = View.GONE
                    loading.visibility = View.VISIBLE
                    viewModel.liberarBox(list.box) { dialog.dismiss() }

                }
            } else {
                throw error("erro na leitura")
            }

        } catch (e: Exception) {
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.dialog_erro)
            val text = dialog.findViewById(R.id.texError) as TextView
            text.text = "Erro Na leitura do qrCode"
            dialog.show()
            var btn = dialog.findViewById(R.id.btn_releitura) as Button
            btn.setOnClickListener {
                dialog.dismiss()
                initScanner()
            }

        }
    }

}
