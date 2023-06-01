package com.example.cuidadosoapp.Model

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.cuidadoapp.util.minutesToday
import com.example.cuidadosoapp.R
import com.example.cuidadosoapp.databinding.RowAlarmBinding
import com.example.cuidadosoapp.util.formatTime
import com.google.firebase.database.DataSnapshot


class Alarm(val time: Int,
            val medications: Array<String>) {
    constructor(snap: DataSnapshot) : this(
        snap.key!!.toInt(),
        snap.children.map { it.key!! }.toTypedArray()
    )

}

// Adapter of alarmList to an ArrayAdapter
class AlarmAdapter(context: Context, alarmList: List<Alarm>) :
    ArrayAdapter<Alarm>(context, R.layout.row_alarm, alarmList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val alarm = getItem(position)!!

        val binding: RowAlarmBinding
        val retView: View

        if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            binding = RowAlarmBinding.inflate(inflater, parent, false)
            retView = binding.root
            retView.tag = binding
        } else {
            retView = convertView
            binding = retView.tag as RowAlarmBinding
        }

        binding.timeTextView.text = formatTime(alarm.time)
        binding.medicationsTextView.text = alarm.medications.joinToString(", ")
        disableIfPast(retView, alarm.time)

        return retView
    }

    private fun disableIfPast(v: View, time: Int) {
        if (time < minutesToday()) {
            val disabledColor = ContextCompat.getColor(v.context, R.color.text_disabled)
            v.findViewById<TextView>(R.id.timeTextView).setTextColor(disabledColor)
            v.findViewById<TextView>(R.id.medicationsTextView).setTextColor(disabledColor)
        } else {
            val normalColor = ContextCompat.getColor(v.context, R.color.black)
            v.findViewById<TextView>(R.id.timeTextView).setTextColor(normalColor)
            v.findViewById<TextView>(R.id.medicationsTextView).setTextColor(normalColor)
        }
    }
}




