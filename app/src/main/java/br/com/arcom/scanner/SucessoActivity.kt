package br.com.arcom.scanner

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import br.com.arcom.scanner.databinding.ActivitySucessoBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SucessoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySucessoBinding
    private lateinit var viewModel: ScannerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[ScannerViewModel::class.java]
        setContentView(R.layout.activity_sucesso)
        binding = ActivitySucessoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnCerto.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onBackPressed() {
// super.onBackPressed();
// Not calling **super**, disables back button in current screen.
    }
}