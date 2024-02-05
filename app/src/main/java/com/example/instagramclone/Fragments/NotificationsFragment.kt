package com.example.instagramclone.Fragments

import android.os.Bundle
import android.renderscript.Sampler.Value
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramclone.Adapter.NotificationsAdapter
import com.example.instagramclone.Adapter.PostDetailsAdapter
import com.example.instagramclone.Model.Notifications
import com.example.instagramclone.Model.Post
import com.example.instagramclone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Collections

class NotificationsFragment : Fragment() {

    private lateinit var notificationsAdapter:NotificationsAdapter
    private var notificationsList:MutableList<Notifications>? = null
    private var recyclerView: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)

        recyclerView = view.findViewById(R.id.recyclerviewNotifications) // initialized after view is being inflated
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView?.layoutManager = linearLayoutManager

        notificationsList = ArrayList()
        notificationsAdapter = context?.let{ NotificationsAdapter(it,notificationsList as ArrayList<Notifications>) } !!
        recyclerView?.adapter = notificationsAdapter

        readNotifications()

        return view
    }

    private fun readNotifications() {
        FirebaseDatabase.getInstance().reference.child("Notifications")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    (notificationsList as ArrayList<Notifications>).clear()
                    if(snapshot.exists()){
                        for(snap in snapshot.children){
                            val notification = snap.getValue(Notifications::class.java)
                            if (notification != null) {
                                (notificationsList as ArrayList<Notifications>).add(notification)
                            }
                        }
                        (notificationsList as ArrayList<Notifications>).reverse()
                        notificationsAdapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

}