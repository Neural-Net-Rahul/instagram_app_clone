package com.example.instagramclone.Model

class Comment {
    private var comment:String = ""
    private var publisher:String = ""

    constructor()

    constructor(comment:String,publisher:String){
        this.comment = comment
        this.publisher = publisher
    }

    fun getComment():String{
        return this.comment
    }

    fun getPublisher():String{
        return this.publisher
    }

    fun setComment(comment:String){
        this.comment = comment
    }

    fun setPublisher(publisher: String){
        this.publisher = publisher
    }
}