package com.example.instagramclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.instagramclone.Fragments.HomeFragment
import com.example.instagramclone.Fragments.NotificationsFragment
import com.example.instagramclone.Fragments.ProfileFragment
import com.example.instagramclone.Fragments.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {

    private val onItemSelectedListener = NavigationBarView.OnItemSelectedListener{ item ->
        when(item.itemId){
            R.id.nav_homecoming -> {
                moveToFragment(HomeFragment())
                return@OnItemSelectedListener true
            }
            R.id.nav_search -> {
                moveToFragment(SearchFragment())
                return@OnItemSelectedListener true
            }
            R.id.nav_add_post -> {
                item.isChecked = false
                startActivity(Intent(this,AddPostActivity::class.java))
                return@OnItemSelectedListener true
            }
            R.id.nav_notifications -> {
                moveToFragment(NotificationsFragment())
                return@OnItemSelectedListener true
            }
            R.id.nav_profile -> {
                moveToFragment(ProfileFragment())
                return@OnItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView:BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnItemSelectedListener(onItemSelectedListener)

        // default fragment is Home Fragment
        moveToFragment(HomeFragment())
    }
    private fun moveToFragment(fragment:Fragment){
        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.fragment_container,fragment)
        fragmentTrans.commit()
    }
}