package com.igorgabriel.recyclerviewtransacoes

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.igorgabriel.recyclerviewtransacoes.databinding.ActivityEditarTransacoesBinding

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val bundle = intent.extras // Todos os parametros passados da outra tela

        if (bundle != null) {
            val id = bundle.getString("id").toString()
            val descricao = bundle.getString("descricao")
            val categoria = bundle.getString("categoria")
            val valor = bundle.getString("valor")

            binding.editDescricao.setText(descricao)
            binding.editCategoria.setText(categoria)
            binding.editValor.setText(valor)

            binding.btnEditar.setOnClickListener {
                val descricao = binding.editDescricao.text.toString()
                val categoria = binding.editCategoria.text.toString()
                val valor = binding.editValor.text.toString()

                editarTransacao(id, descricao, categoria, valor)
            }
        }

    }

    private fun editarTransacao(id: String, descricao: String, categoria: String, valor: String) {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        if(idUsuarioLogado != null) {
            val dados = mapOf(
                "descricao" to descricao,
                "categoria" to categoria,
                "valor" to valor
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