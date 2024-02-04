package com.example.instagramclone.Adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.Fragments.PostDetailsFragment
import com.example.instagramclone.Model.Post
import com.example.instagramclone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso

class MyImagesAdapter(private var mContext: Context,
                  private var mPost:List<Post>,private var profileId:String) : RecyclerView.Adapter<MyImagesAdapter.ViewHolder>(){

    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.images_item_layout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = mPost[position]
        Picasso.get().load(post.getPostImage()).into(holder.postImage)

        holder.postImage.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("keyId", profileId)
            bundle.putString("keyPostId",post.getPostId())

            val postDetailsFragment = PostDetailsFragment()
            postDetailsFragment.arguments = bundle

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, postDetailsFragment)
                .addToBackStack(null) // to retain previous page
                .commit()
        }
    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var postImage = itemView.findViewById<ImageView>(R.id.postImage)
    }

}