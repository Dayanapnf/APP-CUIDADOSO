/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.cuidadosoapp.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.cuidadosoapp.AlarmView
import com.example.cuidadosoapp.util.formatTime
import com.example.cuidadosoapp.util.getUserDBRef
import com.example.cuidadosoapp.util.sendNotification
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val time = intent.getIntExtra("time", 0)
        Log.d("ALARM", "Received alarm time $time")
        val notificationManager = ContextCompat.getSystemService(
                context,
                NotificationManager::class.java
        ) as NotificationManager

        if(intent.getStringArrayExtra("meds")?.isNotEmpty() == true){ // Se o intent ja veio com o meds nao precisa chamar firebase (esse e o caso do snooze)
            var meds : MutableList<String> = intent.getStringArrayExtra("meds")!!.toMutableList()
            var msg = "Lembrete das ${formatTime(time)}! Hora de tomar "
            meds.forEachIndexed { index, med ->
                if(index > 0)
                    msg += ", "
                msg += med
            }
            notificationManager.sendNotification(
                    msg,
                    context
            )
            val alarmViewIntent = Intent(context, AlarmView::class.java).apply {
                this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                        Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP
                this.putExtra("meds", meds.toTypedArray())
                this.putExtra("time", time)
            }
            context.startActivity(alarmViewIntent)
        }
        else {
            getUserDBRef().child("alarms/$time").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var msg = "Lembrete das ${formatTime(time)}! Hora de tomar "
                    var meds: MutableList<String> = mutableListOf()
                    snapshot.children.forEachIndexed { index, med ->
                        if (index > 0)
                            msg += ", "
                        msg += med.key!!
                        meds.add(med.key!!)
                    }

                    notificationManager.sendNotification(
                            msg,
                            context
                    )
                    val alarmViewIntent = Intent(context, AlarmView::class.java).apply {
                        this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                                Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP
                        this.putExtra("meds", meds.toTypedArray())
                        this.putExtra("time", time)
                    }

                    context.startActivity(alarmViewIntent)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
    }

}
