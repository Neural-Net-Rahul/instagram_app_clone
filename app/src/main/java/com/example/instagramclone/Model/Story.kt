package com.example.instagramclone.Model

class Story {
    private var imageUrl:String = ""
    private var timeStart:Long = 0
    private var timeEnd:Long = 0
    private var storyid:String = ""
    private var userid:String = ""

    constructor()
    constructor(
        imageUrl: String ,
        timeStart: Long ,
        timeEnd: Long ,
        storyid: String ,
        userid: String
    ) {
        this.imageUrl = imageUrl
        this.timeStart = timeStart
        this.timeEnd = timeEnd
        this.storyid = storyid
        this.userid = userid
    }

    fun getImageUrl():String{
        return imageUrl
    }
    fun getTimeStart():Long{
        return timeStart
    }
    fun getTimeEnd():Long{
        return timeEnd
    }
    fun getStoryId():String{
        return storyid
    }
    fun getUserId():String{
        return userid
    }
    fun setImageUrl(imageUrl: String){
        this.imageUrl = imageUrl
    }
    fun setTimeStart(timeStart: Long){
        this.timeStart = timeStart
    }
    fun setTimeEnd(timeEnd:Long){
        this.timeEnd = timeEnd
    }
    fun setStoryId(storyid: String){
        this.storyid = storyid
    }
    fun setUserId(userid: String){
        this.userid = userid
    }

}