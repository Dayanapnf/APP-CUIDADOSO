import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cuidadosoapp.AddMedicamento
import com.example.cuidadosoapp.MedicationView
import com.example.cuidadosoapp.Model.Medicamentos
import com.example.cuidadosoapp.Model.unbundledMedicamentos
import com.example.cuidadosoapp.R
import com.example.cuidadosoapp.databinding.FragmentHomeBinding
import com.example.cuidadosoapp.util.getUserDBRef
import com.google.firebase.database.*

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var medEventListener: ValueEventListener
    private lateinit var medicamentosAdapter: MedicamentosAdapter



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.buttonAddMed.setOnClickListener { startAddMedication() }

        createChannel(
            getString(R.string.med_notification_channel_id),
            getString(R.string.med_notification_channel_name)
        )

        // startActivity(Intent(applicationContext, AlarmView::class.java))
        // Renders med list
        val medRef = getUserDBRef().child("medicamentos")
        medEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val medList = ArrayList<Medicamentos>()
                for (snap in snapshot.children) {
                    val nome = snap.child("nome_med").getValue(String::class.java)
                    val frequencia = snap.child("frequency").getValue(Int::class.java)
                    val alarme = snap.child("startingTime").getValue(Int::class.java)

                    val frequenciaInt: Int = frequencia ?: 0
                    val alarmeInt: Int = alarme ?: 0

                    val medicate = Medicamentos(nome, frequenciaInt, alarmeInt)


                    medList.add(medicate)


                    Log.d("Firebase", "medicamentos: $medicate")

                }
                medicamentosAdapter = MedicamentosAdapter(medList)
                medicamentosAdapter.notifyDataSetChanged()
                Log.d("Firebase", "Total de Medicamentos: ${medList.size}")
                if (medList.isEmpty()) {
                    binding.initialMessageLinearLayout.visibility = View.VISIBLE
                } else {
                    binding.initialMessageLinearLayout.visibility = View.GONE
                    binding.recyclerView.adapter = medicamentosAdapter
                    binding.recyclerView.layoutManager = LinearLayoutManager(context)
                    medicamentosAdapter.setOnItemClickListener(object :
                        MedicamentosAdapter.OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            val intent = Intent(
                                context?.applicationContext,
                                MedicationView::class.java
                            ).apply { }
                            val medicationBundle = medList[position].bundle()
                            intent.putExtra("medicamentos", medicationBundle)
                            startActivity(intent)
                        }
                    })

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("FIREBASE", "Cancelled meds search")
            }
        }

        medRef.addValueEventListener(medEventListener)
        return view
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Canal para avisar horários de medicação"
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

            val notificationManager =
                context?.getSystemService(
                    NotificationManager::class.java
                )
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }


    fun startAddMedication() {
        val intent = Intent(requireContext(), AddMedicamento::class.java)
        startActivity(intent)
    }

}