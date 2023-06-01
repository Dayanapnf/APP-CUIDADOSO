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
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cuidadosoapp.AddMedicamento
import com.example.cuidadosoapp.MedicamentosAdapter
import com.example.cuidadosoapp.MedicationView
import com.example.cuidadosoapp.Model.Medicamentos
import com.example.cuidadosoapp.Model.MedicationAdapter
import com.example.cuidadosoapp.R
import com.example.cuidadosoapp.databinding.FragmentHomeBinding
import com.example.cuidadosoapp.util.getUserDBRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var medEventListener: ValueEventListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        createChannel(
            getString(R.string.med_notification_channel_id),
            getString(R.string.med_notification_channel_name)
        )
        setupMedList()
        binding.buttonAddMed.setOnClickListener { startAddMedication() }
        return view
    }

    private fun setupMedList() {
        val medRef = getUserDBRef().child("medicamentos")
        medEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val medList = ArrayList<Medicamentos>()
                snapshot.children.forEach { snap ->
                    val medicate = snap.getValue(Medicamentos::class.java)
                    medicate?.let { medList.add(it) }
                    //medList.add(Medicamentos(snap))
                }
                if (medList.isEmpty()) {
                    binding.initialMessageLinearLayout.visibility = View.VISIBLE
                } else {
                    binding.initialMessageLinearLayout.visibility = View.GONE
                    val adapter = MedicamentosAdapter(medList)
                    binding.recyclerView.adapter = adapter
                    binding.recyclerView.layoutManager = LinearLayoutManager(context)
                    adapter.setOnItemClickListener(object :
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
