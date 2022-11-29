package com.example.messenger_1

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.messenger_1.glide.GlideApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.ByteArrayOutputStream
import java.util.*

@Suppress("DEPRECATION")
class ProfileActivity : AppCompatActivity() {

    companion object{
        val RC_SELECT_IMAGE = 2
    }
    private lateinit var userName:String

    private val firestoreInctance:FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val currentUserDocRef: DocumentReference
    get() = firestoreInctance.document("users/${FirebaseAuth.getInstance().currentUser?.uid.toString()}")


    private val storageInstance: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val currentUserStorageRef: StorageReference
        get() = storageInstance.reference.child(FirebaseAuth.getInstance().currentUser?.uid.toString())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        btn_sign_out.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intentSignInActivity = Intent(this@ProfileActivity, SignInActivity::class.java)
            intentSignInActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intentSignInActivity)
        }

        setSupportActionBar(toolbar_activity_profile)
        supportActionBar?.title = "Me"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getUserInfo {user ->
            userName = user.name
            username_view.text = user.name

            if (user.profileImage.isNotEmpty()) {
                GlideApp.with(this@ProfileActivity)
                    .load(storageInstance.getReference(user.profileImage))
                    .placeholder(R.drawable.ic_account_circle)
                    .into(username_img)
            }
        }

        username_img.setOnClickListener {
            val myIntentImage = Intent(Intent()).apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg","image/png"))
            }
            startActivityForResult(Intent.createChooser(myIntentImage,"Select Image"),RC_SELECT_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.data != null){

            progress_profile.visibility = View.VISIBLE

            val selectedImagePath = data.data
            val selectedImageBmp = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImagePath)
            val outputStream = ByteArrayOutputStream()
            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG,100,outputStream)
            val selectedImageBytes = outputStream.toByteArray()
            username_img.setImageURI(data.data)

            uploadProfileImage(selectedImageBytes){path ->
                val userFieldMap = mutableMapOf<String,Any>()
                userFieldMap["name"] = userName
                userFieldMap["profileImage"] = path
                currentUserDocRef.update(userFieldMap)
            }
        }
    }
       private fun uploadProfileImage(selectedImageBytes: ByteArray , onSucces:(imagePath:String) -> Unit) {
            val ref = currentUserStorageRef.child("profilePictures/${UUID.nameUUIDFromBytes(selectedImageBytes)}")
               ref.putBytes(selectedImageBytes).addOnCompleteListener{
             if (it.isSuccessful){
                 onSucces(ref.path)
                 progress_profile.visibility = View.GONE
             } else {
              Toast.makeText(this@ProfileActivity,"Error : ${it.exception?.message.toString()}",Toast.LENGTH_LONG).show()
          }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return false
    }

    private fun getUserInfo(onComplete: (com.example.messenger_1.model.User) -> Unit){
        currentUserDocRef.get().addOnSuccessListener {
            onComplete(it.toObject(com.example.messenger_1.model.User::class.java)!!)
        }
    }

}