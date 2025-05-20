package com.igorgabriel.recyclerviewtransacoes

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.firebase.Timestamp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransacoesAdapter(
    private val clique: (String, String, String, Double, String, Timestamp) -> Unit,
    private val onItemDelete: (transacaoId: String) -> Unit
) : Adapter<TransacoesAdapter.TransacoesViewHolder>() {

    private var listaTransacoes = mutableListOf<Transacao>()

    fun atualizarListaDados(novaLista: MutableList<Transacao>) {
        val diffCallback = TransacaoDiffCallback(listaTransacoes, novaLista)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        listaTransacoes.clear()
        listaTransacoes.addAll(novaLista)
        diffResult.dispatchUpdatesTo(this)
    }

    private inner class TransacaoDiffCallback(
        private val oldList: List<Transacao>,
        private val newList: List<Transacao>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
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
        val textData: TextView = itemView.findViewById( R.id.text_cardview_data )

        val cardView: CardView = itemView.findViewById( R.id.card_view_layout )

        fun bind( transacao: Transacao ){
            textDescricao.text = transacao.descricao
            textCategoria.text = transacao.categoria
            // Formatar valor como moeda
            textValor.text = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(transacao.valor)
            // Formatar data
            textData.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(transacao.data.toDate())

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
                    transacao.valor, // Pass Double
                    transacao.tipo,
                    transacao.data // Pass Timestamp
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

    fun removeAt(position: Int) {
        if (position >= 0 && position < listaTransacoes.size) {
            val transacaoParaRemover = listaTransacoes[position]
            onItemDelete(transacaoParaRemover.id)
            // List update will be handled by DiffUtil via Firestore snapshot listener
        }
    }
}