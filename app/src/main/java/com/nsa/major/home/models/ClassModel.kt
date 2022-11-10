package com.nsa.major.home.models

import android.os.Parcel
import android.os.Parcelable

data class ClassModel(val className:String?=null,
                      var teacherName:String?=null,
                      val classDesc:String?=null,
                      var teacherId:String?=null,
                      var classId:String?=null,
                      val createDate:Long?=null,
                      var takeAttendance:Boolean?=null,
                      var locationLat:Double?=null,
                      var locationLong:Double?=null,
                      var minDistance:Double?=null,
                      var lastAttendance:Long?=null,
                      var attendanceTimer:Int?=null
                      ):Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readValue(Int::class.java.classLoader) as? Int
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(className)
        parcel.writeString(teacherName)
        parcel.writeString(classDesc)
        parcel.writeString(teacherId)
        parcel.writeString(classId)
        parcel.writeValue(createDate)
        parcel.writeValue(takeAttendance)
        parcel.writeValue(locationLat)
        parcel.writeValue(locationLong)
        parcel.writeValue(minDistance)
        parcel.writeValue(lastAttendance)
        parcel.writeValue(attendanceTimer)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ClassModel> {
        override fun createFromParcel(parcel: Parcel): ClassModel {
            return ClassModel(parcel)
        }

        override fun newArray(size: Int): Array<ClassModel?> {
            return arrayOfNulls(size)
        }
    }
}