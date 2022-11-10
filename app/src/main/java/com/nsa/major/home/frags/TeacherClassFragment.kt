package com.nsa.major.home.frags

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
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
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nsa.major.R
import com.nsa.major.databinding.FragmentTeacherClassBinding
import com.nsa.major.home.adapters.MessagesListAdapter
import com.nsa.major.home.bottom_sheets.TakeAttendanceBottomSheet
import com.nsa.major.home.models.ClassModel
import com.nsa.major.home.models.MessageModel
import com.nsa.major.home.viewmodels.ClassViewModel
import com.nsa.major.util.Constants
import com.nsa.major.util.CustomProgressDialog
import com.nsa.major.util.LocationDialog
import com.nsa.major.util.Util


class TeacherClassFragment : Fragment(), PopupMenu.OnMenuItemClickListener {

    private lateinit var binding: FragmentTeacherClassBinding
    val args: StudentClassFragmentArgs by navArgs()
    val db = Firebase.firestore
    val dbRef = Firebase.database.reference
    private lateinit var user: FirebaseUser
    private val progressDialog by lazy { CustomProgressDialog(requireContext()) }

    private val messagesList= arrayListOf<MessageModel>()
    private lateinit var listAdapter: MessagesListAdapter
    private lateinit var listAdapterObserver: RecyclerView.AdapterDataObserver


    private lateinit var classModel: ClassModel
    val classViewModel: ClassViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_teacher_class, container, false)
        return binding.root
    }

    var lastDate=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        classModel=args.classModel

        val messageListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val messageModel=snapshot.getValue<MessageModel>()

                if(lastDate.isEmpty()){
                    lastDate=Util.getDate(messageModel!!.time)
                    Log.e("TAG", "getItemViewType: is Date ${lastDate} ")
                    messagesList.add(0,MessageModel("",Constants.IS_DATE,lastDate,"",88))
                }else{
                    val currDate=Util.getDate(messageModel!!.time)
                    if(currDate != lastDate){
                        lastDate=currDate
                        Log.e("TAG", "getItemViewType: is Date ${lastDate} ")
                        messagesList.add(0,MessageModel("",Constants.IS_DATE,lastDate,"",88))
                    }
                }


                messagesList.add(0, messageModel)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        user= Firebase.auth.currentUser!!
        getClassData()
        getGroupChats()



        binding.menuBtn.setOnClickListener {

            val popup = PopupMenu(requireContext(),it)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.teacher_class_menu, popup.menu)
            popup.show()
            popup.setOnMenuItemClickListener(this@TeacherClassFragment)
            if(classModel.takeAttendance==true){
                popup.menu.findItem(R.id.take_attendance).title="Stop Attendance"
            }
        }


    }


    private fun getGroupChats() {

        listAdapterObserver = (object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.chatsLayout.messagesRecyclerView.smoothScrollToPosition(0)
            }
        })
        listAdapter =
            MessagesListAdapter(classModel.teacherId, messagesList, user.uid)
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
    override fun onDestroyView() {
        super.onDestroyView()
        listAdapter.unregisterAdapterDataObserver(listAdapterObserver)
    }
    private fun stopAttendance() {
        showProgress()
        classModel.takeAttendance=false
        db.collection("classes")
            .document(classModel.classId.toString())
            .set(classModel)
            .addOnSuccessListener {
                hideProgress()
                showToast("Attendance Stopped!")
            }.addOnFailureListener {
                hideProgress()
                showToast("Stop Attendance Error")
            }
    }

    private fun takeAttendance() {
        val locationDialog=LocationDialog(object :LocationDialog.LocationListener{
            override fun gotLocation(currentLocation: Location) {
                val takeAttendanceBottomSheet= TakeAttendanceBottomSheet(currentLocation,object :TakeAttendanceBottomSheet.ClickCallBack{
                    override fun onClick(distance: Double, timer: Int) {
                        showProgress()
                        classModel.locationLat=currentLocation.latitude
                        classModel.locationLong=currentLocation.longitude
                        classModel.minDistance=distance
                        classModel.takeAttendance=true
                        classModel.lastAttendance=System.currentTimeMillis()
                        classModel.attendanceTimer=timer

                        db.collection("classes")
                            .document(classModel.classId.toString())
                            .set(classModel)
                            .addOnSuccessListener {
                                db.collection("classes")
                                    .document(classModel.classId!!)
                                    .collection("attendances")
                                    .document("${classModel.lastAttendance}")
                                    .set(
                                        hashMapOf(
                                            "date" to System.currentTimeMillis()
                                        )
                                    )

                                showToast("Attendance Started")
                                hideProgress()
                            }.addOnFailureListener {
                                Log.e("TAG", "class ex: $it ")
                            }




                    }
                })
                takeAttendanceBottomSheet.show(childFragmentManager,"attendance")
            }

            override fun message(message: String) {
                showToast(message)
            }
        })
        locationDialog.show(childFragmentManager,"location")
    }

    private fun shareClass() {
        showProgress()
        val link="https://major.page.link/?link=" +
                "https://www.major.com/${Constants.class_id}/${args.classModel.classId}"//(/&apn=com.nsa.major&ibn=com.major.ios")
        val shortLinkTask = Firebase.dynamicLinks.shortLinkAsync {
            longLink = Uri.parse(link)
        }.addOnSuccessListener {

            hideProgress()


            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Join your ${args.classModel.className} class with this link\n\n${it.shortLink}")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share Class")
            startActivity(shareIntent)

        }.addOnFailureListener {
            Log.e("TAG", "create link error $it: ")
            showToast("Create link error")
            hideProgress()
        }
    }

    val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
               getCurrentLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                showToast("coarse location_grned")
            } else -> {
                hideProgress()
            // No location access granted.
        }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
           showToast("location permission is not granted")
            hideProgress()
            return
        }
        Log.e("TAG", "getCurrentLocation: ")
    }

    private fun getClassData() {


        classViewModel.getMessage().observe(viewLifecycleOwner){
            showToast(it)
        }
        classViewModel.getJoinedStudents(classModel.classId!!)

        classViewModel.joined_students.observe(viewLifecycleOwner){
            binding.classDesc.text="${classModel.classDesc}\nTotal Students :- ${it.size}"
        }
        classViewModel.getClassData(classModel.classId.toString())
        classViewModel.class_model.observe(viewLifecycleOwner){
            classModel=it
        }

        binding.className.text=classModel.className
        binding.classDesc.text=classModel.classDesc
        binding.classCreatedDate.text=Util.getDate(classModel.createDate)

    }
    private fun showToast(message:String){
        Util.showToast(requireContext(),message)
    }
    private fun showProgress(){
        progressDialog.start()
    }
    private fun hideProgress(){
        progressDialog.stop()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.share_class -> {
                shareClass()
                true
            }
            R.id.take_attendance -> {
                if(classModel.takeAttendance==true){
                    stopAttendance()
                }else{
                    takeAttendance()
                }
                true
            }
            R.id.view_attendance -> {
                findNavController().navigate(
                    TeacherClassFragmentDirections.actionTeacherClassFragmentToViewAttendanceFragment(
                        classModel.classId.toString()
                    )
                )
                true
            }
            R.id.view_students -> {
                findNavController().navigate(
                    TeacherClassFragmentDirections.actionTeacherClassFragmentToStudentsListFragment(
                        args.classModel.classId.toString(),
                        Constants.IS_NOT_ATTENDANCE
                    )
                )
                true
            }
            R.id.open_meet -> {
                val urlIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://meet.google.com/")
                )
                startActivity(urlIntent)
                true
            }
            else -> false
        }

    }



}