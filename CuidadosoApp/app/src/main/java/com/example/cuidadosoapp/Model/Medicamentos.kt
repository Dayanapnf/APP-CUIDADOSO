package com.example.cuidadosoapp.Model

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.cuidadoapp.util.setAlarm
import com.example.cuidadosoapp.R
import com.example.cuidadosoapp.util.cleanEmail
import com.example.cuidadosoapp.util.getUserDBRef
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class Medicamentos(  ) {

    var nome_med: String? = null
    var frequency : Int = 0 // Specified on minutes
    var startingTime : Int = 0 // Specified on minutes 0 = 00:00, 61 = 01:01


    constructor(nome_med: String?, frequency: Int,startingTime: Int) : this(){
        this.nome_med = nome_med ?: ""
        this.frequency = frequency
        this.startingTime = startingTime
    }
    constructor(snap: DataSnapshot) : this(
        snap.child("nome_med").value as String,
        (snap.child("alarm/frequency").value as Long).toInt(),
        (snap.child("alarm/time").value as Long).toInt()
    )

    @JvmName("getNome_med1")
    /*fun getNome_med(): String? {
        return if (::nome_med.isInitialized) nome_med else ""
    }*/

    fun bundle(): Bundle {
        val bundle = Bundle()
        nome_med?.let { bundle.putString("nome_med", it) }
        bundle.putInt("frequency", frequency)
        bundle.putInt("startingTime", startingTime)
        return bundle
    }



    fun saveUID(user : FirebaseUser) {
        val userRef = FirebaseDatabase.getInstance().reference.child("users")
        val cleanEmail = cleanEmail(user.email!!)
        userRef.child(cleanEmail).setValue(user.uid)
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

// Adapter of medList to an ArrayAdapter
class MedicationAdapter(context: Context, medList: ArrayList<Medicamentos>)
    : ArrayAdapter<Medicamentos>(context,  R.layout.row_medicamentos, medList) {

    private class ViewHolder {
        lateinit var nome_med: TextView
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val med = getItem(position)!!
        val holder: ViewHolder
        val retView: View

        if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            retView = inflater.inflate(R.layout.row_medicamentos, parent, false)
            holder = ViewHolder()
            holder.nome_med = retView.findViewById(R.id.medTitleTextView)
            retView.tag = holder
        } else {
            retView = convertView
            holder = retView.tag as ViewHolder
        }

        holder.nome_med.text = med.nome_med
        return retView
    }

}

