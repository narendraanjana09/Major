package com.nsa.major.home.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.nsa.major.home.models.AttendanceModel
import com.nsa.major.home.models.ClassModel
import com.nsa.major.home.models.MessageModel
import com.nsa.major.home.models.StudentJoinModel
import com.nsa.major.util.addMessageItem
import com.nsa.major.util.addNewItem
import kotlinx.coroutines.launch
import kotlin.math.log

class ClassViewModel: ViewModel() {
    val db = Firebase.firestore
    val dbRef = Firebase.database.reference
    private var user: FirebaseUser = Firebase.auth.currentUser!!


    private val _joined_students: MutableLiveData<List<StudentJoinModel>> = MutableLiveData()
    val joined_students: LiveData<List<StudentJoinModel>>
        get() = _joined_students



    private val _class_model: MutableLiveData<ClassModel> = MutableLiveData()
    val class_model: LiveData<ClassModel>
        get() = _class_model

    private val _attendances_list: MutableLiveData<List<String>> = MutableLiveData()
    val attendances_list: LiveData<List<String>>
        get() = _attendances_list

    private val _attendy_students_list: MutableLiveData<List<AttendanceModel>> = MutableLiveData()
    val attendy_students_list: LiveData<List<AttendanceModel>>
        get() = _attendy_students_list




    private val progress=MutableLiveData<Boolean>()

    fun getProgress(): MutableLiveData<Boolean> {
        return progress
    }
    private val message=MutableLiveData<String>()

    fun getMessage(): MutableLiveData<String> {
        return message
    }

    fun getClassData(classId: String){
        progress.value=true
        Log.e("TAG", "getAttendancesList:${classId}NSA", )
        db.collection("classes")
            .document(classId)
            .get()
            .addOnSuccessListener { document ->
                progress.value=false
                if (document != null) {
                    _class_model.value=document.toObject(ClassModel::class.java)
                    Log.e("TAG", "DocumentSnapshot data: ${document.data}")
                } else {
                    message.value="class data acquire error"
                    Log.e("TAG", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                message.value="class data acquire error"
                progress.value=false
                Log.e("TAG", "get failed with ", exception)
            }
    }

    fun getAttendancesList(classId: String){
        progress.value=true
        Log.e("TAG", "getAttendancesList:${classId}NSA", )
        db.collection("classes")
            .document(classId)
            .collection("attendances")
            .get()
            .addOnSuccessListener {
                    progress.value=false

                    if(it.isEmpty){
                        message.value="No Attendance Found!"
                        Log.e("TAG", "getAttendancesList: No Attendance Found ", )
                    }else{

                        val attendanceDateList= arrayListOf<String>()
                        for (document in it) {
                            val date= document.id
                            attendanceDateList.add(date)
                        }
                        _attendances_list.value=attendanceDateList
                    }
                }
                .addOnFailureListener { exception ->
                    progress.value=false
                    message.value="error"
                    Log.e("TAG", "Error getting documents: ", exception)
                }
    }

    fun getJoinedStudents(classId:String){
        progress.value=true
        viewModelScope.launch {
            db.collection("classes")
                .document(classId)
                .collection("students")
                .get()
                .addOnSuccessListener { result ->
                    progress.value=false
                    if(result.isEmpty){
                        message.value="No Students Found!"
                    }else{
                        Log.e("TAG", "getJoinedStudents:${result.documents} ", )
                        val studentsList= arrayListOf<StudentJoinModel>()
                        for (document in result) {
                            val studentJoinModel= document.toObject(StudentJoinModel::class.java)
                            studentsList.add(studentJoinModel)
                        }
                        _joined_students.value=studentsList
                    }
                }
                .addOnFailureListener { exception ->
                    progress.value=false
                    message.value="error"
                    Log.e("TAG", "Error getting documents: ", exception)
                }
        }
    }

    fun getStudentsList(classID: String, attendancesID: String) {
        progress.value=true
        Log.e("TAG", "getStudentsList:${classID}NSA", )
        db.collection("classes")
            .document(classID)
            .collection("attendances")
            .document(attendancesID)
            .collection("students")
            .get()
            .addOnSuccessListener {
                progress.value=false

                if(it.isEmpty){
                    message.value="No Students Found!"
                    Log.e("TAG", "getStudentsList: No Students Found ", )
                }else{

                    val studentsAttenList= arrayListOf<AttendanceModel>()
                    for (document in it) {
                        val date= document.toObject(AttendanceModel::class.java)
                        studentsAttenList.add(date)
                    }
                    _attendy_students_list.value=studentsAttenList
                }
            }
            .addOnFailureListener { exception ->
                progress.value=false
                message.value="error"
                Log.e("TAG", "Error getting documents: ", exception)
            }
    }

    fun sendMessage(message: String, classId: String?, userId: String, name: String?) {
        val messageModel=MessageModel(
            name = name,
            senderId = userId,
            message = message,
            time = System.currentTimeMillis()
        )
        dbRef
            .child("messages")
            .child("classes")
            .child(classId.toString())
            .push()
            .setValue(messageModel)
    }
}