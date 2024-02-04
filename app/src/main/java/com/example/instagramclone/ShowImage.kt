package com.example.instagramclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.example.instagramclone.Fragments.HomeFragment
import com.squareup.picasso.Picasso

class ShowImage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_image)

        val imageData = intent.getStringExtra("postImage")
        val imageView = findViewById<ImageView>(R.id.putImageHere)

        Picasso.get().load(imageData).into(imageView)

    }
}