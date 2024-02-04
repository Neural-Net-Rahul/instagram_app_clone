package com.example.instagramclone

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.instagramclone.Model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask.TaskSnapshot
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage

class AccountSettingsActivity : AppCompatActivity() {
    private val firebaseUser = FirebaseAuth.getInstance().currentUser!!
    private var checker = ""
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfilePictureRef:StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)
        storageProfilePictureRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")

        val logoutBtn = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.logOutButton)
        logoutBtn.setOnClickListener{

            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this@AccountSettingsActivity,SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        findViewById<TextView>(R.id.changeImageTextButton).setOnClickListener {
            checker = "clicked"
            CropImage.activity()
                .setAspectRatio(1,1)
                .start(this@AccountSettingsActivity)
        }

        findViewById<ImageView>(R.id.saveProfileInfoButton).setOnClickListener{
            if(checker=="clicked"){
                uploadImageAndUpdateInfo()
            }
            else{
                updateUserInfoOnly()
            }
        }
        findViewById<ImageView>(R.id.closeProfileButton).setOnClickListener{
            val intent = Intent(this@AccountSettingsActivity,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        userInfo()
    }

    private fun uploadImageAndUpdateInfo() {
        when {
            findViewById<EditText>(R.id.fullNameProfileFrag).text.toString() == "" -> {
                Toast.makeText(this, "Please mention your fullName", Toast.LENGTH_SHORT).show()
            }

            findViewById<EditText>(R.id.userNameProfileFrag).text.toString() == "" -> {
                Toast.makeText(this, "Please mention your userName", Toast.LENGTH_SHORT).show()
            }

            findViewById<EditText>(R.id.bioProfileFrag).text.toString() == "" -> {
                Toast.makeText(this, "Please mention your Bio", Toast.LENGTH_SHORT).show()
            }
            imageUri == null ->{
                Toast.makeText(this, "Please upload image", Toast.LENGTH_SHORT).show()
            }
            else ->{
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait, we are updating your profile...")
                progressDialog.show()

                val fileRef = storageProfilePictureRef!!.child(firebaseUser.uid + ".jpg")

                fileRef.putFile(imageUri!!)
                    .continueWithTask(Continuation<TaskSnapshot?, Task<Uri?>?> { task ->
                        if (!task.isSuccessful) {
                            throw task.exception!!
                        }
                        return@Continuation fileRef.downloadUrl
                    }).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val downloadUri = task.result
                            myUrl = downloadUri.toString()
//                            Log.d("check",myUrl)
//                            Log.d("check", imageUri.toString())

                            val ref = FirebaseDatabase.getInstance().reference.child("Users")
                            val userMap = HashMap<String,Any>()
                            userMap["fullName"] = findViewById<EditText>(R.id.fullNameProfileFrag).text.toString().lowercase()
                            userMap["userName"] = findViewById<EditText>(R.id.userNameProfileFrag).text.toString().lowercase()
                            userMap["bio"] = findViewById<EditText>(R.id.bioProfileFrag).text.toString().lowercase()
                            userMap["image"] = myUrl

                            ref.child(firebaseUser.uid).updateChildren(userMap)

                            Toast.makeText(this, "Account Information has been updated successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@AccountSettingsActivity,MainActivity::class.java)
                            startActivity(intent)
                            finish()
                            progressDialog.dismiss()
                        }
                        else{
                            progressDialog.dismiss()
                        }
                    }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode== Activity.RESULT_OK && data!=null){
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            findViewById<ImageView>(R.id.profileImageViewProfileFragment).setImageURI(imageUri)
        }
    }

    private fun updateUserInfoOnly() {
        when {
            findViewById<EditText>(R.id.fullNameProfileFrag).text.toString() == "" -> {
                Toast.makeText(this, "Please mention your fullName", Toast.LENGTH_SHORT).show()
            }
            findViewById<EditText>(R.id.userNameProfileFrag).text.toString() == "" -> {
                Toast.makeText(this, "Please mention your userName", Toast.LENGTH_SHORT).show()
            }
            findViewById<EditText>(R.id.bioProfileFrag).text.toString() == "" -> {
                Toast.makeText(this, "Please mention your Bio", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val usersRef: DatabaseReference =
                    FirebaseDatabase.getInstance().reference.child("Users")

                val userMap = HashMap<String, Any>()
                userMap["fullName"] =
                    findViewById<EditText>(R.id.fullNameProfileFrag).text.toString().lowercase()
                userMap["userName"] =
                    findViewById<EditText>(R.id.userNameProfileFrag).text.toString().lowercase()
                userMap["bio"] = findViewById<EditText>(R.id.bioProfileFrag).text.toString()

                usersRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(this, "Your account has been updated successfully", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@AccountSettingsActivity,MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun userInfo(){
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        usersRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user = snapshot.getValue(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile_icon).into(findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.profileImageViewProfileFragment))
                    findViewById<TextView>(R.id.fullNameProfileFrag)?.text = user.getFullName()
                    findViewById<TextView>(R.id.userNameProfileFrag)?.text = user.getUserName()
                    findViewById<TextView>(R.id.bioProfileFrag)?.text = user.getBio()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}