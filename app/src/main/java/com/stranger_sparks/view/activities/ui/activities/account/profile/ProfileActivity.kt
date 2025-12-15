package com.stranger_sparks.view.activities.ui.activities.account.profile

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.stranger_sparks.R
import com.stranger_sparks.StrangerSparksApplication
import com.stranger_sparks.databinding.ActivityProfileBinding
import com.stranger_sparks.fcm.ZegoCallManager
import com.stranger_sparks.utils.ConstantUtils
import com.stranger_sparks.utils.Constants
import com.stranger_sparks.utils.FileUtil
import com.stranger_sparks.utils.SharedPreferenceManager
import com.yalantis.ucrop.UCrop
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
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
    private lateinit var galleryLauncher: ActivityResultLauncher<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        ConstantUtils.darkModeDisable()
        super.onCreate(savedInstanceState)
        (this.application as StrangerSparksApplication).applicationComponent.inject(this)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
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

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                openCropActivity(it)
            }
        }

        binding.llUploadProfilePicture.setOnClickListener {
            galleryLauncher.launch("image/*")
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
                        var inletMultiPart: MultipartBody.Part?
                        if (selectedImagesFile[0]?.path.isNullOrBlank()) {
                            ConstantUtils.showToast(this, "Please Picture")
                            return@setOnClickListener
                        } else {
                            inletMultiPart =
                                selectedImagesFile[0].asRequestBody("multipart/form-data".toMediaTypeOrNull())
                                    ?.let {
                                        MultipartBody.Part.createFormData(
                                            "image",
                                            selectedImagesFile[0].name,
                                            it
                                        )
                                    }
                        }
                        if (inletMultiPart != null) {
                            val sharedPreferenceManager = SharedPreferenceManager(this)
                            binding.progressBar.visibility = View.VISIBLE
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
                        binding.progressBar.visibility = View.VISIBLE
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
            binding.progressBar.visibility = View.GONE
            if (resp?.status == true) {
                val sharedPreferenceManager =  SharedPreferenceManager(this)
                sharedPreferenceManager.clearAllData()
                if (resp != null) {
                    sharedPreferenceManager.saveLoginResponse(resp)
                }
                finish()
            } else {
                Log.e("resp","resp")
            }
        }
        binding.tvDOB.setOnClickListener {
            val calendar = Calendar.getInstance()

            // Current date values
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

            // Create DatePickerDialog
            val datePickerDialog = DatePickerDialog(
                this,
                R.style.SpinnerDatePickerDialogStyle, // Optional: custom style
                { _, year, month, dayOfMonth ->
                    // Format selected date as DD-MM-YYYY
                    val formattedDate = String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year)
                    binding.tvDOB.text = formattedDate
                },
                currentYear,
                currentMonth,
                currentDay
            )

            // Restrict maximum date to 18 years ago
            val maxDateCalendar = Calendar.getInstance()
            maxDateCalendar.add(Calendar.YEAR, -18)
            datePickerDialog.datePicker.maxDate = maxDateCalendar.timeInMillis

            // Optional: restrict minimum date (e.g., 100 years ago)
            val minDateCalendar = Calendar.getInstance()
            minDateCalendar.add(Calendar.YEAR, -100)
            datePickerDialog.datePicker.minDate = minDateCalendar.timeInMillis

            datePickerDialog.show()
        }

        //incoming call received
        incomingCallReceived()

    }

    private fun incomingCallReceived() {
        //incoming call received
        val sharedPreferenceManager = SharedPreferenceManager(this)
        val userName = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.name.orEmpty()
        ZegoCallManager.initialize(application, userID, userName)
    }

    //image upload
    //image upload
    private fun openCropActivity(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "croppedImage.jpg"))

        val options = UCrop.Options().apply {
            setCompressionFormat(Bitmap.CompressFormat.JPEG)
            setCompressionQuality(90)
            setFreeStyleCropEnabled(true) // Allows user to adjust crop freely
        }

        UCrop.of(sourceUri, destinationUri)
            .withOptions(options)
            .withAspectRatio(1f, 1f) // Example: square crop
            .start(this)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = data?.let { UCrop.getOutput(it) }

            resultUri?.let { uri ->
                // Compress the cropped image
                val file = compressImage(applicationContext, uri, 50) // quality 50%

                file?.let {
                    // Clear previous selections
                    selectedImagesUri.clear()
                    selectedImagesFile.clear()

                    // Add new selection
                    selectedImagesUri.add(Uri.fromFile(it))
                    selectedImagesFile.add(it)
                    isInLetImageSelected = true

                    // Display image in ImageView
                    Glide.with(this)
                        .load(it)
                        .error(R.drawable.ic_image_place_holder)
                        .transform(CenterCrop(), RoundedCorners(10))
                        .into(binding.ivProfilePicture)

                    // Show file name
                    binding.tvFileName.text = it.name
                }
            }

        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = data?.let { UCrop.getError(it) }
            cropError?.printStackTrace()
        }
    }
    private fun compressImage(context: Context, uri: Uri, quality: Int): File? {
        return try {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)

            val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
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
        if (!ConstantUtils.hasEditText(binding.etBio, "Please Enter Bio")) ret = false
        return ret
    }

    override fun onResume() {
        super.onResume()
        //incoming call received
        incomingCallReceived()
    }

}