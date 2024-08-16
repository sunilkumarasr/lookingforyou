package com.stranger_sparks.view.activities.ui.activities.account.profile

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.stranger_sparks.R
import com.stranger_sparks.StrangerSparksApplication
import com.stranger_sparks.databinding.ActivityProfileBinding
import com.stranger_sparks.utils.ConstantUtils
import com.stranger_sparks.utils.Constants
import com.stranger_sparks.utils.FileUtil
import com.stranger_sparks.utils.SharedPreferenceManager
import com.theartofdev.edmodo.cropper.CropImage
import id.zelory.compressor.Compressor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.Calendar
import javax.inject.Inject


class ProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityProfileBinding
    @Inject
    lateinit var viewModel: ProfileViewModel
    lateinit var phnunber: String
    lateinit var userID: String
    lateinit var fullName: String
    lateinit var gender: String
    lateinit var hobbies: String
    lateinit var height: String
    lateinit var location: String
    lateinit var email: String
    lateinit var imageurl: String
    var emailId: String = ""
    var locationAddress: String = ""
    var imgPos: Int = 0
    val txtList: MutableList<TextView> = mutableListOf()
    val imgUriList: Array<Uri?> = arrayOfNulls(1)
    var isInLetImageSelected: Boolean = false

    private var selectedImagesUri = mutableListOf<Uri>()
    private var selectedImagesFile = mutableListOf<File>()
    var mYear: Int ?=null
    var mMonth: Int ?=null
    var mDay: Int?=null
    var dob: String = ""
    var bio: String = ""
    var alterNativeNumber: String = ""
    var maritalSelection: String = ""
    var languageSelection: String = ""

    val context: Context = this

    val mobileNumberPattern = "^[6-9]\\d{9}$"
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (this.application as StrangerSparksApplication).applicationComponent.inject(this)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        ConstantUtils.changeNotificationBarColor(this, ContextCompat.getColor(this, R.color.black), false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.inputSignal.observe(this) {

            if (it != null) {
                validateViewModelEvents(it)

            }
        }
        selectedImagesFile = arrayListOf<File>()
        selectedImagesUri = arrayListOf<Uri>()
        txtList += listOf(
            binding.tvFileName
            // Add more Uri objects as needed
        )
        binding.ivClose.setOnClickListener {
            finish()
        }

        //disable
        binding.rbMale.isEnabled = false
        binding.rbFemale.isEnabled = false
        binding.radioButtonSingle.isEnabled = false
        binding.radioButtonMarriage.isEnabled = false
        binding.tvDOB.isEnabled = false



        val sharedPreferenceManager = SharedPreferenceManager(this)
        phnunber = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.phone.toString()
        userID = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.id.toString()
        fullName = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.name.toString()
        gender = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.gender.toString()
        hobbies = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.hobbies.toString()
        email = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.email.toString()
        height = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.height.toString()
        location = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.location.toString()
        imageurl = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.image.toString()
        bio = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.description.toString()
        dob = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.age.toString()
        alterNativeNumber = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.alternative_phone.toString()
        maritalSelection = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.marital.toString()
        languageSelection = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.languages.toString()

//        Log.e("image_",imageurl)
//        val uris = Uri.parse(imageurl)
//        if (uris!=null){
//
//            selectedImagesUri.add(uris)
//            isInLetImageSelected = true
//
//        }

        binding.etFullName.setText(fullName)
        binding.etEmailId.setText(email)
        binding.llAutoCompleteTextView.setText(location)
        binding.etStudy.setText(hobbies)
        binding.etBio.setText(bio)
        binding.tvDOB.setText(dob)
        binding.etAlterNativeMobileNumber.setText(alterNativeNumber)
        if(gender == "Male"){
            binding.rbMale.isChecked = true
        }else{
            binding.rbFemale.isChecked = true
        }
        if (maritalSelection.equals("Single")){
            binding.radioButtonSingle.isChecked = true
        }else{
            binding.radioButtonMarriage.isChecked = true
        }
        val languagesList = languageSelection.split(",")
        languagesList.forEach { language ->
            println(language.trim())
            if(language.trim() == "Telugu"){
                binding.checkBoxTelugu.isChecked = true
            }else if(language.trim() == "English") {
                binding.checkBoxEnglish.isChecked = true
            }else if(language.trim() == "Hindi") {
                binding.checkBoxHindi.isChecked = true
            }
        }
        Glide.with(this).load(imageurl)
            .error(R.drawable.ic_image_place_holder)
            .transform(CenterCrop(), RoundedCorners(10))
            .into(binding.ivProfilePicture)

        //search city's list
        viewModel.getCitysList()
        viewModel.cityListLiveData.observe(this) { response ->
            response?.data?.let { cities ->
                val cityNames = cities.map { it.name }
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cityNames)
                binding.llAutoCompleteTextView.setAdapter(adapter)
                binding.llAutoCompleteTextView.threshold = 1
            }
        }


        val heightslist = mutableListOf<String>()

        for (feet in 4..7) {
            for (inch in 0..12) {
                val height = "$feet.$inch"
                if (height.toFloat() >= 4.0 && height.toFloat() <= 7.5) {
                    heightslist.add(height+" ft")
                }
            }
        }
        //default value
        heightslist.add(0, height)
        val adapter = ArrayAdapter(this, R.layout.custom_spinner_dropdown_item, heightslist)
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        binding.spHeight.adapter = adapter


        binding.materialRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedmaterial = findViewById<RadioButton>(checkedId)
            maritalSelection = selectedmaterial.text.toString()
        }


        /*val pickMultipleMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                // Callback is invoked after the user selects media items or closes the
                // photo picker.
                if (uris.isNotEmpty()) {

                    selectedImagesUri.clear()
                    selectedImagesFile.clear()

                        selectedImagesUri.add(it)
                        selectedImagesFile.add(FileUtil.from(applicationContext, it))
                        *//*runOnUiThread(
                            object : Runnable {
                                override fun run() {
                                    selectedImagesUri.add(it)
                                    selectedImagesFile.add(FileUtil.from(ctx, it))
                                }
                            }
                        )*//*
                        isInLetImageSelected = true

                        binding.ivProfilePicture.setImageURI(selectedImagesUri[0])
                        binding.tvFileName.text = selectedImagesFile[0].name



                    //imageAdapter = ImageAdapter(ctx as ImagesGrid, selectedImagesUri, selectedImagesFile)


                    Log.d("PhotoPicker", "Number of items selected: ${uris.size} -- ${selectedImagesFile.size}")
                    Log.d("PhotoPicker", "Number of items selected: ${selectedImagesFile.toString()}")
                } else {
                    Log.d("PhotoPicker", "No media selected")
                }
            }*/

        val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // 'ActivityResultCallback': Handle the returned Uri
            if(uri != null){
                selectedImagesUri.clear()
                selectedImagesFile.clear()

                selectedImagesUri.add(uri)
                selectedImagesFile.add(FileUtil.from(applicationContext, uri))
                isInLetImageSelected = true

                Glide.with(this).load(selectedImagesFile[0])
                    .error(R.drawable.ic_image_place_holder)
                    .transform(CenterCrop(), RoundedCorners(10))
                    .into(binding.ivProfilePicture)
                //binding.ivProfilePicture.setImageURI(selectedImagesUri[0])
                binding.tvFileName.text = selectedImagesFile[0].name
                Log.e("adsdas","das")
            }
        }


        binding.llUploadProfilePicture.setOnClickListener {
            /*cropResultLauncher.launch(
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                    .setMinCropWindowSize(1000, 1000)
                    .getIntent(this)


            )*/


            getContent.launch("image/*")
            //pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnUpdateProfile.setOnClickListener {

            languageS()


            if (!ConstantUtils.isNetworkConnected(applicationContext)) {
                ConstantUtils.alertDialog("Please Check Internet Connection", this)
            } else if (checkValidation()) {

                fullName = binding.etFullName.text.toString().trim()
                hobbies = binding.etStudy.text.toString().trim()
                emailId = binding.etEmailId.text.toString().trim()
                height = binding.spHeight.selectedItem.toString().trim()
                locationAddress = binding.llAutoCompleteTextView.text.toString().trim()
                bio = binding.etBio.text.toString().trim()
                locationAddress = binding.llAutoCompleteTextView.text.toString().trim()
                dob = binding.tvDOB.text.toString().trim()
                if(!binding.etEmailId.text.toString().isNullOrBlank()){
                    emailId = binding.etEmailId.text.toString().trim()
                }else{
                    emailId = ""
                }

                if(!binding.etEmailId.text.toString().isNullOrBlank()){
                    alterNativeNumber = binding.etAlterNativeMobileNumber.text.toString().trim()
                }else{
                    alterNativeNumber = ""
                }

                if (!binding.rbMale.isChecked && !binding.rbFemale.isChecked) {
                    ConstantUtils.showToast(this, "Please Select Gender")
                    return@setOnClickListener
                }
                else {
                    if (isInLetImageSelected) {
                        var file: File
                        var inletMultiPart: MultipartBody.Part?
                        if (selectedImagesFile[0]?.path.isNullOrBlank()) {
                            ConstantUtils.showToast(this, "Please Picture")
                            return@setOnClickListener
                        } else {
                            file = File(selectedImagesFile[0]!!.path.toString())

                            /*inletMultiPart =
                                file?.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                                    ?.let {
                                        MultipartBody.Part.createFormData(
                                            "image", file?.name,
                                            it
                                        )
                                    }*/

                            //if you need bitmap
                           // val bitmap = Compressor(this).compressToBitmap(selectedImagesFile[0])

//if you need file
                            val compressedImageFile = Compressor(this).compressToFile(selectedImagesFile[0])

                            inletMultiPart = compressedImageFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())?.let {
                                MultipartBody.Part.createFormData("image", compressedImageFile.name, it)
                            }

                        }
                        if (inletMultiPart != null) {
                            val sharedPreferenceManager = SharedPreferenceManager(this)
                            binding.progressLay.progressBar.visibility = View.VISIBLE
                            try {
                                var phnunber = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.phone
                                var userID = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.id

                                var selectedGender: String = ""
                                if (binding.rbMale.isChecked) {
                                    selectedGender = "Male"
                                }
                                else if (binding.rbFemale.isChecked) {
                                    selectedGender = "Female"
                                }

                                viewModel.updateProfile(
                                    fullName.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    dob.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    locationAddress.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    emailId.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    phnunber.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    height.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    bio.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    userID.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    selectedGender.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    hobbies.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    alterNativeNumber.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    maritalSelection.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    languageSelection.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    inletMultiPart
                                )


                            }catch (e: Exception){
                                Log.v("Purushotham", e.message.toString())
                            }
                        }
                    } else {
                        val sharedPreferenceManager = SharedPreferenceManager(this)
                        binding.progressLay.progressBar.visibility = View.VISIBLE
                        try {
                            var phnunber = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.phone
                            var userID = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.id

                            var selectedGender: String = ""
                            if (binding.rbMale.isChecked) {
                                selectedGender = "Male"
                            }
                            else if (binding.rbFemale.isChecked) {
                                selectedGender = "Female"
                            }

                            viewModel.updateProfilewithoutpic(
                                fullName.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                dob.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                locationAddress.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                emailId.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                phnunber.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                height.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                bio.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                userID.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                selectedGender.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                hobbies.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                alterNativeNumber.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                maritalSelection.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                languageSelection.toRequestBody("multipart/form-data".toMediaTypeOrNull())
                            )
                        }catch (e: Exception){
                            Log.v("Purushotham", e.message.toString())
                        }
                    }
                }

            }

        }


        viewModel.upDateProfileLiveData.observe(this) { resp ->
            resp?.message?.let { it1 -> ConstantUtils.showSuccessToast(this, it1)}
            binding.progressLay.progressBar.visibility = View.GONE
            if (resp?.status == true) {
                val sharedPreferenceManager =  SharedPreferenceManager(this)
               sharedPreferenceManager.clearAllData()
                if (resp != null) {
                    sharedPreferenceManager.saveLoginResponse(resp)
                }

                finish()
                } else {
                    Log.e("resp","resp")
                //res?.message?.let { ConstantUtils.showToast(this, it) }
            }
        }
        binding.tvDOB.setOnClickListener {
            val c: Calendar = Calendar.getInstance()
            val mYear = c.get(Calendar.YEAR)
            val mMonth = c.get(Calendar.MONTH)
            val mDay = c.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(
                this,
                R.style.SpinnerDatePickerDialogStyle,
                { _, year, monthOfYear, dayOfMonth ->
                    val selectedDate = "$dayOfMonth-${monthOfYear + 1}-$year"
                    binding.tvDOB.text = selectedDate
                },
                mYear,
                mMonth,
                mDay
            )
            // Calculate the date 18 years ago from today
            c.add(Calendar.YEAR, -18)
            datePickerDialog.datePicker.maxDate = c.timeInMillis

            datePickerDialog.show()
        }
    }

    fun languageS(){
        val selectedLanguages = mutableListOf<String>()
        if (binding.checkBoxTelugu.isChecked) selectedLanguages.add("Telugu")
        if (binding.checkBoxEnglish.isChecked) selectedLanguages.add("English")
        if (binding.checkBoxHindi.isChecked) selectedLanguages.add("Hindi")
        languageSelection = selectedLanguages.joinToString(", ")
    }

    private fun validateViewModelEvents(observerEvents: String) {
        if (observerEvents === Constants.ObserverEvents.CLOSE_NOTIFICATION_SCREEN.toString()) {
            finish()
        }
    }

    private val cropResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val cropResult = CropImage.getActivityResult(data)
            if (result.resultCode == Activity.RESULT_OK) {
                var resultUri = cropResult?.uri!!
                //loadProfile(resultUri)
                if (resultUri != null) {
                    /* val backgroundImage = BitmapFactory.decodeStream(
                         contentResolver.openInputStream(resultUri)
                     )*/
                    // binding.rllevelmeter.background = BitmapDrawable(resources, backgroundImage)
                    // binding.tvInlet.setText(getFileNameFromUri(resultUri))
                    txtList.get(imgPos).setText(getFileNameFromUri(resultUri))
                    binding.ivProfilePicture.setImageURI(resultUri)
                    imgUriList[imgPos] = resultUri
                    if (imgPos == 0) {
                        isInLetImageSelected = true
                    }
                    //imgUriList.add(imgPos,resultUri)
                }
                //Log.d("TAG", "onActivityResult: $resultUri")
            } else if (result.resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = cropResult!!.error
                error.printStackTrace()
            }
        }
    }

    fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        val scheme = uri.scheme

        if (ContentResolver.SCHEME_CONTENT == scheme) {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    fileName = cursor.getString(index)
                }
            }
        } else if (ContentResolver.SCHEME_FILE == scheme) {
            fileName = File(uri.path).name
        }

        return fileName
    }
    fun checkValidation(): Boolean {
        var ret = true
        if (!ConstantUtils.hasEditText(binding.etFullName, "Please Enter Full Name")) ret = false
        //if (!ConstantUtils.hasEditText(binding.etEmailId, "Please Email Id")) ret = false
        if (!ConstantUtils.hasEditText(binding.etStudy, "Please Enter Hobbies")) ret = false
        if (maritalSelection.equals("")) {
            ConstantUtils.showToast(applicationContext, "Please select Marital")
            ret = false
        }
        if(binding.tvDOB.text.toString().trim() == applicationContext.resources.getString(R.string.selecte_dob)){
            ConstantUtils.showToast(applicationContext, "Please select Date Birth")
            ret = false
        }
        if (languageSelection.equals("")) {
            ConstantUtils.showToast(applicationContext, "Please select at least one language")
            ret = false
        }
        if (binding.llAutoCompleteTextView.text.toString().equals("")){
            Toast.makeText(applicationContext,"please select location",Toast.LENGTH_SHORT).show()
            ret = false
        }

        if (!isValidMobileNumber(binding.etAlterNativeMobileNumber.text.toString())) {
            Toast.makeText(this, "Invalid  Mobile Number", Toast.LENGTH_SHORT).show()
            ret = false
        }

        if (!isValidEmail(binding.etEmailId.text.toString())) {
            Toast.makeText(this, "Invalid  Email", Toast.LENGTH_SHORT).show()
            ret = false
        }

        if (!ConstantUtils.hasEditText(binding.etBio, "Please Enter Bio")) ret = false
        return ret
    }

    fun isValidMobileNumber(mobileNumber: String): Boolean {
        val regex = Regex(mobileNumberPattern)
        return mobileNumber.matches(regex)
    }

    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && email.matches(emailPattern.toRegex())
    }

}