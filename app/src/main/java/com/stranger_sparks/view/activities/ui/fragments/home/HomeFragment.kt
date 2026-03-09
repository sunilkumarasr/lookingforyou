package com.stranger_sparks.view.activities.ui.fragments.home

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.stranger_sparks.BuildConfig
import com.stranger_sparks.R
import com.stranger_sparks.StrangerSparksApplication
import com.stranger_sparks.adapterrs.HomeAdapter
import com.stranger_sparks.data_model.UserProfileResponse
import com.stranger_sparks.databinding.FragmentHomeBinding
import com.stranger_sparks.datamodels.HomeDataModel
import com.stranger_sparks.fcm.ZegoCallManager
import com.stranger_sparks.inerfaces.OnItemClickListenerProfiles
import com.stranger_sparks.utils.ConstantUtils
import com.stranger_sparks.utils.SharedPreferenceManager
import com.stranger_sparks.view.activities.ui.activities.display_user.DisplayUserActivity
import com.stranger_sparks.viewmodel.SharedCityViewModel
import javax.inject.Inject

class HomeFragment : Fragment(), OnItemClickListenerProfiles {


    private lateinit var photoAdapter: HomeAdapter
    private var dataList = mutableListOf<HomeDataModel>()
    lateinit var binding: FragmentHomeBinding
    @Inject
    lateinit var viewModel:HomeViewModel
    lateinit var userID: String
    lateinit var sharedPreferenceManager: SharedPreferenceManager


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity().application as StrangerSparksApplication).applicationComponent.inject(this)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        sharedPreferenceManager = SharedPreferenceManager(requireActivity())
        userID = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.id.toString()

        sharedPreferenceManager.getSavedLoginResponseUser()?.data?.location?.let {
            viewModel.searchUserByLocationLiveData(
                it,userID
            )
            binding.progressbar.visibility=View.VISIBLE
        }

        val versionName = BuildConfig.VERSION_NAME
        viewModel.versionCheckLiveData(versionName)
        //Log.e("versionName: ", versionName)
        viewModel.versionCheckLiveData.observe(requireActivity()) {
            if (it?.status == false) {
                showBottomPopup()
            }
        }

        binding.rcvUserProfiles.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rcvUserProfiles.setHasFixedSize(true)
        binding.rcvUserProfiles.isNestedScrollingEnabled = false
        photoAdapter = HomeAdapter(requireContext(), this)
        binding.rcvUserProfiles.adapter = photoAdapter

        viewModel.searchUserByLocationLiveData.observe(requireActivity()){
            if(it?.status == true) {
                binding.progressbar.visibility=View.GONE
                binding.tvNoRecordsDefault.visibility = View.GONE
                binding.rcvUserProfiles.visibility = View.VISIBLE
                it?.data?.let { it1 -> photoAdapter.setDataList(it1) }
                photoAdapter.notifyDataSetChanged()
            }else{
                binding.tvNoRecordsDefault.visibility = View.VISIBLE
                binding.rcvUserProfiles.visibility = View.GONE
                binding.progressbar.visibility=View.GONE
            }
        }

        val sharedViewModel: SharedCityViewModel =
            ViewModelProvider(requireActivity()).get(SharedCityViewModel::class.java)
        sharedViewModel.getText()!!.observe(viewLifecycleOwner, object : Observer<String?> {
        override fun onChanged(value: String?) {
                if (value != null) {
                    sharedPreferenceManager.getSavedLoginResponseUser()?.data?.location?.let {
                        ConstantUtils.showToast(requireActivity(), value)
                        viewModel.searchUserByLocationLiveData(
                            value, userID
                        )
                    }
                }
            }
        })


        //incoming call received
        incomingCallReceived()

    }

    private fun incomingCallReceived() {
        val application = requireActivity().application
        val sharedPreferenceManager = SharedPreferenceManager(requireActivity())
        val userID = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.id.orEmpty()
        val userName = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.name.orEmpty()
        ZegoCallManager.initialize(application, userID, userName)
    }

    override fun clickOnCurrentPositionListener(item: UserProfileResponse.Data) {
        Intent(requireActivity(), DisplayUserActivity::class.java).also {
            it.putExtra("PROFILE_ID", item.id.toString())
            startActivity(it)
        }
    }

    private fun showBottomPopup() {

        val dialog = BottomSheetDialog(requireActivity())
        val view = layoutInflater.inflate(R.layout.bottom_version_layout, null)

        dialog.setContentView(view)
        dialog.setCancelable(false) // force update style (optional)

        val btnUpdate = view.findViewById<Button>(R.id.btnUpdate)

        btnUpdate.setOnClickListener {
            dialog.dismiss()
            // Open Play Store
            val appPackageName = "com.stranger_sparks"

            try {
                // Open Play Store App
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
                intent.setPackage("com.android.vending")
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // If Play Store app not installed, open in browser
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
                startActivity(intent)
            }
        }

        (dialog.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        ))?.setBackgroundResource(android.R.color.transparent)

        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        //incoming call received
        incomingCallReceived()
    }





}