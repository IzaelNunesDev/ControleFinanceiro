package com.igorgabriel.recyclerviewtransacoes

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.igorgabriel.recyclerviewtransacoes.databinding.ActivityAdicionarTransacaoBinding
import java.text.SimpleDateFormat
import java.util.Calendar

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // TO DO: data iniciar com o dia atual
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

                    binding.textData.text = SimpleDateFormat("EEE, dd MMM yyyy").format(dataSelecionada.time)
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
                    val valor = binding.editAddValor.text.toString()
                    var tipo = opcaoSelecionada.text.toString()
                    val data = binding.textData.text.toString()

                    if (!data.equals("Escolha uma data")){
                        adicionarTransacao(descricao, categoria, valor, tipo, data)
                    }
                }
            }

        }
    }

    private fun adicionarTransacao(
        descricao: String,
        categoria: String,
        valor: String,
        tipo: String,
        data: String
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