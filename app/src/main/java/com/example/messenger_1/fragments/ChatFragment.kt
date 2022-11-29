package com.example.messenger_1.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger_1.ChatActivity
import com.example.messenger_1.ProfileActivity
import com.example.messenger_1.R
import com.example.messenger_1.model.User
import com.example.messenger_1.recyclerview.ChatItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.fragment_chat.*

class ChatFragment : Fragment() {

    private val firestoreInstance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private lateinit var chatSection: Section

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val textViewTitle = activity?.findViewById<TextView>(R.id.title_toolbar_text_view)
        textViewTitle?.text = "Chats"

        val circleImageViewProfileImage = activity?.findViewById(R.id.activity_profile_img) as ImageView

            circleImageViewProfileImage.setOnClickListener {
            startActivity(Intent(activity, ProfileActivity::class.java))
            activity!!.finish()
        }

        // Listening of chats
        addChatListener(::initRecyclerView)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun addChatListener(onListen : (List<Item>) -> Unit): ListenerRegistration {
        return firestoreInstance.collection("users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection("sharedChat")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                if (firebaseFirestoreException != null){
                    return@addSnapshotListener
                }
                val items = mutableListOf<Item>()

                querySnapshot!!.documents.forEach { document ->

                    if (document.exists()) {
                        items.add(ChatItem(document.id, document.toObject(User::class.java)!!, activity!!))
                    }

                }

                onListen(items)
            }
    }

    private fun initRecyclerView(item: List<Item>){
        chat_recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter =  GroupAdapter<ViewHolder>().apply {
                chatSection = Section(item)
                add(chatSection)
                setOnItemClickListener(onItemClick)
            }
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    val onItemClick = OnItemClickListener { item, view ->

        if (item is ChatItem){

            val intentChatActivity = Intent(activity, ChatActivity::class.java)
            intentChatActivity.putExtra("user_name",item.user.name)
            intentChatActivity.putExtra("profile_image",item.user.profileImage)
            intentChatActivity.putExtra("other_uid",item.uid)
            activity!!.startActivity(intentChatActivity)
        }

    }
}
