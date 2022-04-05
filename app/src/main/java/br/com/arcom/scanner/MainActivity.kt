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
import com.google.android.material.button.MaterialButton





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
                    binding.icLogosvg.visibility = View.INVISIBLE
                    binding.txtBox.text = "Carregamento em andamento Box: " + "\n"+
                            "${viewModel.boxShared()}"
                    binding.txtBox.visibility = View.VISIBLE
                    binding.bemVindo.visibility = View.INVISIBLE

                }
                is Acao.Fim -> {
                    binding.btnScan.text = "Iniciar nova tarefa"
                    binding.txtBox.visibility = View.INVISIBLE
                    binding.icLogosvg.visibility = View.VISIBLE
                    binding.bemVindo.visibility = View.INVISIBLE

                }
                is Acao.Finalizada -> {
                    binding.btnScan.text = "Ler Qr Code"
                    binding.icLogosvg.visibility = View.VISIBLE
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
                    val intentSucess = Intent(this, SucessoActivity::class.java)
                    startActivity(intentSucess)
                    intentSucess.clearStack()
//                    binding.btnScan.text = "Iniciar nova tarefa"
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
        val list = Gson().fromJson(UPCScanned, Box::class.java)
        if (UPCScanned != null) {
            if(viewModel.boxShared() != "" && list.box != viewModel.boxShared()){
                openDialogErro()
            }else{
                openDialog(UPCScanned)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun openDialogErro() {

        val dialogError = Dialog(this)
        dialogError.setContentView(R.layout.erro_box)
        val texto = dialogError.findViewById(R.id.txt_box_nao_finalizada) as TextView
        val btn = dialogError.findViewById(R.id.btn_fechar) as MaterialButton
        texto.text = resources.getString(R.string.box_nao_finalizada, viewModel.boxShared())
        btn.setOnClickListener{
            dialogError.dismiss()
        }
        dialogError.show()

    }


    private fun openDialog(result: String) {
      
            val list = Gson().fromJson(result, Box::class.java)
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.dialog)
            val texto = dialog.findViewById(R.id.numero_box) as TextView
            val titulo = dialog.findViewById(R.id.txt_titulo) as TextView

        texto.text = list.box
            if (!list.box.isNullOrEmpty()) {
                if(viewModel.boxShared() != ""){
                    titulo.text = "Deseja Finalizar tarefa Box: " + "\n" + "${list.box}"
                }else{
                    titulo.text = "Deseja Iniciar uma nova tarefa Box: " + "\n" + "${list.box}"
                }

                dialog.show()
                var btnConfirma = dialog.findViewById(R.id.btn_confirmar) as Button
                var loading = dialog.findViewById(R.id.loading) as ProgressBar
                btnConfirma.setOnClickListener{
                    btnConfirma.visibility = View.GONE
                    loading.visibility = View.VISIBLE
                    viewModel.liberarBox(list.box) {
                        dialog.dismiss()
                    }

                }
            } else {
                throw error("erro na leitura")
            }


    }

}
