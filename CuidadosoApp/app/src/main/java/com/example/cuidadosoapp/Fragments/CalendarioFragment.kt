import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.cuidadosoapp.Model.Medicamentos

import com.example.cuidadosoapp.RvMedicamentoAdapter
import com.example.cuidadosoapp.databinding.FragmentCalendarioBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CalendarioFragment : Fragment() {

    private var _binding : FragmentCalendarioBinding? = null
    private val binding get() = _binding!!
    private lateinit var medList:ArrayList<Medicamentos>
    private lateinit var firebaseRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalendarioBinding.inflate(inflater, container, false)

        firebaseRef = FirebaseDatabase.getInstance().getReference("medicamentos")
        medList = arrayListOf()

        fechData()

        binding.recyclerViewCalend.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this.context)
        }

        return binding.root
    }

    private fun fechData() {
        firebaseRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                medList.clear()
                if(snapshot.exists()){
                    for(medSnap in snapshot.children){
                        val med = medSnap.getValue(Medicamentos::class.java)
                        medList.add(med!!)
                    }
                }
                val rvAdapter = RvMedicamentoAdapter(medList)
                binding.recyclerViewCalend.adapter =  rvAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context,"error:$error", Toast.LENGTH_SHORT).show()
            }
        })

    }


}
