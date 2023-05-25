package com.example.cuidadosoapp.Auth

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.cuidadosoapp.databinding.ActivityRecuperarSenhaBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class RecuperarSenha : AppCompatActivity() {

    private lateinit var binding: ActivityRecuperarSenhaBinding
    private val auth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecuperarSenhaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRecover.setOnClickListener{view ->
            val email_send  = binding.editRecEmail.text.toString()

            if(!email_send.isEmpty()){
                auth.sendPasswordResetEmail(email_send).addOnCompleteListener {recover ->
                    if(recover.isSuccessful){
                        val snackbar = Snackbar.make(view,
                            "Email de redefinição de senha enviado para $email_send. Por favor, verifique o seu e-mail.", Snackbar.LENGTH_LONG)
                        snackbar.setBackgroundTint(Color.GREEN)
                        snackbar.show()
                        binding.editRecEmail.setText("")
                        val intent = Intent(this, FormLogin::class.java)
                        startActivity(intent)
                        finish()
                    }
                }.addOnFailureListener{exception ->
                    val mensagemErro = when(exception){
                        is FirebaseAuthInvalidCredentialsException -> "Email inválido!"
                        is FirebaseNetworkException -> "Sem conexão com a internet!"
                        else -> "Não foi possível enviar o e-mail de recuperação!"
                    }
                    val snackbar = Snackbar.make(view, mensagemErro, Snackbar.LENGTH_SHORT)
                    snackbar.setBackgroundTint(Color.RED)
                    snackbar.show()
                }
            }
        }


    }
}