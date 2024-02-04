package com.example.instagramclone.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.Adapter.PostAdapter
import com.example.instagramclone.Adapter.PostDetailsAdapter
import com.example.instagramclone.Model.Post
import com.example.instagramclone.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PostDetailsFragment : Fragment() {

    private lateinit var postDetailsAdapter:PostDetailsAdapter
    private var postList:MutableList<Post>? = null
    private var recyclerView:RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_post_details, container, false)

        val postId = arguments?.getString("keyPostId")

        recyclerView = view.findViewById(R.id.recyclerviewPDL) // initialized after view is being inflated
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView?.layoutManager = linearLayoutManager

        postList = ArrayList()
        postDetailsAdapter = context?.let{ PostDetailsAdapter(it,postList as ArrayList<Post>)} !!
        recyclerView?.adapter = postDetailsAdapter

        if (postId != null) {
            showPost(postId)
        }
        return view;
    }

    private fun showPost(postId:String) {
        val followingRef = FirebaseDatabase.getInstance().reference.child("Posts")

        followingRef.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                        val post = snap.getValue(Post::class.java)
                        Log.d("check","first $postId")
                        Log.d("check","second ${post?.getPostId()}")
                        if(post?.getPostId() == postId){
                            postList?.add(post)
                        }
                    }
                    postDetailsAdapter?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}