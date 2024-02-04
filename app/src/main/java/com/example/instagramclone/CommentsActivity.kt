package com.example.instagramclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.Adapter.CommentAdapter
import com.example.instagramclone.Model.Comment
import com.example.instagramclone.Model.Post
import com.example.instagramclone.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class CommentsActivity : AppCompatActivity() {

    private var postId = ""
    private var publisherId = ""
    private lateinit var firebaseUser : FirebaseUser
    private var commentAdapter: CommentAdapter? = null
    private var commentList:MutableList<Comment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        postId = intent.getStringExtra("postId").toString()
        publisherId = intent.getStringExtra("publisherId").toString()

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val recyclerView:RecyclerView = findViewById(R.id.recyclerViewComment)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        commentList = ArrayList()
        commentAdapter = CommentAdapter(this,commentList!!)

        recyclerView.adapter = commentAdapter

        val showImgPost = findViewById<ImageView>(R.id.postImageComment)
        showPostImage(postId,showImgPost)

        findViewById<TextView>(R.id.postComment).setOnClickListener {
            if(findViewById<EditText>(R.id.commentText).text.toString() == ""){
                Toast.makeText(this, "You must write something, it's necessary", Toast.LENGTH_SHORT).show()
            }
            else{
                addComment()
            }
        }

        retrieveUserInfo()
        readComments()
    }

    private fun showPostImage(postId: String, showImgPost: ImageView?) {
        FirebaseDatabase.getInstance().reference.child("Posts").child(postId)
            .addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val post = snapshot.getValue(Post::class.java)
                        Picasso.get().load(post?.getPostImage()).into(showImgPost)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun readComments() {
        FirebaseDatabase.getInstance().reference.child("Comments").child(postId)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(snap in snapshot.children){
                        val comment = snap.getValue(Comment::class.java)
                        commentList?.add(comment!!)
                    }
                    commentAdapter!!.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun retrieveUserInfo() {
        FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val user = snapshot.getValue(User::class.java)
                        Picasso.get().load(user?.getImage()).into(findViewById<ImageView>(R.id.profileImageComment))
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun addComment(){
        val commentRef = FirebaseDatabase.getInstance().reference
            .child("Comments").child(postId)

        val commentsMap = HashMap<String,Any>()
        commentsMap["comment"] = findViewById<EditText>(R.id.commentText).text.toString()
        commentsMap["publisher"] = firebaseUser.uid

        commentRef.push().setValue(commentsMap)

        findViewById<EditText>(R.id.commentText).text.clear()
    }
}