package com.stranger_sparks.view.activities.ui.activities.my_account

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.stranger_sparks.R
import com.stranger_sparks.StrangerSparksApplication
import com.stranger_sparks.adapterrs.HomeAdapter
import com.stranger_sparks.adapterrs.SuggestionCityAdapter
import com.stranger_sparks.data_model.CitysListResponse
import com.stranger_sparks.data_model.SuggestionCityResponse
import com.stranger_sparks.data_model.UserProfileResponse
import com.stranger_sparks.databinding.ActivityMyAccountBinding
import com.stranger_sparks.inerfaces.OnItemClickListenerProfiles
import com.stranger_sparks.utils.ConstantUtils
import com.stranger_sparks.utils.SharedPreferenceManager
import com.stranger_sparks.view.activities.ui.activities.display_user.DisplayUserActivity
import javax.inject.Inject

class MyAccount : AppCompatActivity(), OnItemClickListenerProfiles {
    lateinit var binding: ActivityMyAccountBinding
    @Inject
    lateinit var viewModel: MyAccountViewModel

    private lateinit var photoAdapter: HomeAdapter
    lateinit var userID: String
    lateinit var sharedPreferenceManager: SharedPreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (this.application as StrangerSparksApplication).applicationComponent.inject(this)
        binding = ActivityMyAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        sharedPreferenceManager = SharedPreferenceManager(applicationContext)
        userID = sharedPreferenceManager.getSavedLoginResponseUser()?.data?.id.toString()

        sharedPreferenceManager.getSavedLoginResponseUser()?.data?.location?.let {
            viewModel.searchUserByLocationLiveData(
                it, userID
            )
        }
        binding.progressbar.visibility=View.VISIBLE
        binding.btnSearchCitySubmit.setOnClickListener {

            if(!binding.llAutoCompleteTextView.text.isNullOrBlank()){
                ConstantUtils.hideKeyboard(applicationContext, binding.llAutoCompleteTextView)
                viewModel.searchUserByLocationLiveData(
                    binding.llAutoCompleteTextView.text.toString(), userID
                )
                binding.progressbar.visibility=View.VISIBLE
            }else{
                ConstantUtils.showToast(applicationContext, "Please Enter City Name")
            }
        }

        viewModel.getCitysList()
        viewModel.cityListLiveData.observe(this) { response ->
            response?.data?.let { cities ->
                val cityNames = cities.map { it.name }
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cityNames)
                binding.llAutoCompleteTextView.setAdapter(adapter)
                binding.llAutoCompleteTextView.threshold = 1
                binding.progressbar.visibility=View.GONE
                binding.llAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                    val selectedCityName = parent.getItemAtPosition(position).toString()
                    if(!binding.llAutoCompleteTextView.text.isNullOrBlank()){
                        ConstantUtils.hideKeyboard(applicationContext, binding.llAutoCompleteTextView)
                        viewModel.searchUserByLocationLiveData(selectedCityName, userID)
                        Toast.makeText(this, selectedCityName + userID, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.rcvUserProfiles.layoutManager = GridLayoutManager(applicationContext, 2)
        photoAdapter = HomeAdapter(applicationContext, this)
        binding.rcvUserProfiles.adapter = photoAdapter

        viewModel.searchUserByLocationLiveData.observe(this){
            if(it?.status == true) {
                binding.progressbar.visibility=View.GONE
                binding.tvNoRecordsDefault.visibility = View.GONE
                binding.rcvUserProfiles.visibility = View.VISIBLE
                /*photoAdapter.setDataList(it)*/
                photoAdapter.setDataList(it.data)
                //it?.data?.let { it1 -> photoAdapter.setDataList(it1) }
                photoAdapter.notifyDataSetChanged()
            }else{
                binding.progressbar.visibility=View.GONE
                binding.tvNoRecordsDefault.visibility = View.VISIBLE
                binding.rcvUserProfiles.visibility = View.GONE
            }
        }

        binding.ivClose.setOnClickListener { finish() }
    }

    override fun clickOnCurrentPositionListener(item: UserProfileResponse.Data) {
        Intent(applicationContext, DisplayUserActivity::class.java).also {
            it.putExtra("PROFILE_ID", item.id.toString())
            startActivity(it)
        }
    }
}