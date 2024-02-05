package com.example.instagramclone.Adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.CommentsActivity
import com.example.instagramclone.Fragments.ProfileFragment
import com.example.instagramclone.Fragments.SearchFragment
import com.example.instagramclone.Model.Post
import com.example.instagramclone.Model.User
import com.example.instagramclone.R
import com.example.instagramclone.ShowImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PostDetailsAdapter(private var mContext: Context,
                  private var mPost:List<Post>) : RecyclerView.Adapter<PostDetailsAdapter.ViewHolder>(){

    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.post_details_layout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = mPost[position]
        // Details of post(post.getters) have to be placed on their correct pos (holder.something), these posts came from database

        Picasso.get().load(post.getPostImage()).into(holder.postImage)
        publisherInfo(holder.profileImage,holder.userName,holder.publisher,post.getPublisher())

        holder.postImage.setOnClickListener {
            val intent = Intent(mContext,ShowImage::class.java)
            intent.putExtra("postImage",post.getPostImage())
            mContext.startActivity(intent)
        }

        isLikes(post.getPostId(),holder.likeButton)
        savePosts(post.getPostId(),holder.saveButton)
        numberOfLikes(holder.likes,post.getPostId())
        numberOfComments(holder.comments,post.getPostId())
        setDescription(holder.description,post.getPostId())

        holder.likeButton.setOnClickListener{
            if(holder.likeButton.tag == "Like"){
                FirebaseDatabase.getInstance().reference
                    .child("Likes").child(post.getPostId())
                    .child(firebaseUser?.uid!!).setValue(true)

                addNotifications(post.getPublisher(),post.getPostId())
            }
            else{
                FirebaseDatabase.getInstance().reference
                    .child("Likes").child(post.getPostId())
                    .child(firebaseUser?.uid!!).removeValue()
            }
        }
        holder.commentButton.setOnClickListener {
            val intent = Intent(mContext,CommentsActivity::class.java)
            intent.putExtra("postId",post.getPostId())
            intent.putExtra("publisherId",post.getPublisher())
            mContext.startActivity(intent)

        }
        holder.saveButton.setOnClickListener {
            if(holder.saveButton.tag == "Save"){
                FirebaseDatabase.getInstance().reference
                    .child("Saves").child(firebaseUser?.uid!!)
                    .child(post.getPostId()).setValue(true)
            }
            else{
                FirebaseDatabase.getInstance().reference
                    .child("Saves").child(firebaseUser?.uid!!)
                    .child(post.getPostId()).removeValue()
            }
        }
        holder.profileImage.setOnClickListener{
            val bundle = Bundle()
            bundle.putString("keyId", post.getPublisher())

            val profileFragment = ProfileFragment()
            profileFragment.arguments = bundle

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, profileFragment)
                .commit()
        }
        holder.userName.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("keyId", post.getPublisher())

            val profileFragment = ProfileFragment()
            profileFragment.arguments = bundle

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, profileFragment)
                .commit()
        }
        holder.likes.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("topic","likes")
            bundle.putString("postId",post.getPostId())

            val searchFragment = SearchFragment()
            searchFragment.arguments = bundle

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, searchFragment)
                .addToBackStack(null)
                .commit()
        }
        holder.comments.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("topic","comments")
            bundle.putString("postId",post.getPostId())

            val searchFragment = SearchFragment()
            searchFragment.arguments = bundle

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, searchFragment)
                .addToBackStack(null)
                .commit()
        }

    }

    private fun savePosts(postId: String, likeButton: ImageView) {
        FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid)
            .addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.child(postId).exists()){
                        likeButton.setImageResource(R.drawable.save_large_icon)
                        likeButton.tag = "Saved"
                    }
                    else{
                        likeButton.setImageResource(R.drawable.save_unfilled_large_icon)
                        likeButton.tag = "Save"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun numberOfComments(comments: TextView, postId: String) {
        FirebaseDatabase.getInstance().reference.child("Comments").child(postId)
            .addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        "${snapshot.childrenCount} comments".also { comments.text = it }
                    }
                    else{
                        "No comments".also { comments.text = it }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun getColoredSpanned(text: String, color: String): String? {
        return "<font color=$color>$text</font>"
    }

    private fun setDescription(description: TextView, postId: String) {
        FirebaseDatabase.getInstance().reference.child("Posts").child(postId).child("description")
            .addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name: String = getColoredSpanned("Description :", "#800000").toString()
                    description.setText(Html.fromHtml("$name ${snapshot.getValue(String::class.java).toString()}"));
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun numberOfLikes(likes: TextView, postId: String) {
        FirebaseDatabase.getInstance().reference.child("Likes").child(postId)
            .addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        "${snapshot.childrenCount} likes".also { likes.text = it }
                    }
                    else{
                        "No likes".also { likes.text = it }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun isLikes(postId: String, likeButton: ImageView) {
        FirebaseDatabase.getInstance().reference.child("Likes").child(postId)
            .addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.child(firebaseUser?.uid!!).exists()){
                        likeButton.setImageResource(R.drawable.heart_clicked)
                        likeButton.tag = "Liked"
                    }
                    else{
                        likeButton.setImageResource(R.drawable.heart_not_clicked)
                        likeButton.tag = "Like"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun publisherInfo(profileImage: CircleImageView, userName: TextView, publisher: TextView, publisherId: String) {
        FirebaseDatabase.getInstance().reference.child("Users").child(publisherId)
            .addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val user = snapshot.getValue(User::class.java)
                        Picasso.get().load(user?.getImage()).into(profileImage)
                        userName.text = user?.getUserName()
                        publisher.text = user?.getFullName()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

    }

    private fun addNotifications(userId:String,postId: String){
        val notifsRef = FirebaseDatabase.getInstance().reference
            .child("Notifications").child(userId)

        val notifsMap = HashMap<String,Any>()
        notifsMap["userId"] = firebaseUser!!.uid
        notifsMap["text"] = "liked your post"
        notifsMap["postId"] = postId
        notifsMap["isPost"] = true

        if(userId!= firebaseUser !!.uid) {
            notifsRef.push().setValue(notifsMap)
        }
    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var profileImage: de.hdodenhof.circleimageview.CircleImageView
        var postImage: ImageView
        var likeButton: ImageView
        var commentButton: ImageView
        var saveButton:ImageView
        var userName:TextView
        var likes: TextView
        var description:TextView
        var publisher:TextView
        var comments:TextView

        init {
            profileImage = itemView.findViewById(R.id.userProfileImagePDL)
            postImage = itemView.findViewById(R.id.postImagePDL)
            likeButton = itemView.findViewById(R.id.postImageLikeButtonPDL)
            commentButton = itemView.findViewById(R.id.postImageCommentButtonPDL)
            saveButton = itemView.findViewById(R.id.postSaveButtonPDL)
            userName = itemView.findViewById(R.id.userNamePDL)
            likes = itemView.findViewById(R.id.likesPDL)
            description = itemView.findViewById(R.id.descriptionPDL)
            publisher = itemView.findViewById(R.id.publisherPDL)
            comments = itemView.findViewById(R.id.commentsPDL)
        }
    }

}