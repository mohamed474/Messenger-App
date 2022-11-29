package com.example.messenger_1.recyclerview

import android.content.Context
import android.text.format.DateFormat
import com.example.messenger_1.R
import com.example.messenger_1.model.TextMessage
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.recipient_item_text_message.*

class RecipientTextMessageItem ( private val textMessage: TextMessage,
                                 private val messageID:String,
                                 val  context: Context): Item() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.text_view_message.text = textMessage.text
        viewHolder.text_view_time.text = DateFormat.format("hh:mm a", textMessage.data).toString()
    }

    override fun getLayout() = R.layout.recipient_item_text_message
}