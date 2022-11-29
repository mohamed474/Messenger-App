package com.example.messenger_1.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.messenger_1.ProfileActivity
import com.example.messenger_1.R

class ChatFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val textViewTitle = activity?.findViewById<TextView>(R.id.title_toolbar_text_view)
            textViewTitle?.text = "Chats"

        val circleImageViewProfileImage = activity?.findViewById<ImageView>(R.id.activity_profile_img)
        circleImageViewProfileImage?.setOnClickListener {
            startActivity(Intent(activity, ProfileActivity::class.java))
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }


}