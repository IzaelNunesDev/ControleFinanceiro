package com.igorgabriel.recyclerviewtransacoes

import com.google.firebase.Timestamp

data class Transacao(
    val id: String,
    val descricao: String,
    val categoria: String,
    val valor: Double,
    val tipo: String,
    val data: Timestamp
)
