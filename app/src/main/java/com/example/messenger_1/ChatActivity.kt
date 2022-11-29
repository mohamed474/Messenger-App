package com.example.messenger_1

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.messenger_1.glide.GlideApp
import com.example.messenger_1.model.ImageMessage
import com.example.messenger_1.model.Message
import com.example.messenger_1.model.MessageType
import com.example.messenger_1.model.TextMessage
import com.example.messenger_1.recyclerview.RecipientImageMessageItem
import com.example.messenger_1.recyclerview.RecipientTextMessageItem
import com.example.messenger_1.recyclerview.SenderImageMessageItem
import com.example.messenger_1.recyclerview.SenderTextMessageItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.activity_chat.*
import java.io.ByteArrayOutputStream
import java.util.*

@Suppress("DEPRECATION")
class ChatActivity : AppCompatActivity() {

    private lateinit var mCurrentChatChannelId: String

    private val storageInstance: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }
    private val firestoreInstance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val chatChannelCollectionRef = firestoreInstance.collection("chatChannels")
    private val currentImageRef : StorageReference
    get() = storageInstance.reference

    // vars
    private var mRecipientId = ""
    private var mCurrentUserId = FirebaseAuth.getInstance().currentUser!!.uid
    private val messageAdapter by lazy { GroupAdapter<ViewHolder>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        imageView_back.setOnClickListener {
            finish()
        }

        val userName = intent.getStringExtra("user_name")
        val profileImage = intent.getStringExtra("profile_image")
        mRecipientId = intent.getStringExtra("other_uid")!!
        textView_user_name.text = userName

        send_image.setOnClickListener {
            val myIntentImage = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
            }
            startActivityForResult(Intent.createChooser(myIntentImage, "Select Image"), 2)
        }

        createChatChannel { channelId ->

            mCurrentChatChannelId = channelId

            getMessage(channelId)
            image_view_send.setOnClickListener {
                val text = editText_message.text.toString()
                if (text.isNotEmpty()){
                    val messageSend = TextMessage(text, mCurrentUserId,mRecipientId , Calendar.getInstance().time)
                    sentMessage(channelId, messageSend)
                    editText_message.setText("")
                } else {
                    Toast.makeText(this@ChatActivity,"Empty", Toast.LENGTH_LONG).show()
                }
            }
        }

        chat_recyclerView.apply {
            adapter = messageAdapter
        }

        if (profileImage!!.isNotEmpty()){
            GlideApp.with(this@ChatActivity)
                .load(storageInstance.getReference(profileImage))
                .into(imageView_profile_img)
        } else {
            imageView_profile_img.setImageResource(R.drawable.ic_account_circle)
        }
    }

    private fun sentMessage(channelId: String,messageSend: Message) {
        chatChannelCollectionRef.document(channelId).collection("messages").add(messageSend)
    }

    private fun createChatChannel(onComplete:(channelId: String) -> Unit){
        firestoreInstance.collection("users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection("sharedChat")
            .document(mRecipientId)
            .get()
            .addOnSuccessListener {
                if (it.exists()){
                    onComplete(it["channelId"] as String)
                    return@addOnSuccessListener
                }
        val newChatChannel = firestoreInstance.collection("users").document()

        firestoreInstance.collection("users")
            .document(mRecipientId)
            .collection("sharedChat")
            .document(mCurrentUserId)
            .set(mapOf("channelId" to newChatChannel.id))

        firestoreInstance.collection("users")
            .document(mCurrentUserId)
            .collection("sharedChat")
            .document(mRecipientId)
            .set(mapOf("channelId" to newChatChannel.id))

        onComplete(newChatChannel.id)
            }
    }

    private fun getMessage(channelID: String) {
        
        val query = chatChannelCollectionRef.document(channelID).collection("messages")
            .orderBy("data", Query.Direction.DESCENDING)
        query.addSnapshotListener {querySnapshot, firebaseFirestoreException ->

            messageAdapter.clear()
            querySnapshot!!.documents.forEach { document ->

                if (document["type"] == MessageType.TEXT){

                    val textMessage = document.toObject(TextMessage::class.java)

                    if (textMessage?.senderId == mCurrentUserId) {
                        messageAdapter.add(SenderTextMessageItem(document.toObject(TextMessage::class.java)!!, document.id,this@ChatActivity))
                    } else {
                        messageAdapter.add(RecipientTextMessageItem(document.toObject(TextMessage::class.java)!!, document.id,this@ChatActivity))
                    }

                } else {

                    val imageMessage = document.toObject(ImageMessage::class.java)

                    if (imageMessage?.senderId == mCurrentUserId){
                        messageAdapter.add(SenderImageMessageItem(document.toObject(ImageMessage::class.java)!!, document.id, this@ChatActivity))
                    } else {
                        messageAdapter.add(RecipientImageMessageItem(document.toObject(ImageMessage::class.java)!!, document.id, this@ChatActivity))
                    }

                }

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null && data.data != null ){

            val selectedImagePath = data.data
            val selectedImageBmp = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImagePath)
            val outputStream = ByteArrayOutputStream()
            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 25, outputStream)
            val selectedImageBytes = outputStream.toByteArray()

            uploadImage(selectedImageBytes){ path ->
                val imageMessage = ImageMessage(path, mCurrentUserId, mRecipientId, Calendar.getInstance().time)

                //chatChannelCollectionRef.document(mCurrentChatChannelId).collection("messages").add(imageMessage)

                sentMessage(mCurrentChatChannelId, imageMessage)
            }
        }
    }

    private fun uploadImage(selectedImageBytes: ByteArray, onSucced: (imagePath: String) -> Unit) {
        val ref = currentImageRef.child("${FirebaseAuth.getInstance().currentUser!!.uid}/images/${UUID.nameUUIDFromBytes(selectedImageBytes)}")
        ref.putBytes(selectedImageBytes)
            .addOnCompleteListener{
                if (it.isSuccessful){
                    onSucced(ref.path)
                    Toast.makeText(this, "Done", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "error", Toast.LENGTH_LONG).show()
                }
            }
    }
}