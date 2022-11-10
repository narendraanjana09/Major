package com.nsa.major.home.frags

import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nsa.major.R
import com.nsa.major.databinding.FragmentStudentClassBinding
import com.nsa.major.home.adapters.MessagesListAdapter
import com.nsa.major.home.bottom_sheets.NormalTextBottomSheet
import com.nsa.major.home.models.AttendanceModel
import com.nsa.major.home.models.ClassModel
import com.nsa.major.home.models.MessageModel
import com.nsa.major.home.viewmodels.ClassViewModel
import com.nsa.major.home.viewmodels.StudentHomeViewModel
import com.nsa.major.util.Constants
import com.nsa.major.util.CustomProgressDialog
import com.nsa.major.util.LocationDialog
import com.nsa.major.util.Util
import kotlin.math.roundToInt


class StudentClassFragment : Fragment(), PopupMenu.OnMenuItemClickListener {

    private lateinit var binding: FragmentStudentClassBinding
    val args: StudentClassFragmentArgs by navArgs()
    val db = Firebase.firestore

    private lateinit var user: FirebaseUser
    private val progressDialog by lazy { CustomProgressDialog(requireContext()) }
    private lateinit var classModel: ClassModel
    val homeViewModel: StudentHomeViewModel by viewModels()
    val classViewModel: ClassViewModel by viewModels()
    val dbRef = Firebase.database.reference
    private val messagesList= arrayListOf<MessageModel>()
    private lateinit var listAdapter: MessagesListAdapter
    private lateinit var listAdapterObserver: RecyclerView.AdapterDataObserver




    var lastDate=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
//            findNavController().popBackStack()
//        }

        classModel=args.classModel


        val messageListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val messageModel=snapshot.getValue<MessageModel>()
                if(lastDate.isEmpty()){
                    lastDate=Util.getDate(messageModel!!.time)
                    Log.e("TAG", "getItemViewType: is Date ${lastDate} ", )
                    messagesList.add(0,MessageModel("",Constants.IS_DATE,lastDate,"",88))
                }else{
                    val currDate=Util.getDate(messageModel!!.time)
                    if(currDate != lastDate){
                        lastDate=currDate
                        Log.e("TAG", "getItemViewType: is Date ${lastDate} ", )
                        messagesList.add(0,MessageModel("",Constants.IS_DATE,lastDate,"",88))
                    }
                }

                messagesList.add(0,messageModel!!)
                listAdapter.let {
                    it.notifyItemInserted(0)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        }

        dbRef
            .child("messages")
            .child("classes")
            .child(classModel.classId!!)
            .addChildEventListener(messageListener)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_student_class, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        user= Firebase.auth.currentUser!!

        getClassData()
        homeViewModel.getProgress().observe(viewLifecycleOwner){
            if(it && !progressShowing){
                progressShowing=true
                showProgress()
            }else{
                hideProgress()
            }
        }
        homeViewModel.getMessage().observe(viewLifecycleOwner){
            showToast(it)
        }
        binding.menuBtn.setOnClickListener {
            PopupMenu(requireContext(), it).apply {
                // MainActivity implements OnMenuItemClickListener
                setOnMenuItemClickListener(this@StudentClassFragment)
                inflate(R.menu.student_class_menu)
                show()
            }
        }


        binding.attendanceBtn.setOnClickListener {
            val locationDialog=LocationDialog(object :LocationDialog.LocationListener{
                override fun gotLocation(location: Location) {
                    showProgress()
                    val distance:Double=calculateDistanceBetween(
                        lat1 = location.latitude,
                        lon1 = location.longitude,
                        lat2 = classModel.locationLat!!,
                        lon2 =classModel.locationLong!!
                    )

                    if(distance<=classModel.minDistance!!){

                        db.collection("classes")
                            .document(classModel.classId!!)
                            .collection("attendances")
                            .document("${classModel.lastAttendance}")
                            .collection("students")
                            .document(user.uid)
                            .set(
                                AttendanceModel(
                                    studentName = user.displayName,
                                    studentId = user.uid,
                                    distance = distance,
                                    time = System.currentTimeMillis()
                                )
                            ).addOnSuccessListener {
                                hideProgress()
                                binding.attendanceBtn.text="Attendance Marked."
                                binding.attendanceBtn.isClickable=false

                                val normalTextBottomSheet=NormalTextBottomSheet(
                                    "Attendance"
                                ,"Your Attendance is marked successfully. You are at a distance of $distance meter from your class."
                                ,object :NormalTextBottomSheet.ClickCallBack{
                                        override fun onClick() {

                                        }
                                    }
                                )
                                normalTextBottomSheet.show(childFragmentManager,"cdscbh")


                            }.addOnFailureListener {
                                Log.e("TAG", ",mark attendance: $it", )
                                hideProgress()
                            }
                    }else{
                        hideProgress()
                        val normalTextBottomSheet=NormalTextBottomSheet(
                            "Attendance"
                            ,"Sorry, we couldn't mark your attendance because you are $distance meter away from your class.\nMin Distance is ${classModel.minDistance} meter."
                            ,object :NormalTextBottomSheet.ClickCallBack{
                                override fun onClick() {

                                }
                            }
                        )
                        normalTextBottomSheet.show(childFragmentManager,"cdcbh")

                    }
                }

                override fun message(message: String) {
                   showToast(message)
                }
            })
            locationDialog.show(childFragmentManager,"location")
        }
        getGroupChats()
    }

    private fun getGroupChats() {

        listAdapterObserver = (object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.chatsLayout.messagesRecyclerView.smoothScrollToPosition(0)
            }
        })
        listAdapter =
            MessagesListAdapter(classModel.teacherId,messagesList,user.uid)
        listAdapter.registerAdapterDataObserver(listAdapterObserver)
        binding.chatsLayout.messagesRecyclerView.adapter = listAdapter
        binding.chatsLayout.messageEd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                binding.chatsLayout.sendBtn.isEnabled = !p0.isNullOrEmpty()
            }
        })

        binding.chatsLayout.sendBtn.setOnClickListener {
            classViewModel.sendMessage(binding.chatsLayout.messageEd.text.toString().trim()
                ,classModel.classId
                ,user.uid
                ,user.displayName
            )
            binding.chatsLayout.messageEd.setText("")
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.leave_class -> {
                 leaveClass()
                true
            }
            R.id.view_students -> {
                findNavController().navigate(
                    StudentClassFragmentDirections.actionStudentClassFragmentToStudentsListFragment(
                        args.classModel.classId.toString(),
                        Constants.IS_NOT_ATTENDANCE
                    )
                )
                true
            }
            else -> false
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        listAdapter.unregisterAdapterDataObserver(listAdapterObserver)
    }

    private fun leaveClass() {
      homeViewModel.leaveClass(classModel)
        homeViewModel.classLeave.observe(viewLifecycleOwner){
            if(it){
                findNavController().previousBackStackEntry?.savedStateHandle?.set(Constants.CLASS_LEAVE,true)
                findNavController().popBackStack()
            }
        }

    }

    private fun calculateDistanceBetween(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {

        var lat1 = lat1
        var lat2 = lat2
        var lon1 = lon1
        var lon2 = lon2
        lon1 = Math.toRadians(lon1)
        lon2 = Math.toRadians(lon2)
        lat1 = Math.toRadians(lat1)
        lat2 = Math.toRadians(lat2)


        val dlon = lon2 - lon1
        val dlat = lat2 - lat1
        val a = (Math.pow(Math.sin(dlat / 2), 2.0)
                + (Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2), 2.0)))
        val c = 2 * Math.asin(Math.sqrt(a))


        val r = 6371.0

        val dis = c * r * 1000//for meteres
        return (dis * 100.0).roundToInt() / 100.0
    }

    private fun getClassData() {


        Log.e("TAG", "getClassData: $classModel")
        classViewModel.getJoinedStudents(classModel.classId!!)
        classViewModel.joined_students.observe(viewLifecycleOwner){
            binding.classDesc.text="${binding.classDesc.text}\nTotal Students :- ${it.size}"
        }

        homeViewModel.getClassData(classModel.classId!!)
        homeViewModel.lastAttendanceMarked.observe(viewLifecycleOwner){
            if(it){
                binding.attendanceBtn.text="Attendance Marked."
                binding.attendanceBtn.isClickable=false
            }
        }


        homeViewModel.classModel.observe(viewLifecycleOwner){
            classModel=it
            homeViewModel.getLastAttendanceMarked(classModel.classId.toString(),classModel.lastAttendance.toString())


            if(classModel.takeAttendance!=null){
                if(classModel.takeAttendance==true){
                    binding.attendanceBtn.visibility=View.VISIBLE
                }else{
                    binding.attendanceBtn.visibility=View.GONE
                }
            }
        }

        binding.className.text=classModel.className
        binding.classDesc.text=classModel.classDesc+"\n"+binding.classDesc.text
        binding.classTeacher.text="Formed By:- ${classModel.teacherName}"
        binding.classCreatedDate.text=Util.getDate(classModel.createDate)

    }
    private fun showToast(message:String){
        Util.showToast(requireContext(),message)
    }


    private var progressShowing=false

    private fun showProgress(){
        progressDialog.start()
    }
    private fun hideProgress(){
        progressDialog.stop()
    }




}