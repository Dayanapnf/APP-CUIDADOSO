package com.example.cuidadosoapp.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import com.example.cuidadosoapp.Auth.FormLogin
import com.example.cuidadosoapp.R
import com.example.cuidadosoapp.Settings
import com.example.cuidadosoapp.databinding.ActivityMainBinding
import com.example.cuidadosoapp.databinding.FragmentPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.zip.Inflater


class PerfilFragment : Fragment() {


    private lateinit var binding: FragmentPerfilBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPerfilBinding.inflate(inflater,container,false)


        FirebaseAuth.getInstance().currentUser?.let { user ->
            binding.nomeperfil.setText("Olá ${user.displayName}!")
        }



        binding.buttonSair.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            val intent = Intent (context ,FormLogin::class.java)
            startActivity(intent)

        }
        binding.btnpermissoes.setOnClickListener{

            val intent = Intent (context ,Settings::class.java)
            startActivity(intent)
        }
        return binding.root
    }

}