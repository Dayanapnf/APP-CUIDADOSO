package com.example.cuidadosoapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cuidadosoapp.Model.Calendario
import com.example.cuidadosoapp.Model.Medicamentos
import com.example.cuidadosoapp.databinding.RowCalendarioBinding
import com.example.cuidadosoapp.databinding.RowMedicamentosBinding

class RvMedicamentoAdapter(private val medList: java.util.ArrayList<Medicamentos>) :
    RecyclerView.Adapter<RvMedicamentoAdapter.ViewHolder>() {

    class ViewHolder(val binding: RowCalendarioBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            RowCalendarioBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return medList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = medList[position]
        holder.apply {
            binding.apply {
                caledTitleTextView.text = currentItem.nome_med

            }
        }

    }
}