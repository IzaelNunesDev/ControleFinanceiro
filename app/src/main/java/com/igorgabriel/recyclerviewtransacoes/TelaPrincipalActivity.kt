package com.igorgabriel.recyclerviewtransacoes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.igorgabriel.recyclerviewtransacoes.databinding.ActivityTelaPrincipalBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TelaPrincipalActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityTelaPrincipalBinding.inflate(layoutInflater)
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

        CoroutineScope(Dispatchers.IO).launch {

            getSaldo()

            val receitas = async { filtrarReceitas() }
            val despesas = async { filtrarDespesas() }

            val resultadoReceitas = receitas.await()
            val resultadoDespesas = despesas.await()

            val balanco = resultadoReceitas - resultadoDespesas

            withContext(Dispatchers.Main) {
                binding.textBalanco.text = balanco.toString()
            }
        }

    }

    private suspend fun getSaldo() {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        if(idUsuarioLogado != null) {
            val refUserSaldo = bancoDados
                .collection("usuarios/${idUsuarioLogado}")

            refUserSaldo.addSnapshotListener { querySnapshot, error ->
                val listaDocuments = querySnapshot?.documents

                listaDocuments?.forEach{documentSnapshot ->
                    val dados = documentSnapshot?.data
                    if(dados != null){
                        val saldo = dados["saldo"].toString()
                        binding.textSaldo.text = saldo
                    }
                }
            }
        }
    }

    private suspend fun filtrarReceitas(): Double {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        var valorReceita = 0.0

        if (idUsuarioLogado != null) {
            val refUserReceita = bancoDados
                .collection("usuarios/${idUsuarioLogado}/transacoes")
                .whereEqualTo("tipo", "Receita")

            refUserReceita.addSnapshotListener { querySnapshot, error ->
                val listaDocuments = querySnapshot?.documents
                //var listaResultado = 0.0

                listaDocuments?.forEach { documentSnapshot ->
                    val dados = documentSnapshot?.data
                    if (dados != null) {
                        val valor = dados["valor"].toString()

                        valorReceita += valor.toDouble()
                    }
                }

                binding.textBlReceita.text = valorReceita.toString()
            }
        }

        return valorReceita
    }

    private suspend fun filtrarDespesas(): Double {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        var valorDespesa = 0.0

        if (idUsuarioLogado != null) {

            // Filtra as despesas
            val refUserDespesa = bancoDados
                .collection("usuarios/${idUsuarioLogado}/transacoes")
                .whereEqualTo("tipo", "Despesa")

            refUserDespesa.addSnapshotListener { querySnapshot, error ->
                val listaDocuments = querySnapshot?.documents
                //var listaResultado = 0.0

                listaDocuments?.forEach { documentSnapshot ->
                    val dados = documentSnapshot?.data
                    if (dados != null) {
                        val valor = dados["valor"].toString()

                        valorDespesa += valor.toDouble()
                    }
                }

                binding.textBlDespesa.text = valorDespesa.toString()
            }
        }

        return valorDespesa
    }


    /* private suspend fun calcularBalaco() {

         *//*val receitas = binding.textBlReceita.text.toString()
        val despesas = binding.textBlDespesa.text.toString()
        val balanco = receitas.toDouble()-despesas.toDouble()*//*

        val receitas = filtrarReceitas()
        val despesas = filtrarDespesas()
        val balanco = receitas-despesas

        binding.textBalanco.text = balanco.toString()

    }*/
}