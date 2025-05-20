package com.igorgabriel.recyclerviewtransacoes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.igorgabriel.recyclerviewtransacoes.databinding.ActivityTelaPrincipalBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

            val receitasDeferred = async { filtrarReceitas() }
            val despesasDeferred = async { filtrarDespesas() }
            val gastosHojeDeferred = async { getGastosHoje() }

            val resultadoReceitas = receitasDeferred.await()
            val resultadoDespesas = despesasDeferred.await()
            val resultadoGastosHoje = gastosHojeDeferred.await()

            val balanco = resultadoReceitas - resultadoDespesas

            withContext(Dispatchers.Main) {
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
                binding.textSaldo.text = currencyFormat.format(balanco) // Update Saldo Total
                binding.textBlReceita.text = currencyFormat.format(resultadoReceitas)
                binding.textBlDespesa.text = currencyFormat.format(resultadoDespesas)
                binding.textGastosHoje.text = currencyFormat.format(resultadoGastosHoje)
                binding.textBalanco.text = currencyFormat.format(balanco)
            }
        }
    }

    private suspend fun filtrarReceitas(): Double {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        var valorReceita = 0.0

        if (idUsuarioLogado != null) {
            try {
                val querySnapshot = bancoDados
                    .collection("usuarios/${idUsuarioLogado}/transacoes")
                    .whereEqualTo("tipo", "Receita")
                    .get()
                    .await()

                for (documentSnapshot in querySnapshot.documents) {
                    val valor = documentSnapshot.getDouble("valor") ?: 0.0
                    valorReceita += valor
                }
            } catch (e: Exception) {
                // Handle exceptions, e.g., log error
                // For now, we'll let it return 0.0 on error
            }
        }
        return valorReceita
    }

    private suspend fun filtrarDespesas(): Double {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        var valorDespesa = 0.0

        if (idUsuarioLogado != null) {
            try {
                val querySnapshot = bancoDados
                    .collection("usuarios/${idUsuarioLogado}/transacoes")
                    .whereEqualTo("tipo", "Despesa")
                    .get()
                    .await()

                for (documentSnapshot in querySnapshot.documents) {
                    val valor = documentSnapshot.getDouble("valor") ?: 0.0
                    valorDespesa += valor
                }
            } catch (e: Exception) {
                // Handle exceptions
                // For now, we'll let it return 0.0 on error
            }
        }
        return valorDespesa
    }

    private suspend fun getGastosHoje(): Double {
        val idUsuarioLogado = autenticacao.currentUser?.uid
        var valorGastosHoje = 0.0

        if (idUsuarioLogado != null) {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
            val inicioDia = Timestamp(calendar.time)

            calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59); calendar.set(Calendar.SECOND, 59); calendar.set(Calendar.MILLISECOND, 999)
            val fimDia = Timestamp(calendar.time)

            try {
                val querySnapshot = bancoDados
                    .collection("usuarios/${idUsuarioLogado}/transacoes")
                    .whereEqualTo("tipo", "Despesa")
                    .whereGreaterThanOrEqualTo("data", inicioDia)
                    .whereLessThanOrEqualTo("data", fimDia)
                    .get()
                    .await()

                for (documentSnapshot in querySnapshot.documents) {
                    val valor = documentSnapshot.getDouble("valor") ?: 0.0
                    valorGastosHoje += valor
                }
            } catch (e: Exception) {
                // Handle exceptions
                // For now, we'll let it return 0.0 on error
            }
        }
        return valorGastosHoje
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