package com.example.cuidadosoapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cuidadosoapp.Model.Medicamentos
import org.imaginativeworld.whynotimagecarousel.adapter.FiniteCarouselAdapter

class MedicamentosAdapter(private val medList: ArrayList<Medicamentos>) :
    RecyclerView.Adapter<MedicamentosAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Inflate the layout for each item and return a new ViewHolder object
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.row_medicamentos, parent, false)
        return MyViewHolder(itemView)
    }

    // This method returns the total
    // number of items in the data set
    override fun getItemCount(): Int {
        return medList.size
    }

    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    // Método para definir o listener do item
    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }


    // This method binds the data to the ViewHolder object
    // for each item in the RecyclerView
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentMed = medList[position]
        holder.name.text = currentMed.nome_med

        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(position)
        }
    }

    // This class defines the ViewHolder object for each item in the RecyclerView
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.medTitleTextView)
    }

}