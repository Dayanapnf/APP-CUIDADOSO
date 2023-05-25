package com.example.cuidadosoapp.Auth


import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.cuidadosoapp.MainActivity
import com.example.cuidadosoapp.databinding.ActivityFormLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class FormLogin : AppCompatActivity() {

    private lateinit var binding: ActivityFormLoginBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)




       binding.button.setOnClickListener {view ->
           val email = binding.editEmail.text.toString()
           val senha = binding.editSenha.text.toString()
           val progressBar = binding.progressBarLogin
           progressBar.visibility = View.VISIBLE

           if(email.isEmpty() || senha.isEmpty()){
               progressBar.visibility = View.INVISIBLE
               val snackbar = Snackbar.make(view,"Preencha todos os campos!", Snackbar.LENGTH_SHORT)
               snackbar.setBackgroundTint(Color.RED)
               snackbar.show()
           }else{
                auth.signInWithEmailAndPassword(email,senha).addOnCompleteListener{aut ->
                    if(aut.isSuccessful){
                        progressBar.visibility = View.INVISIBLE
                        val intent  = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }.addOnFailureListener{exception ->
                    val mensagemErro = when(exception){
                        is FirebaseNetworkException -> "Sem conexão com a internet!"
                        is FirebaseAuthInvalidCredentialsException -> "Email ou senha incorretos."
                        else -> "Falha ao autenticar usuário!"
                    }
                    progressBar.visibility = View.INVISIBLE
                    val snackbar = Snackbar.make(view,mensagemErro, Snackbar.LENGTH_SHORT)
                    snackbar.setBackgroundTint(Color.RED)
                    snackbar.show()
                }
           }
       }

       binding.textTelaCadastro.setOnClickListener{
           val intent = Intent(this, FormCadastro::class.java)
           startActivity(intent)
       }

       binding.textRecuperarSenha.setOnClickListener{
           val intent = Intent(this, RecuperarSenha::class.java)
           startActivity(intent)
       } 
    }

    override fun onStart(){
        super.onStart()

        val usuarioAtual = FirebaseAuth.getInstance().currentUser

        if(usuarioAtual != null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}