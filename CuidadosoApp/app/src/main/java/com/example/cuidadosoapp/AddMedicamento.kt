package com.example.cuidadosoapp

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import android.widget.*
import com.example.cuidadosoapp.Model.Medicamentos
import com.example.cuidadosoapp.databinding.ActivityAddMedicamentoBinding
import com.google.firebase.analytics.FirebaseAnalytics

class AddMedicamento : AppCompatActivity() {
    private lateinit var binding: ActivityAddMedicamentoBinding
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private var frequencia: Int = 0

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMedicamentoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        binding.buttonSalvarMed.setOnClickListener { view ->
            if (checkForm()) {
                val nome_med = binding.editMedicamentoForms.text.toString()

                val checkedRadioButtonId = binding.radiogroup.checkedRadioButtonId
                if (checkedRadioButtonId != -1) {
                    frequencia = when (checkedRadioButtonId) {
                        binding.radioButton1.id -> 24 * 60
                        binding.radioButton2.id -> 12 * 60
                        binding.radioButton3.id -> 8 * 60
                        binding.radioButton4.id -> 6 * 60
                        binding.radioButton5.id -> 4 * 60
                        else -> throw Exception("Not known med frequency chosen")
                    }

                    val startMinute: Int =
                        binding.startTimePicker.hour * 60 + binding.startTimePicker.minute
                    val med = Medicamentos(nome_med, frequencia, startMinute)
                    med.save()
                    med.setAlarm(applicationContext)
                    val minutesToAlarm = med.minutesToAlarm()
                    Toast.makeText(
                        applicationContext,
                        "Alarme tocará em ${minutesToAlarm / 60} horas e ${minutesToAlarm % 60} minutos, repetindo a cada ${frequencia / 60} horas",
                        Toast.LENGTH_SHORT
                    ).show()

                    val medBundle = med.bundle()
                    medBundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "medication")
                    medBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, nome_med)
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, medBundle)
                    finish()
                } else {
                    Toast.makeText(this, "Defina a frequência", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    override fun onRestart() {
        super.onRestart()
    }

    private fun checkForm(): Boolean {
        var valid = true
        if (binding.editMedicamentoForms.text.isEmpty()) {
            binding.editMedicamentoForms.error = "Digite o nome da medicação"
            valid = false
        }
        if (binding.radiogroup.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Defina a frequência", Toast.LENGTH_SHORT).show()
            valid = false
        }
        return valid
    }
}
