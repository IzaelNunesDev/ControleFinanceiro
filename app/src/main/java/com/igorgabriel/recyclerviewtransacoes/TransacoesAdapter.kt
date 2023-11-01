package com.igorgabriel.recyclerviewtransacoes

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class TransacoesAdapter(
    private val clique: (String, String, String, String, String) -> Unit
) : Adapter<TransacoesAdapter.TransacoesViewHolder>() {

    private var listaTransacoes = mutableListOf<Transacao>()

    fun atualizarListaDados(lista: MutableList<Transacao> ) {
        listaTransacoes = lista
        notifyDataSetChanged()
    }

    /*fun adicionar(descricao: String, categoria: String, valor: String){
        listaTransacoes.add(0, Transacao(descricao, categoria, valor))
        notifyItemInserted(0)
    }*/

    inner class TransacoesViewHolder(
        val itemView : View
    ): ViewHolder( itemView ){

        val textDescricao: TextView = itemView.findViewById( R.id.text_cardview_descricao )
        val textCategoria: TextView = itemView.findViewById( R.id.text_cardview_categoria )
        val textValor: TextView = itemView.findViewById( R.id.text_cardview_valor )

        val cardView: CardView = itemView.findViewById( R.id.card_view_layout )

        fun bind( transacao: Transacao ){
            textDescricao.text = transacao.descricao
            textCategoria.text = transacao.categoria
            textValor.text = transacao.valor

            if(transacao.tipo.equals("Receita")) {
                textValor.setTextColor( Color.GREEN )
            } else if(transacao.tipo.equals("Despesa")){
                textValor.setTextColor( Color.RED )
            }

            cardView.setOnClickListener{
                clique(
                    transacao.id,
                    transacao.descricao,
                    transacao.categoria,
                    transacao.valor,
                    transacao.tipo
                )
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransacoesViewHolder {
        val layoutInflater = LayoutInflater.from(
            parent.context
        )

        val itemView = layoutInflater.inflate(
            R.layout.item_cardview_transacoes, parent, false
        )

        return TransacoesViewHolder( itemView )
    }

    override fun getItemCount(): Int {
        return listaTransacoes.size
    }

    override fun onBindViewHolder(holder: TransacoesViewHolder, position: Int) {
        val transacoes = listaTransacoes[position]

        holder.bind( transacoes )
    }
}