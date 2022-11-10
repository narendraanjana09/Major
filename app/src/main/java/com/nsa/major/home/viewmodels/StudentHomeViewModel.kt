package com.nsa.major.home.viewmodels

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nsa.major.home.HomeActivity
import com.nsa.major.home.models.ClassModel
import com.nsa.major.home.models.StudentJoinModel
import com.nsa.major.util.Constants
import com.nsa.major.util.SharedPref
import com.nsa.major.util.Util.showToast
import kotlinx.coroutines.launch

class StudentHomeViewModel: ViewModel() {

    val db = Firebase.firestore
    private var user: FirebaseUser = Firebase.auth.currentUser!!

    private val classes: MutableLiveData<List<ClassModel>> by lazy {
        MutableLiveData<List<ClassModel>>().also {
            loadClasses()
        }
    }

    private val _classModel: MutableLiveData<ClassModel> = MutableLiveData()
    val classModel: LiveData<ClassModel>
        get() = _classModel

    private val _lastAttendanceMarked: MutableLiveData<Boolean> = MutableLiveData()
    val lastAttendanceMarked: LiveData<Boolean>
        get() = _lastAttendanceMarked

    private val _classExist:MutableLiveData<Boolean> = MutableLiveData()
    val classExist:LiveData<Boolean>
         get() = _classExist

    private val _classLeaved:MutableLiveData<Boolean> = MutableLiveData()
    val classLeave:LiveData<Boolean>
        get() = _classLeaved


    private val progress=MutableLiveData<Boolean>()

    fun getProgress(): MutableLiveData<Boolean> {
        return progress
    }
    private val message=MutableLiveData<String>()

    fun getMessage(): MutableLiveData<String> {
        return message
    }

    fun getClasses(): LiveData<List<ClassModel>> {
        return classes
    }

    fun loadClasses() {
        progress.value=true
       viewModelScope.launch {
           db.collection("students").document(user.uid)
               .collection("classes")
               .get()
               .addOnSuccessListener { result ->
                   progress.value=false
                   if(result.isEmpty){
                       message.value="No Classes Found!"
                   }else{
                       val classesList= arrayListOf<ClassModel>()
                       for (document in result) {
                           val classModel= document.toObject(ClassModel::class.java)
                           classesList.add(classModel)
                       }
                       classes.value=classesList
                   }
               }
               .addOnFailureListener { exception ->
                   progress.value=false
                   message.value="error"
                   Log.e("TAG", "Error getting documents: ", exception)
               }
       }
    }

    fun checkClass(classID: String) {
        progress.value=true
        viewModelScope.launch {
            db.collection("students").document(user.uid)
                .collection("classes")
                .document(classID)
                .get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result

                        if(document != null) {
                            if(document.exists()){
                                progress.value=false
                                _classExist.value=true
                                _classModel.value= document.toObject(ClassModel::class.java)
                            }else{
                                _classExist.value=false
                                progress.value=true
                                db.collection("classes").document(classID)
                                    .get().addOnCompleteListener { task ->
                                        progress.value=false
                                        if(task.isSuccessful){
                                            val classDocument = task.result
                                            if(classDocument.exists()){
                                                val classModel= classDocument.toObject(ClassModel::class.java)
                                                classModel!!.classId=classDocument.id
                                                _classModel.value=classModel!!
                                            }
                                        }else{
                                            message.value="error3"
                                            Log.d("TAG", "Error: ", task.exception)
                                        }
                                    }



                            }
                        }
                    } else {
                        progress.value=false
                        message.value="error2"
                        Log.d("TAG", "Error: ", task.exception)
                    }
                }
        }
    }
    fun leaveClass(classModel: ClassModel){
        progress.value=true
        viewModelScope.launch {

            db.collection("students").document(user.uid)
                .collection("classes")
                .document(classModel!!.classId!!)
                .delete()
                .addOnSuccessListener {
                    db.collection("classes")
                        .document(classModel!!.classId!!)
                        .collection("students")
                        .document(user.uid)
                        .delete()
                        .addOnSuccessListener {
                            progress.value=false
                            message.value="You have leaved ${classModel.className} class SuccessFully!"
                            _classLeaved.value=true
                            loadClasses()
                        }.addOnFailureListener {
                            message.value="error leave class2"
                            progress.value=false
                        }
                }.addOnFailureListener {
                    message.value="error leave class"
                    progress.value=false
                }
        }
    }

    fun joinClass(classModel: ClassModel?) {
       progress.value=true
        viewModelScope.launch {
            db.collection("students").document(user.uid)
                .collection("classes")
                .document(classModel!!.classId!!)
                .set(classModel)
                .addOnSuccessListener {
                    db.collection("classes")
                        .document(classModel!!.classId!!)
                        .collection("students")
                        .document(user.uid)
                        .set(StudentJoinModel(
                            studentName = user.displayName,
                            studentId = user.uid,
                            joinDate = System.currentTimeMillis()
                        ))
                        .addOnSuccessListener {
                            progress.value=false
                            message.value="Class Joined SuccessFully!"
                            loadClasses()
                        }.addOnFailureListener {
                            message.value="error join class2"
                            progress.value=false
                        }
                }.addOnFailureListener {
                    message.value="error join class"
                    progress.value=false
                }
        }

    }

    fun getLastAttendanceMarked(classID: String,lastAttendanceID:String){
        progress.value=true
        db.collection("classes")
            .document(classID)
            .collection("attendances")
            .document(lastAttendanceID)
            .collection("students")
            .document(user.uid).get()
            .addOnCompleteListener { task ->
                progress.value=false
            if (task.isSuccessful) {
                val document = task.result

                if(document != null) {
                    _lastAttendanceMarked.value = document.exists()
                }else{
                    _lastAttendanceMarked.value=false
                }
            }
            }.addOnFailureListener {
                message.value="last attendance error"
                progress.value=false
            }
    }

    fun getClassData(classId: String) {
        progress.value=true
        db.collection("classes")
            .document(classId)
            .get()
            .addOnSuccessListener {
                progress.value=false
                _classModel.value=it.toObject(ClassModel::class.java)
            }
            .addOnFailureListener {
                progress.value=false
                Log.e("TAG", "getClassData: $it ", )
            }
    }

}