package com.example.instagramclone.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.Model.Story
import com.example.instagramclone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import de.hdodenhof.circleimageview.CircleImageView

class StoryAdapter(private var mContext: Context,
                  private var mStory:List<Story>) : RecyclerView.Adapter<StoryAdapter.ViewHolder>(){

    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if(viewType == 0){
            val view = LayoutInflater.from(mContext).inflate(R.layout.add_story_item,parent,false)
            ViewHolder(view)
        } else{
            val view = LayoutInflater.from(mContext).inflate(R.layout.story_item,parent,false)
            ViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return mStory.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = mStory[position]
    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        // story_item
        var story_image : CircleImageView? = null
        var story_image_seen : CircleImageView? = null
        var story_userName : TextView? = null

        // add_story_item
        var add_button : CircleImageView? = null
        var add_story_text : TextView? = null

        init{
            // story_item
            story_image = itemView.findViewById(R.id.storyImage)
            story_image_seen = itemView.findViewById(R.id.storyImageSeen)
            story_userName = itemView.findViewById(R.id.storyUserName)

            // add_story_item
            add_button = itemView.findViewById(R.id.addButton)
            add_story_text = itemView.findViewById(R.id.addStory)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(position == 0){
            return 0
        }
        return 1
    }

}