package br.com.arcom.scanner
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import br.com.arcom.scanner.api.model.Box
import br.com.arcom.scanner.databinding.ActivityMainBinding
import br.com.arcom.scanner.util.Result
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import dagger.hilt.android.AndroidEntryPoint


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
        binding.btnDeslogar.setOnClickListener {
            viewModel.deslogar()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        viewModel = ViewModelProvider(this)[ScannerViewModel::class.java]
        viewModel.verificaBox()
        viewModel.dadosUsuario.observe(this) {
            binding.nomeUsuario.text = it
        }


        viewModel.status.observe(this) {
            fun Intent.clearStack() {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            when(it) {
                is Result.Finalizada -> {
//                    Toast.makeText(this, "Tarefa Finalizada!", Toast.LENGTH_SHORT).show()
                    binding.btnScan.text = "Iniciar nova tarefa"
                    val intent = Intent(this, SucessoActivity::class.java)
                    startActivity(intent)
                }
                is Result.Error -> {
//                    Toast.makeText(this, "Erro ao processar a requisicao $it!", Toast.LENGTH_LONG).show()
                    openDialogErro(viewModel.boxShared())
                // desabilitar o loading progress
                }
                is Result.Loading -> { }// habilitar o progress
                is Result.Token -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    intent.clearStack()
                }
                is Result.Unauthorized -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onBackPressed() {
// super.onBackPressed();
// Not calling **super**, disables back button in current screen.
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

    fun openDialogErro(result: String) {

        val dialogError = Dialog(this)
        dialogError.setCancelable(false)
        dialogError.setContentView(R.layout.erro_box)
        val texto = dialogError.findViewById(R.id.numero_box) as TextView
        val btn = dialogError.findViewById(R.id.btn_fechar) as MaterialButton
        texto.text = result
        btn.setOnClickListener{
            dialogError.dismiss()
        }
        dialogError.show()
    }


    private fun openDialog(result: String) {
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.dialog)

            try {
                val list = Gson().fromJson(result, Box::class.java)
                val texto = dialog.findViewById(R.id.numero_box) as TextView
                texto.text = list.box

                if (!list.box.isNullOrEmpty()) {

                    dialog.show()
                    var btnConfirma = dialog.findViewById(R.id.btn_confirmar) as Button
                    var btnCancelar = dialog.findViewById(R.id.btn_fechar) as Button

                    btnCancelar.setOnClickListener{
                        dialog.dismiss()
                    }

                    var loading = dialog.findViewById(R.id.loading) as ProgressBar
                    btnConfirma.setOnClickListener{
                        btnConfirma.visibility = View.GONE
                        btnCancelar.visibility = View.GONE
                        loading.visibility = View.VISIBLE
                        viewModel.liberarBox(list.box) { dialog.dismiss() }

                    }
                } else {
                    throw error("erro na leitura")
                }

            }catch (e: Exception){
                Toast.makeText(this, "Erro ao processar Qr code Tente novamente", Toast.LENGTH_LONG).show()

            }
    }

}
