package com.example.instagramclone.Fragments

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.renderscript.Sampler.Value
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.AccountSettingsActivity
import com.example.instagramclone.Adapter.MyImagesAdapter
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
import java.util.Collections


class ProfileFragment : Fragment() {
    private lateinit var profileId:String
    private var firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
    lateinit var upButton:Button
    private var postList:MutableList<Post>? = null
    private var saveList:MutableList<Post>? = null
    private var imageAdapter:MyImagesAdapter? = null
    private var saveImageAdapter:MyImagesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        if (arguments?.containsKey("keyId") == true) {
            val userId = arguments?.getString("keyId")
            if (userId != null) {
                profileId = userId
            }
        } else {
            profileId = firebaseUser.uid
        }

        view.findViewById<LinearLayout>(R.id.followingSection).setOnClickListener{
            // who i am following
            val bundle = Bundle()
            bundle.putString("topic","following")

            val searchFragment = SearchFragment()
            searchFragment.arguments = bundle

            (context as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, searchFragment)
                .addToBackStack(null)
                .commit()
        }
        view.findViewById<LinearLayout>(R.id.followersSection).setOnClickListener{
            // who are my followers
            val bundle = Bundle()
            bundle.putString("topic","followers")

            val searchFragment = SearchFragment()
            searchFragment.arguments = bundle

            (context as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, searchFragment)
                .addToBackStack(null)
                .commit()
        }

        val recyclerView:RecyclerView = view.findViewById(R.id.recyclerUploadPic)
        recyclerView.setHasFixedSize(true)
        val layoutManager = GridLayoutManager(context,3)
        recyclerView.layoutManager = layoutManager
        postList = ArrayList()
        imageAdapter = context?.let{ MyImagesAdapter(it,postList as ArrayList<Post>,profileId) }
        recyclerView.adapter = imageAdapter


        val saveRecyclerView:RecyclerView = view.findViewById(R.id.recyclerSavedPic)
        saveRecyclerView.setHasFixedSize(true)
        val saveLayoutManager = GridLayoutManager(context,3)
        saveRecyclerView.layoutManager = saveLayoutManager
        saveList = ArrayList()
        saveImageAdapter = context?.let{ MyImagesAdapter(it,saveList as ArrayList<Post>,profileId) }
        saveRecyclerView.adapter = saveImageAdapter

        if(profileId!=firebaseUser.uid){
            val saveImageBtn : ImageView = view.findViewById(R.id.imagesSaveBtn);
            saveImageBtn.isEnabled = false
            saveImageBtn.alpha = 0.1f
        }

        recyclerView.visibility = View.VISIBLE
        saveRecyclerView.visibility = View.GONE

        view.findViewById<ImageView>(R.id.imageGridViewBtn).setOnClickListener {
            recyclerView.visibility = View.VISIBLE
            saveRecyclerView.visibility = View.GONE
        }
        view.findViewById<ImageView>(R.id.imagesSaveBtn).setOnClickListener {
            recyclerView.visibility = View.GONE
            saveRecyclerView.visibility = View.VISIBLE
        }


        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        upButton = view?.findViewById<Button>(R.id.editAccountSettingsButton)!!
        if(profileId == firebaseUser.uid){
            upButton.text = "Edit Profile"
        }
        else{
            checkFollowAndFollowing()
        }

        view.findViewById<Button>(R.id.editAccountSettingsButton).setOnClickListener {
            when {
                upButton.text == "Edit Profile" -> {
                    startActivity(Intent(context, AccountSettingsActivity::class.java))
                }

                upButton.text == "Follow" -> {
                    firebaseUser.uid.let { it ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it)
                            .child("Following").child(profileId)
                            .setValue(true)
                    }
                    firebaseUser.uid.let { it ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it)
                            .setValue(true)
                    }
                    addNotifications(profileId)
                }

                upButton.text == "Following" -> {
                    firebaseUser.uid.let { it ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it)
                            .child("Following").child(profileId)
                            .removeValue()
                    }
                    firebaseUser.uid.let { it ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it)
                            .removeValue()
                    }
                }

            }
        }

        getFollowers()
        getFollowing()
        userInfo()
        myPhotos()
        mySavedPhotos()
        totalPosts()

        return view
    }

    private fun totalPosts() {
        FirebaseDatabase.getInstance().reference.child("Posts")
            .addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var count = 0;
                    for(snap in snapshot.children) run {
                        val post = snap.getValue(Post::class.java)
                        if(profileId==post?.getPublisher()){
                            count += 1;
                        }
                    }
                    view?.findViewById<TextView>(R.id.total_posts)?.text = count.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun myPhotos() {
        FirebaseDatabase.getInstance().reference.child("Posts")
            .addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    (postList as ArrayList<Post>).clear()
                    for(snap in snapshot.children){
                        val post = snap.getValue(Post::class.java)
//                        Log.d("task","post : ${post!!.getPostId()}")
//                        Log.d("task","uid : ${firebaseUser.uid}")
                        if(post?.getPublisher()==profileId){
                            postList!!.add(post)
                        }
                    }
                    postList?.let { Collections.reverse(it) }
                    imageAdapter!!.notifyDataSetChanged()

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun mySavedPhotos() {
        val saveIdList: MutableList<String> = ArrayList()  // Initialize the list
        FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        saveIdList.clear()  // Clear the list before adding new values
                        for (snap in snapshot.children) {
                            Log.d("snap",snap.key!!)
                            saveIdList.add(snap.key ?: "")
                        }
                        retrieveSavedPhotos(saveIdList)  // Call the function to retrieve saved photos
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled if needed
                }
            })
    }

    private fun retrieveSavedPhotos(saveIdList: List<String>) {
        FirebaseDatabase.getInstance().reference.child("Posts")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    (saveList as ArrayList<Post>).clear()
                    for (snap in snapshot.children) {
                        val post = snap.getValue(Post::class.java)
                        if (post?.getPostId() in saveIdList) {
                            saveList?.add(post!!)
                        }
                    }
                    saveList?.let { Collections.reverse(it) }
                    saveImageAdapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled if needed
                }
            })
    }

    private fun checkFollowAndFollowing() {
        val followingRef = firebaseUser.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1)
                .child("Following")
        }
        followingRef.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child(profileId).exists()){
                    upButton.text = "Following"
                }
                else{
                    upButton.text = "Follow"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    private fun getFollowers(){
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Followers")

        followersRef.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    view?.findViewById<TextView>(R.id.total_followers)?.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun getFollowing(){
        val followingRef =
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Following")

        followingRef.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    view?.findViewById<TextView>(R.id.total_following)?.text= (snapshot.childrenCount - 1).toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun userInfo(){
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(profileId)
        usersRef.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(context!=null){
                    if(snapshot.exists()){
                        val user = snapshot.getValue(User::class.java)
                        Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile_icon).into(view?.findViewById(R.id.proImageProfileFragment))
                        view?.findViewById<TextView>(R.id.profileFragmentUsername)?.text = user.getUserName()
                        view?.findViewById<TextView>(R.id.fullNameProfileFragment)?.text = user.getFullName()
                        view?.findViewById<TextView>(R.id.bioProfileFragment)?.text = user.getBio()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    private fun addNotifications(userId:String){
        val notifsRef = FirebaseDatabase.getInstance().reference
            .child("Notifications").child(userId)

        val notifsMap = HashMap<String,Any>()
        notifsMap["userId"] = firebaseUser.uid
        notifsMap["text"] = "started following you"
        notifsMap["postId"] = ""
        notifsMap["isPost"] = false

        if(userId!=firebaseUser.uid) {
            notifsRef.push().setValue(notifsMap)
        }
    }
}