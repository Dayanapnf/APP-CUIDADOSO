import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cuidadosoapp.Model.Medicamentos
import com.example.cuidadosoapp.R
import org.imaginativeworld.whynotimagecarousel.adapter.FiniteCarouselAdapter

class MedicamentosAdapter(private val medicamentos: List<Medicamentos>) :
    RecyclerView.Adapter<MedicamentosAdapter.MedicamentoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicamentoViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_medicamentos, parent, false)
        return MedicamentoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MedicamentoViewHolder, position: Int) {
        val medicamento = medicamentos[position]
        holder.bind(medicamento)
    }

    override fun getItemCount(): Int = medicamentos.size

    inner class MedicamentoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nomeTextView: TextView = itemView.findViewById(R.id.medTitleTextView)

        fun bind(medicamento: Medicamentos) {
            nomeTextView.text = medicamento.nome_med
            itemView.setOnClickListener {
                onItemClickListener?.onItemClick(position)
            }
        }
    }
    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }




    // Método para definir o listener do item
    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }
}
