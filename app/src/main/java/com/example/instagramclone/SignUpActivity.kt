package com.example.instagramclone

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.text.TextUtils
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

@Suppress("DEPRECATION")
class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val singInButton = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.logInsideButtonMainPage)
        singInButton.setOnClickListener{
            startActivity(Intent(this,SignInActivity::class.java))
            finish()
        }

        val singUpButton = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.signupButtonMainPage)
        singUpButton.setOnClickListener{
            CreateAccount()
        }
    }

    private fun CreateAccount() {
        val fullName = findViewById<EditText>(R.id.fullNameSignUp).text.toString()
        val userName = findViewById<EditText>(R.id.userNameSignUp).text.toString()
        val email = findViewById<EditText>(R.id.emailSignUp).text.toString()
        val password = findViewById<EditText>(R.id.passwordSignUp).text.toString()

        when{
            TextUtils.isEmpty(fullName) -> {
                Toast.makeText(this, "FullName required", Toast.LENGTH_SHORT).show()
            }
            TextUtils.isEmpty(userName) -> {
                Toast.makeText(this, "UserName required", Toast.LENGTH_SHORT).show()
            }
            TextUtils.isEmpty(email) -> {
                Toast.makeText(this, "Email required", Toast.LENGTH_SHORT).show()
            }
            TextUtils.isEmpty(password) -> {
                Toast.makeText(this, "Password required", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val progressDialog = ProgressDialog(this@SignUpActivity)
                progressDialog.setTitle("SignUp")
                progressDialog.setMessage("This may take a while...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val mAuth:FirebaseAuth = FirebaseAuth.getInstance()
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener{
                        task ->
                        if(task.isSuccessful){
                            saveUserInfo(fullName,userName,email,progressDialog)
                        }
                        else{
                            val message = task.exception!!.toString()
                            Toast.makeText(this, "Error : $message", Toast.LENGTH_SHORT).show()
                            mAuth.signOut()
                            progressDialog.dismiss()
                        }
                    }
            }
        }
    }

    private fun saveUserInfo(fullName: String, userName: String, email: String, progressDialog: ProgressDialog) {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String,Any>()
        userMap["uid"] = currentUserId
        userMap["fullName"] = fullName.lowercase()
        userMap["userName"] = userName.lowercase()
        userMap["email"] = email
        userMap["bio"] = "Add a bio"
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/instagram-app-clone-1af2b.appspot.com/o/defaultImage.jpg?alt=media&token=18a341b2-ed6a-4504-a8e7-4d5ab4dcd2f8"

        usersRef.child(currentUserId).setValue(userMap)
            .addOnCompleteListener{
                task ->
                if(task.isSuccessful){
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(currentUserId)
                        .child("Following").child(currentUserId)
                        .setValue(true)

                    progressDialog.dismiss()
                    Toast.makeText(this, "Your account has been created successfully", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@SignUpActivity,MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                else{
                    val message = task.exception!!.toString()
                    Toast.makeText(this, "Error : $message", Toast.LENGTH_SHORT).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }
            }
    }
}