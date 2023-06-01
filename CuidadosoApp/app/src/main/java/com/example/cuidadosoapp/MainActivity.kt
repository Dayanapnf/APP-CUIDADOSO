package com.example.cuidadosoapp

import HomeFragment
import android.R.*
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.cuidadosoapp.Auth.RecuperarSenha
import com.example.cuidadosoapp.Fragments.*
import com.example.cuidadosoapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(HomeFragment())

        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.item_home -> replaceFragment(HomeFragment())
                R.id.item_tarefas -> replaceFragment(TarefasFragment())
                R.id.item_calendario -> replaceFragment(CalendarioFragment())
                R.id.item_perfil -> replaceFragment(PerfilFragment())
                else -> {
                    replaceFragment(HomeFragment())
                }
            }
            true
        }

        binding.floatingButton.setOnClickListener{
            showBottomDialog()
        }

    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout_main, fragment)
        fragmentTransaction.commit()
    }

    private fun showBottomDialog(){
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottomsheet_layout)

        val medicamentoLayout: TextView = dialog.findViewById(R.id.medicamento)
        val alimentacaoLayout: TextView = dialog.findViewById(R.id.alimentacao)
        val higieneLayout: TextView = dialog.findViewById(R.id.higiene)
        val lazerLayout: TextView = dialog.findViewById(R.id.lazer)
        val cancelButton: ImageView = dialog.findViewById(R.id.icon_close)

        cancelButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                dialog.dismiss()
            }
        })
        medicamentoLayout.setOnClickListener {
            val intent = Intent(this, AddMedicamento::class.java)
            startActivity(intent)
        }
        alimentacaoLayout.setOnClickListener{
            val intent = Intent(this, AddAlimentacao::class.java)
            startActivity(intent)
        }
        higieneLayout.setOnClickListener{
            val intent = Intent(this, AddHigiene::class.java)
            startActivity(intent)
        }
        lazerLayout.setOnClickListener{
            val intent = Intent(this, AddLazer::class.java)
            startActivity(intent)
        }

        dialog.show();
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation;
        dialog.window?.setGravity(Gravity.BOTTOM);

    }


}