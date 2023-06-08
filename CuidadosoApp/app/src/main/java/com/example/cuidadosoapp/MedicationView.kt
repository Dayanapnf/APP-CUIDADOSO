package com.example.cuidadosoapp

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cuidadosoapp.Model.Medicamentos
import com.example.cuidadosoapp.Model.unbundledMedicamentos
import com.example.cuidadosoapp.databinding.ActivityMedicationViewBinding



class MedicationView : AppCompatActivity() {
    private lateinit var binding: ActivityMedicationViewBinding
    private var med = Medicamentos("Nenhuma medicação selecionada!", 24*60, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicationViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.getBundleExtra("medicamentos")?.let { med = it.unbundledMedicamentos() }
        binding.titleTextView.text = med.nome_med
        binding.startTimeTextView.text = nextAlarmMessage()
        binding.frequencyTextView.text = "Repete a cada ${med.frequency/60} horas."

    }

    private fun nextAlarmMessage() : String {
        val nextAlarmIn = med.minutesToAlarm()
        val hours = nextAlarmIn/60
        val hourNoun = "hora" + if(hours > 1) "s" else ""
        val min = nextAlarmIn%60
        val minNoun = "minuto" + if(min > 1) "s" else ""
        if(hours > 0){
            if(min > 0)
                return "Próxima dose em $hours $hourNoun e $min $minNoun"
            else
                return "Próxima dose em $hours $hourNoun"
        }
        else {
            if(min > 0)
                return "Próxima dose em $min $minNoun"
            else
                return "Tomar sua dose agora"
        }
    }

    fun editMedication(view: View) {
        Toast.makeText(applicationContext, "Não implementado ainda", Toast.LENGTH_SHORT).show()
    }


    // MENU with delete med option
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.medication_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.deleteItem){
            deleteMedication()
            return true
        }
        return false
    }

    private fun deleteMedication() {
        med.delete()
        finish()
    }
}