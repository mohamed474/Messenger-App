package com.example.messenger_1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.example.messenger_1.fragments.CallFragment
import com.example.messenger_1.fragments.ChatFragment
import com.example.messenger_1.fragments.PeopleFragment
import com.example.messenger_1.glide.GlideApp
import com.example.messenger_1.model.User
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener{

    private val mAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val mChatFragment    = ChatFragment()
    private val mCallFragment    = CallFragment()
    private val mPeopleFragment  = PeopleFragment()


    private val firestoreInstance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val storageInstance: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firestoreInstance.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .get()
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)

                if (user!!.profileImage.isNotEmpty()){
                    GlideApp.with(this@MainActivity)
                        .load(storageInstance.getReference(user.profileImage))
                        .into(activity_profile_img)
                } else {
                    activity_profile_img.setImageResource(R.drawable.ic_account_circle)
                }
            }



        setSupportActionBar(toolbar_main)
        supportActionBar?.title = ""

        bottomNavigationView_main.setOnItemSelectedListener (this@MainActivity)
        setFragment(mChatFragment)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {

            R.id.navigation_chat -> {
                setFragment(mChatFragment)
                return true
            }

            R.id.navigation_call -> {
                setFragment(mCallFragment)
                return true
            }

            R.id.navigation_people -> {
                setFragment(mPeopleFragment)
                return true
            }

            else -> return false
        }
    }

    private fun setFragment(fragment: Fragment) {
        val fr = supportFragmentManager.beginTransaction()
        fr.replace(R.id.coordinatorLayout_main_content,fragment)
        fr.commit()

    }

}