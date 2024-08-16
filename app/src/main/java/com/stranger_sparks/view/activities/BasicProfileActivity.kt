package com.stranger_sparks.view.activities

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
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.stranger_sparks.R
import com.stranger_sparks.StrangerSparksApplication
import com.stranger_sparks.data_model.CitysListResponse
import com.stranger_sparks.data_model.LoginResponse
import com.stranger_sparks.databinding.ActivityBasicProfileBinding
import com.stranger_sparks.utils.ConstantUtils
import com.stranger_sparks.utils.Constants
import com.stranger_sparks.utils.FileUtil
import com.stranger_sparks.utils.SharedPreferenceManager
import com.stranger_sparks.view.activities.ui.activities.webview.WebViewUrlLoad
import com.stranger_sparks.viewmodel.BasicProfileViewModel
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import id.zelory.compressor.Compressor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.Calendar
import javax.inject.Inject


class BasicProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityBasicProfileBinding

    @Inject
    lateinit var viewModel: BasicProfileViewModel
    var fullName: String = ""
    var emailId: String = ""
    var gender: String = ""
    var hobbies: String = ""
    var height: String = ""
    var locationAddress: String = ""
    var dob: String = ""
    var bio: String = ""
    var alterNativeNumber: String = ""
    var imgPos: Int = 0
    val txtList: MutableList<TextView> = mutableListOf()
    val imgUriList: Array<Uri?> = arrayOfNulls(1)
    var isInLetImageSelected: Boolean = false
    private var selectedImagesUri = mutableListOf<Uri>()
    private var selectedImagesFile = mutableListOf<File>()
    var mYear: Int? = null
    var mMonth: Int? = null
    var mDay: Int? = null
    var maritalSelection: String = ""
    var languageSelection: String = ""

    val mobileNumberPattern = "^[6-9]\\d{9}$"
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (this.application as StrangerSparksApplication).applicationComponent.inject(this)
        binding = ActivityBasicProfileBinding.inflate(layoutInflater)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setContentView(binding.root)
        ConstantUtils.createFullScreen(this)
        txtList += listOf(
            binding.tvFileName
            // Add more Uri objects as needed
        )
        viewModel.inputSignal.observe(this) {

            if (it != null) {
                validateViewModelEvents(it)

            }
        }

        val getContent =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                // 'ActivityResultCallback': Handle the returned Uri
                if (uri != null) {
                    selectedImagesUri.clear()
                    selectedImagesFile.clear()

                    selectedImagesUri.add(uri)
                    selectedImagesFile.add(FileUtil.from(applicationContext, uri))
                    isInLetImageSelected = true

                    binding.ivProfilePicture.setImageURI(selectedImagesUri[0])
                    binding.tvFileName.text = selectedImagesFile[0].name
                }
            }

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

        binding.llUploadProfilePicture.setOnClickListener {
            /* cropResultLauncher.launch(
                 CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                     .setMinCropWindowSize(1000, 1000)
                     .getIntent(this)
             )*/
            getContent.launch("image/*")
        }

        binding.txtTerms.setOnClickListener {
            Intent(applicationContext, WebViewUrlLoad::class.java).also {
                it.putExtra("url_type", Constants.ObserverEvents.TERMS_AND_CONDITIONS.toString())
                startActivity(it)
            }
        }

        val heightslist = mutableListOf<String>()
        for (feet in 4..7) {
            for (inch in 0..12) {
                val height = "$feet.$inch"
                if (height.toFloat() >= 4.0 && height.toFloat() <= 7.5) {
                    heightslist.add(height + " ft")
                }
            }
        }
        //default value
        heightslist.add(0, "Height")
        val adapter = CustomArrayAdapter(this, R.layout.custom_spinner_dropdown_item, heightslist)
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        binding.spHeight.adapter = adapter

        //marital
        binding.materialRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedmaterial = findViewById<RadioButton>(checkedId)
            maritalSelection = selectedmaterial.text.toString()
        }



        binding.btnContinue.setOnClickListener {

            languageS()

            if (!ConstantUtils.isNetworkConnected(applicationContext)) {
                ConstantUtils.alertDialog("Please Check Internet Connection", this)
            } else if (checkValidation()) {

                fullName = binding.etFullName.text.toString().trim()
                hobbies = binding.etStudy.text.toString().trim()
                if (!binding.etEmailId.text.toString().isNullOrBlank()) {
                    emailId = binding.etEmailId.text.toString().trim()
                } else {
                    emailId = ""
                }

                if (!binding.etEmailId.text.toString().isNullOrBlank()) {
                    alterNativeNumber = binding.etAlterNativeMobileNumber.text.toString().trim()
                } else {
                    alterNativeNumber = ""
                }

                height = binding.spHeight.selectedItem.toString().trim()
                bio = binding.etBio.text.toString().trim()
                locationAddress = binding.llAutoCompleteTextView.text.toString().trim()
                dob = binding.tvDOB.text.toString().trim()

                if (!binding.rbMale.isChecked && !binding.rbFemale.isChecked) {
                    ConstantUtils.showToast(this, "Please Select Gender")
                    return@setOnClickListener
                } else {

                    if (!binding.checkTerms.isChecked) {
                        ConstantUtils.showToast(this, "Please accept our Terms & Conditions")

                        return@setOnClickListener
                    }
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
                            val compressedImageFile =
                                Compressor(this).compressToFile(selectedImagesFile[0])



                            inletMultiPart =
                                compressedImageFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                                    ?.let {
                                        MultipartBody.Part.createFormData(
                                            "image",
                                            compressedImageFile.name,
                                            it
                                        )
                                    }


                        }
                        var selectedGender: String = ""
                        if (binding.rbMale.isChecked) {
                            selectedGender = "Male"
                        } else if (binding.rbFemale.isChecked) {
                            selectedGender = "Female"
                        }
                        if (inletMultiPart != null) {
                            val sharedPreferenceManager = SharedPreferenceManager(this)
                            binding.progressLay.progressBar.visibility = View.VISIBLE
                            try {
                                var phnunber =
                                    sharedPreferenceManager.getSavedLoginResponseUser()?.data?.phone
                                var userID =
                                    sharedPreferenceManager.getSavedLoginResponseUser()?.data?.id

                                viewModel.updateProfile(
                                    fullName.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    dob.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    locationAddress.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    emailId.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    phnunber.toString()
                                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    height.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    bio.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    userID.toString()
                                        .toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    selectedGender.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    hobbies.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    alterNativeNumber.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    maritalSelection.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    languageSelection.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                    inletMultiPart
                                )

                            } catch (e: Exception) {
                                Log.v("Purushotham", e.message.toString())
                            }
                        }
                    } else {
                        ConstantUtils.showToast(this, "Please Upload Picture")
                        return@setOnClickListener
                    }
                }

            }

        }
        viewModel.upDateProfileLiveData.observe(this) { resp ->
            resp?.message?.let { ConstantUtils.showToast(this, it) }
            binding.progressLay.progressBar.visibility = View.GONE
            if (resp?.status == true) {
                /*val sharedPreferenceManager =  SharedPreferenceManager(this)
                var loginResponse: LoginResponse? = sharedPreferenceManager.getSavedLoginResponseUser()
                loginResponse?.data?.profile_completed = "1"
                sharedPreferenceManager.clearAllData()
                if (loginResponse != null) {
                    sharedPreferenceManager.saveLoginResponse(loginResponse)
                }

                *//*if (resp != null) {
                    sharedPreferenceManager.saveLoginResponse(resp)
                }*//*
                if(sharedPreferenceManager.getSavedLoginResponseUser()?.data?.profile_completed == "1"){
                    Intent(applicationContext, Subcription::class.java).also {
                        startActivity(it)

                    }

                }else{

                }*/


                if (resp?.status == true) {
                    val sharedPreferenceManager = SharedPreferenceManager(this)
                    sharedPreferenceManager.clearAllData()
                    if (resp != null) {
                        sharedPreferenceManager.saveLoginResponse(resp)
                    }


                    if (sharedPreferenceManager.getSavedLoginResponseUser()?.data?.profile_completed == "1") {
//                        Intent(applicationContext, Subcription::class.java).also {
//                            startActivity(it)

                        Intent(applicationContext, HomeActivity::class.java).also {
                            startActivity(it)


                        }
                    }
                } else {
                    //res?.message?.let { ConstantUtils.showToast(this, it) }
                }
            } else {
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

    class CustomArrayAdapter(context: Context, resource: Int, objects: List<String>) :
        ArrayAdapter<String>(context, resource, objects) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            if (position == 0) {
                (view as TextView).setTextColor(ContextCompat.getColor(context, R.color.dark_grey))
            }
            return view
        }
    }

    fun languageS() {
        val selectedLanguages = mutableListOf<String>()
        if (binding.checkBoxTelugu.isChecked) selectedLanguages.add("Telugu")
        if (binding.checkBoxEnglish.isChecked) selectedLanguages.add("English")
        if (binding.checkBoxHindi.isChecked) selectedLanguages.add("Hindi")
        languageSelection = selectedLanguages.joinToString(", ")
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
        if (binding.tvDOB.text.toString()
                .trim() == applicationContext.resources.getString(R.string.selecte_dob)
        ) {
            ConstantUtils.showToast(applicationContext, "Please select Date Birth")
            ret = false
        }
        if (binding.spHeight.selectedItem.toString().equals("Height")) {
            Toast.makeText(applicationContext, "Please select Height", Toast.LENGTH_SHORT).show()
            ret = false
        }
        if (languageSelection.equals("")) {
            ConstantUtils.showToast(applicationContext, "Please select at least one language")
            ret = false
        }

        if (binding.llAutoCompleteTextView.text.toString().equals("")) {
            Toast.makeText(applicationContext, "please select location", Toast.LENGTH_SHORT).show()
            ret = false
        }

        if (binding.llAutoCompleteTextView.text.toString().equals("")) {
            Toast.makeText(applicationContext, "please select location", Toast.LENGTH_SHORT).show()
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

    fun validateViewModelEvents(observerEvents: String?) {
        if (observerEvents === Constants.ObserverEvents.GOTO_OTP.toString()) {

        } else if (observerEvents === Constants.ObserverEvents.GOTO_SIGN_UP.toString()) {

        } else if (observerEvents === Constants.ObserverEvents.GOTO_SIGN_IN.toString()) {

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

}