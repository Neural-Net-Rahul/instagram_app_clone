package com.example.instagramclone.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.renderscript.Sampler.Value
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.Adapter.PostAdapter
import com.example.instagramclone.Adapter.UserAdapter
import com.example.instagramclone.Model.Comment
import com.example.instagramclone.Model.Post
import com.example.instagramclone.Model.User
import com.example.instagramclone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class SearchFragment : Fragment() {
    private var recyclerView:RecyclerView? = null
    private var userAdapter:UserAdapter? = null
    private var mUser:MutableList<User>? = null
    var loggedInUserFullName:Any? = null
    private val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
    private val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewSearch)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        mUser = ArrayList()
        userAdapter = context?.let{ UserAdapter(it,mUser as ArrayList<User>) }
        recyclerView?.adapter = userAdapter

        // Not Working

//        view.findViewById<EditText>(R.id.searchEditText).addTextChangedListener(object:TextWatcher{
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                val newText = p0.toString().lowercase()
//                this@SearchFragment.searchUser(newText)
//            }
//
//            override fun afterTextChanged(p0: Editable?) {
//                TODO("Not yet implemented")
//            }
//
//        })

        // loggedQuery contains reference of "fullName" of current logged in User
        val loggedQuery = usersRef.child(currentUserId).child("fullName")
        loggedQuery.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                loggedInUserFullName = snapshot.getValue(String::class.java).toString()
//                Log.d("Logged In","Zero : $loggedInUserFullName")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

//        Log.d("contains", arguments?.containsKey("topic").toString())
        if (arguments?.containsKey("topic") == true) {
            val topic = arguments?.getString("topic")
            if(topic=="likes"){
                view.findViewById<EditText>(R.id.searchEditText).visibility = View.GONE
                view.findViewById<Button>(R.id.searchBtn).visibility = View.GONE
                view.findViewById<TextView>(R.id.whoLikedPost).visibility = View.VISIBLE
                view.findViewById<TextView>(R.id.whoCommentedPost).visibility = View.GONE
                view.findViewById<TextView>(R.id.following).visibility = View.GONE
                view.findViewById<TextView>(R.id.followers).visibility = View.GONE
                showWhoLikedThePost(arguments?.getString("postId"))
            }
            else if(topic== "comments"){
                view.findViewById<EditText>(R.id.searchEditText).visibility = View.GONE
                view.findViewById<Button>(R.id.searchBtn).visibility = View.GONE
                view.findViewById<TextView>(R.id.whoLikedPost).visibility = View.GONE
                view.findViewById<TextView>(R.id.whoCommentedPost).visibility = View.VISIBLE
                view.findViewById<TextView>(R.id.following).visibility = View.GONE
                view.findViewById<TextView>(R.id.followers).visibility = View.GONE
                showWhoCommentedThePost(arguments?.getString("postId"))
            }
            else if(topic=="following"){
                view.findViewById<EditText>(R.id.searchEditText).visibility = View.GONE
                view.findViewById<Button>(R.id.searchBtn).visibility = View.GONE
                view.findViewById<TextView>(R.id.whoLikedPost).visibility = View.GONE
                view.findViewById<TextView>(R.id.whoCommentedPost).visibility = View.GONE
                view.findViewById<TextView>(R.id.following).visibility = View.VISIBLE
                view.findViewById<TextView>(R.id.followers).visibility = View.GONE
                showFollowing()
            }
            else{
                view.findViewById<EditText>(R.id.searchEditText).visibility = View.GONE
                view.findViewById<Button>(R.id.searchBtn).visibility = View.GONE
                view.findViewById<TextView>(R.id.whoLikedPost).visibility = View.GONE
                view.findViewById<TextView>(R.id.whoCommentedPost).visibility = View.GONE
                view.findViewById<TextView>(R.id.following).visibility = View.GONE
                view.findViewById<TextView>(R.id.followers).visibility = View.VISIBLE
                showFollowers()
            }
        }
        else {
            searchUser()
            view.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.searchBtn)
                .setOnClickListener {
                    searchUser(
                        view.findViewById<EditText>(R.id.searchEditText).text.toString().lowercase()
                    )
                }
        }

        return view
    }

    private fun showFollowers() {
        val followersList:MutableList<String> = ArrayList()
        FirebaseDatabase.getInstance().reference.child("Follow").child(currentUserId).child("Followers")
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(snap in snapshot.children){
                        followersList.add(snap.key.toString())
                    }
                    getUsers(followersList)
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun showFollowing() {
        val followingList:MutableList<String> = ArrayList()
        FirebaseDatabase.getInstance().reference.child("Follow").child(currentUserId).child("Following")
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(snap in snapshot.children){
                        followingList.add(snap.key.toString())
                    }
                    getUsers(followingList)
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun showWhoCommentedThePost(postId: String?) {
        val userIds:MutableList<String> = ArrayList()
        if (postId != null) {
            FirebaseDatabase.getInstance().reference.child("Comments").child(postId)
                .addValueEventListener(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(snap in snapshot.children){
                            val comment = snap.getValue(Comment::class.java)
                            userIds.add(comment!!.getPublisher())
                        }
                        getUsers(userIds)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }
    }

    private fun showWhoLikedThePost(postId: String?) {
        val userIds:MutableList<String> = ArrayList()
        if (postId != null) {
            FirebaseDatabase.getInstance().reference.child("Likes").child(postId)
                .addValueEventListener(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(snap in snapshot.children){
                            userIds.add(snap.key.toString())
                        }
                        getUsers(userIds)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }
    }

    private fun getUsers(userIds: MutableList<String>) {
        val query = FirebaseDatabase.getInstance().reference
            .child("Users")

        query.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUser?.clear()
                if(dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val user = snapshot.getValue(User::class.java)
//                        Log.d("seeUserIds","first ${user?.getUid()}")
//                        Log.d("seeUserIds","second $userIds")
                        if(user?.getUid().toString() in userIds) {
                            mUser?.add(user !!)
                        }
                    }
                }
                userAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun searchUser(input: String = "") {
        val query = if(input==""){
            FirebaseDatabase.getInstance().reference
                .child("Users")
                .orderByChild("fullName")
        } else {
            FirebaseDatabase.getInstance().reference
                .child("Users")
                .orderByChild("fullName")
                .startAt(input)
                .endAt(input + "\uf8ff")
        }

        query.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUser?.clear()
                if(dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val user = snapshot.getValue(User::class.java)
                        val currentUserFullName = user?.getFullName().toString()
//                        Log.d("Logged In", "Second : $currentUserFullName")
//                        if(currentUserFullName != loggedInUserFullName && user!=null){
//                            mUser?.add(user)
//                        }
                        mUser?.add(user!!)
                    }
                }
                userAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

}