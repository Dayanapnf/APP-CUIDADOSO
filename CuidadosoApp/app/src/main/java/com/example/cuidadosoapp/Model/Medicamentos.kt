package com.example.cuidadosoapp.Model

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cuidadosoapp.R
import com.example.cuidadosoapp.databinding.FragmentCalendarioBinding
import com.example.cuidadosoapp.databinding.FragmentHomeBinding
import com.example.cuidadosoapp.util.getUserDBRef
import com.google.firebase.database.DataSnapshot
import java.util.*
import kotlin.collections.ArrayList

class Medicamentos( ) {

    var nome_med: String? = null
    var frequency : Int = 0 // Specified on minutes
    var startingTime : Int = 0 // Specified on minutes 0 = 00:00, 61 = 01:01

    constructor(nome_med: String?, frequency: Int,startingTime: Int) : this(){
        this.nome_med = nome_med
        this.frequency = frequency
        this.startingTime = startingTime
    }

    constructor(snap: DataSnapshot) : this(
        snap.child("nome_med").value as String,
        (snap.child("alarm/frequency").value as Long).toInt(),
        (snap.child("alarm/time").value as Long).toInt()
    )

    fun bundle(): Bundle {
        val bundle = Bundle()
        nome_med?.let { bundle.putString("nome_med", it) }
        bundle.putInt("frequency", frequency)
        bundle.putInt("startingTime", startingTime)
        return bundle
    }

    // Save medicine to firebase
    fun save() {
        val medRef = getUserDBRef().child("medicamentos/$nome_med")
        val singleUpdate = mutableMapOf<String, Any>(
            "alarm/time" to startingTime,
            "alarm/frequency" to frequency
        )
        medRef.updateChildren(singleUpdate)

        // Saving alarm references
        val alarmRef = getUserDBRef().child("alarms")
        alarmSchedule().forEach(fun(time: Int) {
            alarmRef.child("$time/$nome_med").setValue(true)
        })
    }

    fun delete() {
        val medRef = getUserDBRef().child("medicamentos/$nome_med")
        medRef.removeValue()
        // Deleting alarm references
        val alarmRef = getUserDBRef().child("alarms")
        alarmSchedule().forEach(fun(time: Int) {
            alarmRef.child("$time/$nome_med").removeValue()
        })
    }

    fun minutesToday(c: Calendar = Calendar.getInstance()) = c.get(Calendar.HOUR_OF_DAY)*60 + c.get(Calendar.MINUTE)

    fun minutesToAlarm(): Int {
        if (frequency == 0) {
            // Tratar o caso em que frequency é igual a zero
            return 0 // Ou qualquer outro valor adequado para o seu caso
        }

        var minutesToAlarm = startingTime - minutesToday()
        if (minutesToAlarm < 0) {
            minutesToAlarm += 24 * 60
        }
        minutesToAlarm %= frequency
        return minutesToAlarm
    }

    private fun alarmSchedule() : List<Int> {
        val schedule = mutableListOf<Int>()
        if(frequency < 10){ // DEBUG
            for (i in 0..10) {
                schedule.add(startingTime + i*frequency)
            }
        }
        else{
            var ini = startingTime%frequency
            while(ini < 24*60){
                schedule.add(ini)
                ini += frequency
            }
        }
        return schedule
    }

    fun setAlarm(context: Context ){
        alarmSchedule().forEach { time -> com.example.cuidadoapp.util.setAlarm(context, time) }
    }
}

// Extension of Bundle to extract Medication
fun Bundle.unbundledMedicamentos() : Medicamentos {
    return Medicamentos(
        this.getString("nome_med"),
        this.getInt("frequency"),
        this.getInt("startingTime")
    )
}




