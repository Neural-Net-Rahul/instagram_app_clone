package com.example.instagramclone.Adapter

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.Fragments.ProfileFragment
import com.example.instagramclone.Model.User
import com.example.instagramclone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class UserAdapter(private var mContext: Context,
                  private var mUser:List<User>,
                  private var isFragment:Boolean = false) : RecyclerView.Adapter<UserAdapter.ViewHolder>(){

    private var firebaseUser:FirebaseUser? = FirebaseAuth.getInstance().currentUser
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_item_layout,parent,false)
        return UserAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {
        val user = mUser[position]
        holder.userName.text = user.getUserName()
        holder.fullName.text = user.getFullName()
        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile_icon).into(holder.profileImage)
        
        checkFollowingStatus(user.getUid(),holder.followBtn)

        holder.itemView.setOnClickListener{
            val bundle = Bundle()
            bundle.putString("keyId", user.getUid())

            val profileFragment = ProfileFragment()
            profileFragment.arguments = bundle

            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, profileFragment)
                .commit()
        }
        
        holder.followBtn.setOnClickListener{
            if(holder.followBtn.text.toString() == "Follow"){
                    firebaseUser?.uid.let { it1 ->
                        // it1 is current logged in user uid
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(user.getUid())
                            .setValue(true).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    firebaseUser?.uid.let { it1 ->
                                        FirebaseDatabase.getInstance().reference
                                            .child("Follow").child(user.getUid())
                                            .child("Followers").child(it1.toString())
                                            .setValue(true).addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    // We are good to go
                                                }
                                            }
                                    }
                                }
                            }
                    }
                }
            else{
                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it1.toString())
                        .child("Following").child(user.getUid())
                        .removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                firebaseUser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.getUid())
                                        .child("Followers").child(it1.toString())
                                        .removeValue().addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                // We are good to go
                                            }
                                        }
                                }
                            }
                        }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var userName:TextView = itemView.findViewById(R.id.userNameSearch)
        var fullName:TextView = itemView.findViewById(R.id.userFullNameSearch)
        var profileImage:de.hdodenhof.circleimageview.CircleImageView = itemView.findViewById(R.id.userProfileImageSearch)
        var followBtn:androidx.appcompat.widget.AppCompatButton = itemView.findViewById(R.id.userFollowButton)
    }

    private fun checkFollowingStatus(uid: String, followBtn: androidx.appcompat.widget.AppCompatButton) {
        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }
//        Log.d("result",followingRef.toString())
        followingRef.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child(uid).exists()){
                    followBtn.text = "Following"
                }
                else{
                    followBtn.text = "Follow"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}