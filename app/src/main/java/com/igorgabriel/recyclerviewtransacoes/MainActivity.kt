package com.igorgabriel.recyclerviewtransacoes

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.igorgabriel.recyclerviewtransacoes.databinding.ActivityMainBinding
import java.util.Date

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

        transacaoAdapter = TransacoesAdapter(
            clique = { id, descricao, categoria, valor, tipo, data ->
                val intent = Intent(this, Editar_Transacoes_Activity::class.java)

                intent.putExtra("id", id)
                intent.putExtra("descricao", descricao)
                intent.putExtra("categoria", categoria)
                intent.putExtra("valor", valor) // Pass Double directly
                intent.putExtra("tipo", tipo)
                intent.putExtra("data", data.toDate().time) // Convert Timestamp to Long

                startActivity(intent)
            },
            onItemDelete = { transacaoId ->
                deleteTransactionFromFirestore(transacaoId)
            }
        )

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

        binding.btnPrincipal.setOnClickListener {
            startActivity(
                Intent(this, TelaPrincipalActivity::class.java)
            )
        }

        // Remover transacoes arrastando para os lados
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.START or ItemTouchHelper.END
            ){
            // Comportamentos de arratar na tela
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                transacaoAdapter.removeAt(viewHolder.adapterPosition)
            }

        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.rvLista)

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
                        val descricao = dados["descricao"]?.toString() ?: ""
                        val categoria = dados["categoria"]?.toString() ?: ""
                        val valor = dados["valor"] as? Double ?: 0.0
                        val tipo = dados["tipo"]?.toString() ?: ""
                        val data = dados["data"] as? com.google.firebase.Timestamp ?: Timestamp(Date())

                        lista_transacoes.add(0, Transacao(documentId, descricao, categoria, valor, tipo, data))
                        transacaoAdapter.atualizarListaDados(lista_transacoes)
                    }
                }
            }
        }
    }

    private fun deleteTransactionFromFirestore(transacaoId: String) {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        if (idUsuarioLogado != null) {
            val referenciaDocumento = bancoDados
                .collection("usuarios/${idUsuarioLogado}/transacoes")
                .document(transacaoId)

            referenciaDocumento.delete()
                .addOnSuccessListener {
                    Log.d("MainActivity", "Transaction deleted successfully: $transacaoId")
                    // Optional: Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Error deleting transaction: $transacaoId", e)
                    Toast.makeText(this, "Failed to delete transaction: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Log.w("MainActivity", "User not logged in, cannot delete transaction: $transacaoId")
            Toast.makeText(this, "Error: User not logged in.", Toast.LENGTH_LONG).show()
        }
    }
}