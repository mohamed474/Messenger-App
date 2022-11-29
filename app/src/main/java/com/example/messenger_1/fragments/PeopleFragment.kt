package com.example.messenger_1.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.messenger_1.R

class PeopleFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val textViewTitle = activity?.findViewById<TextView>(R.id.title_toolbar_text_view)
            textViewTitle?.text = "People"


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_people, container, false)
    }
}