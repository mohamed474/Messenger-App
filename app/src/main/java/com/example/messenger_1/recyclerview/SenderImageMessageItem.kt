package com.example.messenger_1.recyclerview

import android.content.Context
import android.text.format.DateFormat
import com.example.messenger_1.R
import com.example.messenger_1.glide.GlideApp
import com.example.messenger_1.model.ImageMessage
import com.google.firebase.storage.FirebaseStorage
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.sender_item_image_message.*

class SenderImageMessageItem (private val imageMessage: ImageMessage,
                              private val messageID:String,
                              val  context: Context): Item() {

    private val storageInstance: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.textView_message_time.text = DateFormat.format("hh:mm a", imageMessage.data).toString()
        if (imageMessage.imagePath.isNotEmpty()) {
            GlideApp.with(context)
                .load(storageInstance.getReference(imageMessage.imagePath))
                .placeholder(R.drawable.ic_image_black_24)
                .into(viewHolder.imageView_message_image)
        }

    }

    override fun getLayout() = R.layout.sender_item_image_message
}