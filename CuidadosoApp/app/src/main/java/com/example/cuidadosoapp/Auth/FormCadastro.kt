package com.example.cuidadosoapp.Auth

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.cuidadosoapp.databinding.ActivityFormCadastroBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class FormCadastro : AppCompatActivity() {

    private lateinit var binding: ActivityFormCadastroBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.btnCadastrar.setOnClickListener{view ->
            val nome  = binding.editNome.text.toString()
            val email = binding.editEmail.text.toString()
            val senha = binding.editSenha.text.toString()
            val confSenha = binding.confirmSenha.text.toString()
            val progressBar = binding.progressBarCad
            progressBar.visibility = View.VISIBLE

            if(nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confSenha.isEmpty()){
                progressBar.visibility = View.INVISIBLE
                val snackbar = Snackbar.make(view,"Preencha todos os campos!", Snackbar.LENGTH_SHORT)
                snackbar.setBackgroundTint(Color.RED)
                snackbar.show()
            }else if(!senha.equals(confSenha)){
                progressBar.visibility = View.INVISIBLE
                val snackbar = Snackbar.make(view,"A confirmação de senha não confere! ", Snackbar.LENGTH_SHORT)
                snackbar.setBackgroundTint(Color.RED)
                snackbar.show()
            }else{

                auth.createUserWithEmailAndPassword(email,senha).addOnCompleteListener{cadastro ->
                    if(cadastro.isSuccessful){
                        val snackbar = Snackbar.make(view,"Sucesso ao cadastrar usuário! ", Snackbar.LENGTH_SHORT)
                        snackbar.setBackgroundTint(Color.GREEN)
                        snackbar.show()
                        binding.editNome.setText("")
                        binding.editEmail.setText("")
                        binding.editSenha.setText("")
                        binding.confirmSenha.setText("")

                        progressBar.visibility = View.INVISIBLE

                        val intent = Intent(this, FormLogin::class.java)
                        startActivity(intent)
                        finish()
                    }
                }.addOnFailureListener{exception ->
                    val mensagemErro = when(exception){
                        is FirebaseAuthWeakPasswordException -> "A senha deve ter no mínimo  6 caracteres"
                        is FirebaseAuthInvalidCredentialsException -> "Email inválido!"
                        is FirebaseAuthUserCollisionException -> "Esta conta já existe!"
                        is FirebaseNetworkException -> "Sem conexão com a internet!"
                        else -> "Erro ao cadastrar usuário!"
                    }
                    progressBar.visibility = View.INVISIBLE
                    val snackbar = Snackbar.make(view,mensagemErro, Snackbar.LENGTH_SHORT)
                    snackbar.setBackgroundTint(Color.RED)
                    snackbar.show()
                }
            }

        }

        binding.possuiCadastro.setOnClickListener {
            val intent = Intent(this, FormLogin::class.java)
            startActivity(intent)
        }
    }
}