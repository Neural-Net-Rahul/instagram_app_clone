package com.example.instagramclone.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.renderscript.Sampler.Value
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.Adapter.PostAdapter
import com.example.instagramclone.Adapter.StoryAdapter
import com.example.instagramclone.Model.Post
import com.example.instagramclone.Model.Story
import com.example.instagramclone.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private var postAdapter:PostAdapter? = null
    private var postList:MutableList<Post>? = null
    private var storyAdapter: StoryAdapter? = null
    private var storyList:MutableList<Story>? = null
    private var followingList:MutableList<String>? = null
    private var recyclerView:RecyclerView? = null
    private var recyclerViewStory:RecyclerView? = null
    private var shimmerFrameLayout:ShimmerFrameLayout? = null
    private var shimmerFrameLayoutStory:ShimmerFrameLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        shimmerFrameLayout = view.findViewById(R.id.shimmerFrameLayout)
        shimmerFrameLayout?.run {
            Log.d("shimmer","First shimmer working")
            startShimmer()
        }

        shimmerFrameLayoutStory = view.findViewById(R.id.shimmerFrameLayoutStory)
        shimmerFrameLayoutStory?.run {
            Log.d("shimmer","Second shimmer working")
            startShimmer()
        }

        recyclerView = view.findViewById(R.id.recyclerViewHome) // initialized after view is being inflated
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView?.layoutManager = linearLayoutManager
        recyclerView?.visibility = View.GONE
        
        postList = ArrayList()
        postAdapter = context?.let{PostAdapter(it,postList as ArrayList<Post>)}
        recyclerView?.adapter = postAdapter

        recyclerViewStory = view.findViewById(R.id.recyclerViewStory) // initialized after view is being inflated
        val linearLayoutManagerStory = LinearLayoutManager(context)
        linearLayoutManagerStory.reverseLayout = true
        linearLayoutManagerStory.stackFromEnd = true
        recyclerViewStory?.layoutManager = linearLayoutManagerStory
        recyclerViewStory?.visibility = View.GONE

        storyList = ArrayList()
        storyAdapter = context?.let{StoryAdapter(it,storyList as ArrayList<Story>)}
        recyclerViewStory?.adapter = storyAdapter

        checkFollowings()

        return view;
    }

    private fun checkFollowings() {
        followingList = ArrayList()

        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Following")

        followingRef.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    (followingList as ArrayList<String>).clear()
                    for(snap  in snapshot.children){
                        snap.key?.let{(followingList as ArrayList<String>).add(it)}
//                        snap.key?.let { Log.d("task1", "up : $it") }
                        /*
                        EMAEIfkNiKZur0VQgpMDn3Pn8PF2
                        hfBKpoPVxVWxoAxI22gpB3ddqFL2
                        odlFx5rK5PShha4vXjSt4xZAlEg2
                         */
                    }

                    // we will show the posts of only those whom we are following
                    retrievePosts()
                    retrieveStories()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
/*
First time retrieveStories() is called, it will see snapshot does not exist so will call the
else condition, if there were no if else condition then due to initial call it will execute inside
on Data change but will no create path. Suppose at some other place we have done setValue(true),
now there will be a change in database, because of which "here" onDataChange will be called and
snapshot also exists. When you open app again, due to initial call it will be snapshot exists and
will apply that content and if there will be any change in db, then also onDataChange will be
called.
 */
    private fun retrieveStories(){
        FirebaseDatabase.getInstance().reference.child("Story")
            .addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
//                    Log.d("homeFrag","Inside")
                    val timeCurrent = System.currentTimeMillis()
                    (storyList as ArrayList<Story>).clear()
                    (storyList as ArrayList<Story>).add(
                        Story(
                            "" ,
                            0 ,
                            0 ,
                            "" ,
                            FirebaseAuth.getInstance().currentUser !!.uid
                        )
                    )
                    for (id in followingList !!) {
                        var countStory = 0
                        var story : Story? = null
                        for (snap in snapshot.child(id).children) {
//                            Log.d("homeFrag","In this")
                            // for first time snapshot does not exist so it will not enter this for loop
                            story = snap.getValue(Story::class.java)
                            if (timeCurrent > story !!.getTimeStart() && timeCurrent < story.getTimeEnd()) {
                                countStory ++;
                            }
                        }
                        if (countStory > 0) {
                            (storyList as ArrayList<Story>).add(story !!)
                        }
                    }
                    storyAdapter !!.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postsRef.addValueEventListener(object:ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                postList?.clear()
//                Log.d("task1", "mt: ${followingList?.size}")
//                for(value in followingList!!){
//                    Log.d("task1","value : $value")
//                }
                for(snap in snapshot.children){
                    val post = snap.getValue(Post::class.java)
                    for(id in followingList!! as ArrayList<String>){
//                        Log.d("task1","me $id")
//                        Log.d("task1", "ot: ${post?.getPublisher()}")
                        if(id == post?.getPublisher()){
//                            Log.d("task1","kt: ${post.getDescription()}")
                            postList?.add(post);
                        }
//                        Log.d("task1", "nt: ${postList?.size}")
                    }
                }
                postAdapter?.notifyDataSetChanged()
                postAdapter = context?.let{PostAdapter(it,postList as ArrayList<Post>)}
                recyclerView?.adapter = postAdapter

                shimmerFrameLayout?.run { stopShimmer() }
                shimmerFrameLayoutStory?.run {stopShimmer()}
                shimmerFrameLayout?.visibility = View.GONE
                shimmerFrameLayoutStory?.visibility = View.GONE
                recyclerView?.visibility = View.VISIBLE
                recyclerViewStory?.visibility = View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}