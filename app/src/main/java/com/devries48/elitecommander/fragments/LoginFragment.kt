package com.devries48.elitecommander.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.devries48.elitecommander.R
import com.devries48.elitecommander.activities.LoginActivity


class LoginFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val button: Button = view.findViewById(R.id.frontierLoginButton)
        button.setOnClickListener {
            val i = Intent(context, LoginActivity::class.java)
            activity?.startActivityForResult(i, FRONTIER_LOGIN_REQUEST_CODE)
        }
    }

    companion object {
        private const val FRONTIER_LOGIN_REQUEST_CODE = 999
    }
    }
