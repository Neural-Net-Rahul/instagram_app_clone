package com.example.instagramclone.Model

class Notifications {
    private var userId:String = ""
    private var text:String = ""
    private var postId:String = ""
    private var isPost = false

    constructor()

    constructor(userId: String , text: String , postId: String , isPost: Boolean) {
        this.userId = userId
        this.text = text
        this.postId = postId
        this.isPost = isPost
    }

    fun getUserId():String{
        return userId
    }

    fun getText():String{
        return text
    }

    fun getPostId():String{
        return postId
    }

    fun getIsPost():Boolean{
        return isPost
    }

    fun setUserId(userId:String){
        this.userId = userId
    }

    fun setText(text:String){
        this.text = text
    }

    fun setPostId(postId:String){
        this.postId = postId
    }

    fun setIsPost(isPost: Boolean){
        this.isPost = isPost
    }

}