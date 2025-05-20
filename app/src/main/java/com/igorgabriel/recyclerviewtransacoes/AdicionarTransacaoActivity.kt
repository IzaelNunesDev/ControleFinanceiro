package com.igorgabriel.recyclerviewtransacoes

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.igorgabriel.recyclerviewtransacoes.databinding.ActivityAdicionarTransacaoBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AdicionarTransacaoActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAdicionarTransacaoBinding.inflate(layoutInflater)
    }

    private val autenticacao by lazy {
        FirebaseAuth.getInstance()
    }

    private val bancoDados by lazy {
        FirebaseFirestore.getInstance()
    }

    private var selectedTimestamp: Timestamp? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Inicializar data com o dia atual
        selectedTimestamp = Timestamp(Date())
        binding.textData.text = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date())

        binding.ivMostrarCalendario.setOnClickListener {
            val calendario = Calendar.getInstance()
            val ano = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, anoSelecionado, mesSelecionado, diaSelecionado ->
                    val dataSelecionada = Calendar.getInstance()
                    dataSelecionada.set(anoSelecionado, mesSelecionado, diaSelecionado)
                    selectedTimestamp = Timestamp(dataSelecionada.time)
                    binding.textData.text = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(dataSelecionada.time)
                },
                ano,
                mes,
                dia
            )
            datePickerDialog.show()
        }

        binding.radioTipo.setOnCheckedChangeListener { group, checkedId ->
            if(checkedId != -1){
                val opcaoSelecionada = findViewById<RadioButton>(checkedId)

                binding.btnAdicionarTransacao.setOnClickListener {
                    val descricao = binding.editAddDescricao.text.toString()
                    val categoria = binding.editAddCategoria.text.toString()
                    val valorString = binding.editAddValor.text.toString()
                    val tipo = opcaoSelecionada.text.toString()

                    val valor = valorString.toDoubleOrNull()
                    if (valor == null) {
                        exibirMensagem("Valor inválido. Por favor, insira um número.")
                        return@setOnClickListener
                    }

                    if (selectedTimestamp != null){
                        adicionarTransacao(descricao, categoria, valor, tipo, selectedTimestamp!!)
                    } else {
                        exibirMensagem("Por favor, escolha uma data.")
                    }
                }
            }
        }
    }

    private fun adicionarTransacao(
        descricao: String,
        categoria: String,
        valor: Double,
        tipo: String,
        data: Timestamp
    ) {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        if (idUsuarioLogado != null) {
            val dados = mapOf(
                "descricao" to descricao,
                "categoria" to categoria,
                "valor" to valor,
                "tipo" to tipo,
                "data" to data
            )

            val referenciaUsuario = bancoDados
                .collection("usuarios/${idUsuarioLogado}/transacoes")

            referenciaUsuario
                .add(dados)
                .addOnSuccessListener {
                    exibirMensagem("Transacao adicionada com sucesso")
                    finish()
                }.addOnFailureListener { exception ->
                    exibirMensagem("Falha ao adicionar transacao")
                }
        }
    }

    private fun exibirMensagem(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
    }
}