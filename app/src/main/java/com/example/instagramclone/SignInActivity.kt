package com.example.instagramclone

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().currentUser!=null){
            val intent = Intent(this@SignInActivity,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val singUpButton = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.signUpButtonMainPage)
        singUpButton.setOnClickListener{
            startActivity(Intent(this,SignUpActivity::class.java))
            finish()
        }

        val loginBtn = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.loginButtonMainPage)
        loginBtn.setOnClickListener {
            loginUser()
        }

    }

    private fun loginUser() {
        val email = findViewById<EditText>(R.id.emailLogin).text.toString()
        val password = findViewById<EditText>(R.id.passwordLogin).text.toString()

        when{
            TextUtils.isEmpty(email) -> {
                Toast.makeText(this, "Email required", Toast.LENGTH_SHORT).show()
            }
            TextUtils.isEmpty(password) -> {
                Toast.makeText(this, "Password required", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val progressDialog = ProgressDialog(this@SignInActivity)
                progressDialog.setTitle("SignIn")
                progressDialog.setMessage("This may take a while...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val mAuth:FirebaseAuth = FirebaseAuth.getInstance()

                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener{
                            task ->
                        if(task.isSuccessful){
                            progressDialog.dismiss()

                            val intent = Intent(this@SignInActivity,MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
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
}