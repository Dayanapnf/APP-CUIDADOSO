package com.example.cuidadosoapp

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.example.cuidadosoapp.databinding.ActivityAlarmViewBinding
import com.example.cuidadosoapp.receiver.SnoozeReceiver
import com.example.cuidadosoapp.util.getUserDBRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream

class AlarmView : AppCompatActivity() {
    lateinit var ringtone : Ringtone
    lateinit var vibrator : Vibrator
    private val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    private val CAM_REQUEST = 112
    private var requiresConfirmation = false
    private lateinit var binding: ActivityAlarmViewBinding

    companion object {
        var activity : Activity? = null
    }

    fun forceLightTheme() = AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

    override fun onCreate(savedInstanceState: Bundle?) {
        forceLightTheme()
        activity = this
        binding = ActivityAlarmViewBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        checkConfirmation()
        playAlarm()
        setMedText()
    }

    private fun checkConfirmation() {
        getUserDBRef().child("confirmation").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.value?.let { value ->
                    if(value as Boolean){
                        requiresConfirmation = true
                        binding.tookPills.text = "Tirar foto das medicações"
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun playAlarm() {
        ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
        if (!ringtone.isPlaying)
            ringtone.play()

        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(500)
        }
    }

    private fun setMedText() {
        val meds = intent.getStringArrayExtra("meds")
        var msg = ""
        meds?.forEachIndexed{ i, medName ->
            if(i == 0)
                msg = "Hora de tomar "
            else
                msg += ", "
            msg += medName
        }
        binding.medsTextView.text = msg
    }

    fun tookPills(v: View) {
        ringtone.stop()
        val notificationManager = ContextCompat.getSystemService(
                applicationContext,
                NotificationManager::class.java
        ) as NotificationManager
        notificationManager.cancelAll() // Não realmente cancela os próximos alarmes. (Checar se cancela pro dia seguinte)
        if(requiresConfirmation)
            takePhoto()
        else
            finish()
    }

    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAM_REQUEST)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAM_REQUEST){
            Log.d("CAM", "request code is CAM_REQUEST")
            when (resultCode) {
                Activity.RESULT_OK -> {
                    Log.d("CAM", "RESULT_OK for CAM_REQUEST")
                    // Image captured and saved to fileUri specified in the Intent
                    val extras: Bundle? = data?.extras
                    val bitmap = extras?.get("data") as Bitmap?
                    val bytes = ByteArrayOutputStream()
                    bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                    val path: String = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
                    val imageUri = Uri.parse(path)
                    //img.setImageBitmap(bitmap)
                    val shareIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, imageUri)
                        putExtra(Intent.EXTRA_TITLE, "Testando titulo")
                        type = "image/jpeg"
                    }
                    startActivity(Intent.createChooser(shareIntent, "Enviar foto para:"))
                    finish()
                }
                Activity.RESULT_CANCELED -> {
                    // User cancelled the image capture
                    Toast.makeText(applicationContext, "Compartilhe uma foto dos seus medicamentos para quem cuida de você!", Toast.LENGTH_LONG).show()
                }
                else -> {
                    // Image capture failed, advise user
                }
            }
        }
    }

    fun snoozePills(view: View) {
        assert(ringtone.isPlaying)
        ringtone.stop()
        vibrator.cancel()
        SnoozeReceiver.setSnoozeAlarm(applicationContext, intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        vibrator.cancel()
        ringtone.stop()
    }
}