package com.example.cuidadosoapp.Auth


import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import com.example.cuidadoapp.util.setAlarm
import com.example.cuidadosoapp.MainActivity
import com.example.cuidadosoapp.R
import com.example.cuidadosoapp.databinding.ActivityFormLoginBinding
import com.example.cuidadosoapp.util.cleanEmail
import com.example.cuidadosoapp.util.getUserDBRef
import com.example.cuidadosoapp.util.isAuth
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.util.*

class FormLogin : AppCompatActivity() {

    private val RC_OVERLAY_PERMISSION = 111
    private lateinit var binding: ActivityFormLoginBinding
    private lateinit var  auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFormLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        forceLightTheme()

        // Configurar as opções de login do Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("729936358852-pau5oanu4cr0alfag3q6p4908lmmvgn2.apps.googleusercontent.com")
            .requestEmail()
            .build()

        // Criar o cliente de login do Google
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.txtLoginGoogle.setOnClickListener {
            signOut()
            signInWithGoogle()
        }


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
                    if(aut.isSuccessful ){
                        if (!hasAlarmsPermission())
                            createPermissionAlertDialog()
                        else {
                            progressBar.visibility = View.INVISIBLE
                            auth.currentUser?.let { saveUID(it) }
                            updateAlarms()
                            startMain()
                        }
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

       binding.textRecuperarSenha.setOnClickListener {
           val intent = Intent(this, RecuperarSenha::class.java)
           startActivity(intent)
       }
    }

    override fun onStart(){
        super.onStart()
        val usuarioAtual = FirebaseAuth.getInstance().currentUser
        if(usuarioAtual != null){
            startMain()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Obter a conta do Google a partir do resultado
                val account = task.getResult(ApiException::class.java)

                // Autenticar com as credenciais do Google no Firebase
                val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener(this) { authTask ->
                        if (authTask.isSuccessful) {
                            if (!hasAlarmsPermission()){
                                createPermissionAlertDialog()
                            } else{
                                // Login com o Google bem-sucedido, redirecionar para a próxima tela
                                val user = auth.currentUser
                                user?.let { saveUID(it) }
                                updateAlarms()
                                Toast.makeText(this, "Login com o Google realizado com sucesso!", Toast.LENGTH_SHORT).show()
                                // Redirecionar para a próxima tela
                                startMain()
                            }

                        } else {
                            // Tratar falha no login com o Google
                            Toast.makeText(this, "Falha no login com o Google!", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: ApiException) {
                // Tratar erro na autenticação com o Google
                Toast.makeText(this, "Erro na autenticação com o Google: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun saveUID(user : FirebaseUser) {
        val userRef = FirebaseDatabase.getInstance().reference.child("users")
        val cleanEmail = cleanEmail(user.email!!)
        userRef.child(cleanEmail).setValue(user.uid)
    }
    private fun startMain() {
        finish()
        val intent = Intent(this, MainActivity::class.java).apply{}
        startActivity(intent)

    }

    private fun getOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                var getPermissionsIntent  : Intent
                if(isXiaomi()){
                    getPermissionsIntent = Intent("miui.intent.action.APP_PERM_EDITOR")
                        .setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
                        .putExtra("extra_pkgname", packageName)
                }
                else {
                    getPermissionsIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                }
                startActivityForResult(getPermissionsIntent, RC_OVERLAY_PERMISSION)
            }
        }
    }

    private fun forceLightTheme() = AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

    @RequiresApi(Build.VERSION_CODES.M)
    private fun createPermissionAlertDialog() {
        if (Settings.canDrawOverlays(applicationContext))
            return
        val builder = AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_warning_black)
            .setCancelable(false)
            .setPositiveButton("Permitir") { dialog, p1 ->
                dialog?.cancel()
                getOverlayPermission()
            }
            .setNegativeButton("Não permitir") { dialog, p1 ->
                dialog?.cancel()
                startMain()
            }
        if(isXiaomi())
            builder.setMessage("Para usar alarmes sonoros é necessário dar as permissões \"Exibir janelas pop-up\" e \"Mostrar janelas pop-up enquanto estiver executando em segundo plano\" para este aplicativo.")
        else
            builder.setMessage("Para usar alarmes sonoros é necessário dar a permissão de \"Exibir janelas pop-up\" para este aplicativo.")
        val dialog: AlertDialog = builder.create()
        dialog.setTitle("Permitir uso de alarmes?")
        dialog.show()
    }

    private fun isXiaomi() : Boolean = ("xiaomi" == Build.MANUFACTURER.toLowerCase(Locale.ROOT))

    private fun hasAlarmsPermission() : Boolean = (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this))

    private fun updateAlarms() {
        getUserDBRef().child("alarms").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { timeSnap ->
                    val time = timeSnap.key!!.toInt()
                    setAlarm(applicationContext, time)
                    Log.d("ALARM", "Set alarm for time $time")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        googleSignInClient.signOut()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Logout bem-sucedido, faça o tratamento necessário aqui
                } else {
                    // Tratar falha no logout, se necessário
                }
            }
    }
    companion object {
        private const val RC_SIGN_IN = 9001
    }
}
