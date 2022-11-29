package com.example.messenger_1.recyclerview

import android.content.Context
import com.example.messenger_1.R
import com.example.messenger_1.glide.GlideApp
import com.example.messenger_1.model.User
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.recycler_view_item.*


class ChatItem(val uid: String,
               val user: User,
               val context: Context): Item(){

    private val firestoreInstance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val currentUserDocRef : DocumentReference
    get() = firestoreInstance.document("users/$uid")

    private val storageInstance: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {


        viewHolder.item_time_textView.text = "Time"
        viewHolder.item_last_message_textView.text = "last message..."

       getCurrentUser{ user ->
           viewHolder.item_name_textView.text = user.name

           if (user.profileImage.isNotEmpty()){
               GlideApp.with(context)
                   .load(storageInstance.getReference(user.profileImage))
                   .into(viewHolder.item_circle_image_view)
           } else {
               viewHolder.item_circle_image_view.setImageResource(R.drawable.ic_account_circle)
           }
       } 

    }

    private fun getCurrentUser(onComplete: (User) -> Unit) {

        currentUserDocRef.get().addOnSuccessListener {
            onComplete(it.toObject(User::class.java)!!)
        }
    }

    override fun getLayout(): Int {
        return R.layout.recycler_view_item
    }
}
