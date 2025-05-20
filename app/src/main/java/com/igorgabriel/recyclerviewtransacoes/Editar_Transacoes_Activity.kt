package com.igorgabriel.recyclerviewtransacoes

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.igorgabriel.recyclerviewtransacoes.databinding.ActivityEditarTransacoesBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Editar_Transacoes_Activity : AppCompatActivity() {

    private val binding by lazy {
        ActivityEditarTransacoesBinding.inflate(layoutInflater)
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

        val bundle = intent.extras // Todos os parametros passados da outra tela

        if (bundle != null) {
            val id = bundle.getString("id").toString()
            val descricao = bundle.getString("descricao")
            val categoria = bundle.getString("categoria")
            val valor = bundle.getDouble("valor") // Retrieve as Double
            val tipo = bundle.getString("tipo")
            val dataMillis = bundle.getLong("data") // Retrieve as Long

            selectedTimestamp = Timestamp(Date(dataMillis)) // Convert Long to Timestamp

            binding.editDescricao.setText(descricao)
            binding.editCategoria.setText(categoria)
            binding.editValor.setText(valor.toString()) // Consider NumberFormat for locale-specific formatting
            binding.editData.text = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(selectedTimestamp!!.toDate())


            binding.btnEditar.setOnClickListener {
                val updatedDescricao = binding.editDescricao.text.toString()
                val updatedCategoria = binding.editCategoria.text.toString()
                val valorString = binding.editValor.text.toString()

                val updatedValor = valorString.toDoubleOrNull()
                if (updatedValor == null) {
                    exibirMensagem("Valor inválido. Por favor, insira um número.")
                    return@setOnClickListener
                }

                if (selectedTimestamp == null) {
                    exibirMensagem("Data inválida. Por favor, selecione uma data.")
                    return@setOnClickListener
                }

                editarTransacao(id, updatedDescricao, updatedCategoria, updatedValor, tipo, selectedTimestamp!!)
            }
        }

        binding.ivMostrarCalendario.setOnClickListener {
            val calendario = Calendar.getInstance()
            selectedTimestamp?.toDate()?.let {
                calendario.time = it
            }
            val ano = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, anoSelecionado, mesSelecionado, diaSelecionado ->
                    val dataSelecionada = Calendar.getInstance()
                    dataSelecionada.set(anoSelecionado, mesSelecionado, diaSelecionado)
                    selectedTimestamp = Timestamp(dataSelecionada.time)
                    binding.editData.text = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(dataSelecionada.time)
                },
                ano,
                mes,
                dia
            )
            datePickerDialog.show()
        }
    }

    private fun editarTransacao(id: String, descricao: String, categoria: String, valor: Double, tipo: String?, data: Timestamp) {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        if(idUsuarioLogado != null) {
            val dados = mapOf(
                "descricao" to descricao,
                "categoria" to categoria,
                "valor" to valor, // Use Double
                "tipo" to tipo,
                "data" to data // Use Timestamp
            )

            val referenciaUsuario = bancoDados
                .collection("usuarios/${idUsuarioLogado}/transacoes")
                .document( id )

            referenciaUsuario
                .set(dados)
                .addOnSuccessListener {
                    exibirMensagem("Transacao atualizada com sucesso")
                    finish()
                }.addOnFailureListener { exception ->
                    exibirMensagem("Falha ao atualizar transacao")
                }
        }
    }

    private fun exibirMensagem(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
    }
}