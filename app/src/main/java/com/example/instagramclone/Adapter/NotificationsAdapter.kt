package com.example.instagramclone.Adapter

import android.content.Context
import android.renderscript.Sampler.Value
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.Model.Notifications
import com.example.instagramclone.Model.Post
import com.example.instagramclone.Model.User
import com.example.instagramclone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class NotificationsAdapter(private var mContext: Context,
                  private var mNotifications:List<Notifications>) : RecyclerView.Adapter<NotificationsAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.notifications_item_layout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mNotifications.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = mNotifications[position]
        holder.text.text = notification.getText()

        getUserNameAndProfileImage(holder.profileImage,holder.userName,notification.getUserId())
        if(notification.getIsPost()){
            holder.postImage.visibility = View.VISIBLE
            getPostImage(holder.postImage,notification.getPostId())
        }
    }

    private fun getPostImage(postImage: ImageView , postId: String) {
        FirebaseDatabase.getInstance().reference.child("Posts").child(postId)
            .addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        val post = snapshot.getValue(Post::class.java)
                        Picasso.get().load(post !!.getPostImage()).into(postImage)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun getUserNameAndProfileImage(profileImage: CircleImageView , userName: TextView , userId: String) {
        FirebaseDatabase.getInstance().reference.child("Users")
            .child(userId)
            .addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val user = snapshot.getValue(User::class.java)
                        userName.text = user!!.getUserName()
                        Picasso.get().load(user.getImage()).into(profileImage)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var postImage:ImageView
        var profileImage:CircleImageView
        var userName: TextView
        var text:TextView
        init{
            postImage = itemView.findViewById(R.id.postImageSmallView)
            profileImage = itemView.findViewById(R.id.profileImage)
            userName = itemView.findViewById(R.id.userNameNIL)
            text = itemView.findViewById(R.id.contentNIL)
        }
    }

}