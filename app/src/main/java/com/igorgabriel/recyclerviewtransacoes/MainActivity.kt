package com.igorgabriel.recyclerviewtransacoes

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.igorgabriel.recyclerviewtransacoes.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var transacaoAdapter: TransacoesAdapter

    private val binding by lazy{
        ActivityMainBinding.inflate(layoutInflater)
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

        listarTransacoes()

        transacaoAdapter = TransacoesAdapter{id, descricao, categoria, valor ->
            val intent = Intent(this, Editar_Transacoes_Activity::class.java)

            intent.putExtra("id", id)
            intent.putExtra("descricao", descricao)
            intent.putExtra("categoria", categoria)
            intent.putExtra("valor", valor)

            startActivity(intent)
        }
        //transacaoAdapter.atualizarListaDados(lista_transacoes)
        binding.rvLista.adapter = transacaoAdapter

        binding.rvLista.layoutManager = LinearLayoutManager(this)

        binding.btnAdicionar.setOnClickListener {
            startActivity(
                Intent(this, AdicionarTransacaoActivity::class.java)
            )
        }

        binding.btnLogout.setOnClickListener {
            autenticacao.signOut()
            finish()
        }
    }

    private fun listarTransacoes() {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        if(idUsuarioLogado != null){
            val referenciaUsuario = bancoDados
                .collection("usuarios/${idUsuarioLogado}/transacoes")

            referenciaUsuario.addSnapshotListener { querySnapshot, error ->
                val listaDocuments = querySnapshot?.documents
                val lista_transacoes = mutableListOf<Transacao>()

                listaDocuments?.forEach{documentSnapshot ->
                    val dados = documentSnapshot?.data
                    if(dados != null){
                        val documentId = documentSnapshot.id
                        val descricao = dados["descricao"].toString()
                        val categoria = dados["categoria"].toString()
                        val valor = dados["valor"].toString()

                        lista_transacoes.add(0, Transacao(documentId, descricao, categoria, valor))
                        transacaoAdapter.atualizarListaDados(lista_transacoes)
                    }
                }
            }
        }
    }
}