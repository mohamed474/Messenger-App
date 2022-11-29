package com.example.messenger_1.model

import java.util.*

class ImageMessage(val imagePath: String,
                   override val senderId: String,
                   override val recipientId: String,
                   override val data: Date,
                   override val type: String = MessageType.IMAGE): Message {

    constructor(): this("","","",Date(0))
}