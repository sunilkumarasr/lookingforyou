package com.stranger_sparks.view.activities.ui.activities.chat.chat_room

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.stranger_sparks.R
import com.stranger_sparks.StrangerSparksApplication
import com.stranger_sparks.adapterrs.ChatRoomAdapter
import com.stranger_sparks.data_model.ChatResponse
import com.stranger_sparks.databinding.ActivityChatRoomBinding
import com.stranger_sparks.datamodels.Message
import com.stranger_sparks.fcm.ZegoCallManager
import com.stranger_sparks.inerfaces.OnItemClickListenerChatRoom
import com.stranger_sparks.utils.ConstantUtils
import com.stranger_sparks.utils.FileUtil
import com.stranger_sparks.utils.SharedPreferenceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ChatRoom : AppCompatActivity(), OnItemClickListenerChatRoom {

    lateinit var binding: ActivityChatRoomBinding

    @Inject
    lateinit var viewModel: ChatRoomViewModel
    private lateinit var chatRoomAdapter: ChatRoomAdapter
    lateinit var userID: String
    var messages: ArrayList<Message>? = null
    lateinit var profile_id: String
    var isInLetImageSelected: Boolean = false
    private var selectedImagesUri = mutableListOf<Uri>()
    private var selectedImagesFile = mutableListOf<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        ConstantUtils.darkModeDisable()
        super.onCreate(savedInstanceState)
        (this.application as StrangerSparksApplication).applicationComponent.inject(this)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profile_id = intent.extras?.getString("PROFILE_ID").toString()
        val sharedPreferenceManager = SharedPreferenceManager(this)
        userID = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.id.toString()

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        /*messages = ArrayList()
        messages!!.add(Message("Hello world", "1", 2023))
        messages!!.add(Message("Hello world", "2", 2023))
        messages!!.add(Message("How Are you", "1", 2023))
        messages!!.add(Message("Fine", "2", 2023))
        messages!!.add(Message("How Are you", "1", 2023))
        messages!!.add(Message("Thanks You", "2", 2023))
        messages!!.add(Message("Hello world", "1", 2023))*/
        chatRoomAdapter = ChatRoomAdapter(this@ChatRoom, userID, profile_id, this)
        binding!!.rcvChat.layoutManager = LinearLayoutManager(this@ChatRoom)
        binding!!.rcvChat.adapter = chatRoomAdapter
        // notificationAdapter.setDataList(dataList)


        viewModel.getMessageLiveData(userID, profile_id)
        viewModel.getMessageLiveData.observe(this) {
            if (it?.status == true) {
                if (it?.data?.size!! > 0) {
                    binding.tvNoRecordsDefault.visibility = View.GONE
                    binding.rcvChat.visibility = View.VISIBLE
                    it?.data?.let { it1 -> chatRoomAdapter.setDataList(it1.reversed()) }
                    chatRoomAdapter.notifyDataSetChanged()
                    binding.rcvChat.post(Runnable {
                        binding.rcvChat.smoothScrollToPosition(
                            chatRoomAdapter.getItemCount() - 1
                        )
                    })
                } else {
                    binding.tvNoRecordsDefault.visibility = View.VISIBLE
                    binding.rcvChat.visibility = View.GONE
                }
            } else {
                binding.tvNoRecordsDefault.visibility = View.VISIBLE
                binding.rcvChat.visibility = View.GONE
            }
        }
        binding.ivClose.setOnClickListener {
            finish()
        }
        binding.ivSendMessage.setOnClickListener {
            if (!binding.etEnteredMessage.text.toString().trim().isNullOrBlank()) {
                /* messages!!.add(Message(binding.etEnteredMessage.text.toString().trim(), "1", 2023))
                 chatRoomAdapter.notifyDataSetChanged()*/
                viewModel.sendMessageLiveData(
                    userID,
                    profile_id,
                    binding.etEnteredMessage.text.toString().trim()
                )
            } else {
                ConstantUtils.showToast(applicationContext, "Please enter Message...!")
            }

        }
        viewModel.sendMessageLiveData.observe(this) {
            if (it?.status == true) {
                binding.etEnteredMessage.text.clear()
                viewModel.getMessageLiveData(userID, profile_id)
            } else {
                if (it != null) {
                    ConstantUtils.showToast(applicationContext, it.message)
                }
            }
        }
        val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // 'ActivityResultCallback': Handle the returned Uri
            if (uri != null) {
                selectedImagesUri.clear()
                selectedImagesFile.clear()

                selectedImagesUri.add(uri)
                selectedImagesFile.add(FileUtil.from(applicationContext, uri))
                isInLetImageSelected = true

                val dialog = Dialog(this@ChatRoom, R.style.AlertDialogCustom)
                dialog.setContentView(R.layout.custom_alert_chat_image)
                dialog.setCancelable(false)

                val btnCancel = dialog.findViewById<AppCompatButton>(R.id.btnCancel)
                val btnSend = dialog.findViewById<AppCompatButton>(R.id.btnSend)
                val ivChatImage = dialog.findViewById<ImageView>(R.id.ivChatImage)
                val progressBar = dialog.findViewById<ProgressBar>(R.id.progressBar)
                ivChatImage.setImageURI(selectedImagesUri[0])
                btnSend.setOnClickListener { v: View? ->
                    /*callSendImageCahtImage()*/
                    if (!ConstantUtils.isNetworkConnected(applicationContext)) {
                        ConstantUtils.alertDialog("Please Check Internet Connection", this)
                    } else {
                        var inletMultiPart: MultipartBody.Part?
                        if (selectedImagesFile[0]?.path.isNullOrBlank()) {
                            ConstantUtils.showToast(this, "Please Select Picture")
                            return@setOnClickListener
                        } else {
                            val compressedFile = compressImageFile(selectedImagesFile[0])
                            val requestFile = compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                            inletMultiPart = MultipartBody.Part.createFormData(
                                "image",
                                compressedFile.name,
                                requestFile
                            )
                        }

                        if (inletMultiPart != null) {
                            val sharedPreferenceManager = SharedPreferenceManager(this)
                            // progressBar.visibility = View.VISIBLE
                            try {
                                var phnunber =
                                    sharedPreferenceManager.getSavedLoginResponseUser()?.data?.phone
                                var userID =
                                    sharedPreferenceManager.getSavedLoginResponseUser()?.data?.id

                                if (isInLetImageSelected) {
                                    dialog.dismiss()
                                    //loading
                                    binding.progressBar.visibility = View.VISIBLE

                                    userID?.let {
                                        viewModel.sendChartImage(
                                            it.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                            profile_id.toRequestBody("multipart/form-data".toMediaTypeOrNull()),
                                            inletMultiPart
                                        )
                                    }
                                }

                            } catch (e: Exception) {
                                Log.v("Purushotham", e.message.toString())
                            }
                        }

                        // dialog.dismiss()
                    }

                }
                btnCancel.setOnClickListener { v: View? ->
                    isInLetImageSelected = false
                    dialog.dismiss()
                }
                dialog.show()

                /*binding.ivProfilePicture.setImageURI(selectedImagesUri[0])
                binding.tvFileName.text = selectedImagesFile[0].name*/

            }
        }
        binding.ivAttachment.setOnClickListener { }
        binding.ivCamera.setOnClickListener { getContent.launch("image/*") }
        viewModel.sendChartImageLiveData.observe(this) {
            //loading dismiss
            binding.progressBar.visibility = View.GONE

            if (it?.status == true) {
                isInLetImageSelected = false
                viewModel.getMessageLiveData(userID, profile_id)
            } else {
                if (it != null) {
                    ConstantUtils.showToast(applicationContext, it.message)
                }
            }
        }
        startUpdates()

        viewModel.deleteMessageLiveData.observe(this) {
            if (it?.status == true) {
                viewModel.getMessageLiveData(userID, profile_id)
            } else {
                Toast.makeText(this@ChatRoom, "Failed", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.deleteMessageBothData.observe(this) {
            if (it?.status == true) {
                viewModel.getMessageLiveData(userID, profile_id)
            } else {
                Toast.makeText(this@ChatRoom, "Failed", Toast.LENGTH_SHORT).show()
            }
        }

        //incoming call received
        incomingCallReceived()


    }

    private fun incomingCallReceived() {
        val sharedPreferenceManager = SharedPreferenceManager(this)
        val userName = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.name.orEmpty()
        ZegoCallManager.initialize(application, userID, userName)
    }

    fun startUpdates() {
        val lifecycle = this //in Activity

        lifecycle.lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // this block is automatically executed when moving into
                // the started state, and cancelled when stopping.
                while (true) {
                    viewModel.getMessageLiveData(userID, profile_id) // the function to repeat
                    delay(5000)
                }
            }
        }

    }

    override fun clickOnCurrentPositionListener(item: ChatResponse.Data, type: String) {
        if (type.equals("single")) {
            viewModel.delete_only_for_you_post(item.sender_id, item.id)
        } else {
            viewModel.delete_only_for_both_post(item.sender_id, item.id)
        }
    }

    fun compressImageFile(originalFile: File): File {
        // Decode the original file to a bitmap
        val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath)

        // Compress the bitmap to JPEG with reduced quality
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream) // 70 = compression quality

        // Save the compressed bytes to a new file
        val compressedFile = File(this@ChatRoom.cacheDir, "compressed_${originalFile.name}")
        val fos = FileOutputStream(compressedFile)
        fos.write(outputStream.toByteArray())
        fos.flush()
        fos.close()

        return compressedFile
    }

    override fun onResume() {
        super.onResume()
        //incoming call received
        incomingCallReceived()
    }

}