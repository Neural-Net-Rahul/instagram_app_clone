package com.example.instagramclone

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage

class AddPostActivity : AppCompatActivity() {

    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storagePostPictureRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)

        storagePostPictureRef = FirebaseStorage.getInstance().reference.child("Post Pictures")

        CropImage.activity()
            .setAspectRatio(2,1)
            .start(this@AddPostActivity)

        findViewById<ImageView>(R.id.saveAddPostButton).setOnClickListener{
            UploadImage()
        }
        findViewById<ImageView>(R.id.closeAddPostButton).setOnClickListener{
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }

    private fun UploadImage(){
        when {
            imageUri == null ->{
                Toast.makeText(this, "Please upload your post", Toast.LENGTH_SHORT).show()
            }
            findViewById<EditText>(R.id.descriptionPost).text.toString() == "" -> {
                Toast.makeText(this, "Please say something about your post", Toast.LENGTH_SHORT).show()
            }
            else ->{
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Uploading Post")
                progressDialog.setMessage("Please wait, we are uploading the post...")
                progressDialog.show()

                val fileRef = storagePostPictureRef!!.child(System.currentTimeMillis().toString() + ".jpg")

                fileRef.putFile(imageUri!!)
                    .continueWithTask(Continuation<UploadTask.TaskSnapshot?, Task<Uri?>?> { task ->
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

                            val ref = FirebaseDatabase.getInstance().reference.child("Posts")
                            val postId = ref.push().key

                            val postMap = HashMap<String,Any>()
                            postMap["postId"] = postId.toString()
                            postMap["description"] = findViewById<EditText>(R.id.descriptionPost).text.toString()
                            postMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                            postMap["postImage"] = myUrl

                            ref.child(postId!!).updateChildren(postMap)

                            Toast.makeText(this, "Post has been added successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@AddPostActivity,MainActivity::class.java)
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
        if(requestCode== CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode== Activity.RESULT_OK && data!=null){
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            findViewById<ImageView>(R.id.imagePost).setImageURI(imageUri)
        }
    }
}