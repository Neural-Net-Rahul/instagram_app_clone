package com.example.instagramclone.Adapter

import android.content.Context
import android.renderscript.Sampler.Value
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.Model.Comment
import com.example.instagramclone.Model.User
import com.example.instagramclone.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class CommentAdapter(private val mContext: Context,
                    private val mComment:MutableList<Comment>): RecyclerView.Adapter<CommentAdapter.ViewHolder>(){
    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var userImage:CircleImageView = itemView.findViewById(R.id.userProfileImageCIL)
        var userName: TextView = itemView.findViewById(R.id.usernameCIL)
        var comment:TextView = itemView.findViewById(R.id.commentCIL)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.comment_item_layout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mComment.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment_db = mComment[position]
        holder.comment.text = comment_db.getComment()

        val publisher = comment_db.getPublisher()
        getUserImageAndUserName(holder.userName,holder.userImage,publisher)
    }

    private fun getUserImageAndUserName(userName: TextView, userImage: CircleImageView, publisher: String) {
        FirebaseDatabase.getInstance().reference.child("Users").child(publisher)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val user = snapshot.getValue(User::class.java)
                        userName.text = user?.getUserName().toString()
                        Picasso.get().load(user?.getImage()).into(userImage)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

}