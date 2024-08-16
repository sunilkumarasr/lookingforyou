package com.stranger_sparks.view.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.stranger_sparks.R
import com.stranger_sparks.StrangerSparksApplication
import com.stranger_sparks.adapterrs.SuggestionCityAdapter
import com.stranger_sparks.data_model.SuggestionCityResponse
import com.stranger_sparks.databinding.ActivityHomeBinding
import com.stranger_sparks.utils.ConstantUtils
import com.stranger_sparks.view.activities.ui.activities.chat.ChatActivity
import com.stranger_sparks.view.activities.ui.activities.help.HelpActivity
import com.stranger_sparks.view.activities.ui.activities.my_account.MyAccount
import com.stranger_sparks.view.activities.ui.activities.notifications.Notifications
import com.stranger_sparks.view.activities.ui.activities.settings.SettingsActivity
import com.stranger_sparks.view.activities.ui.activities.wallet.WalletActivity
import com.stranger_sparks.view.activities.ui.fragments.curved_menu.BlankBottomSheetFragment
import com.stranger_sparks.viewmodel.HomeActivityViewModel
import com.stranger_sparks.viewmodel.SharedCityViewModel
import javax.inject.Inject


class HomeActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityHomeBinding

    @Inject
    lateinit var viewModel: HomeActivityViewModel

    private var locationAdapter: ArrayAdapter<SuggestionCityResponse.Data>? = null
    private val locationModelList: List<SuggestionCityResponse.Data> = ArrayList<SuggestionCityResponse.Data>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (this.application as StrangerSparksApplication).applicationComponent.inject(this)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView: BottomNavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_activity_home)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_my_account,
                R.id.navigation_settings,
                R.id.navigation_wallet,
                R.id.navigation_help
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        supportActionBar?.hide()




        binding.navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    navScreenLoad("Home")
                }
                R.id.navigation_settings -> {
                    navScreenLoad("Settings")
                }
                R.id.navigation_my_account -> {
                    navScreenLoad("My Account")
                } R.id.navigation_help -> {
                    navScreenLoad("Help")
                }R.id.navigation_wallet -> {
                    navScreenLoad("Wallet")
                }
            }
            true

        }





        val sharedViewModel: SharedCityViewModel =
            ViewModelProvider(this).get(SharedCityViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.ivFilter.setOnClickListener {
            val bottomSheetDialog: BlankBottomSheetFragment = BlankBottomSheetFragment()
            bottomSheetDialog.show(supportFragmentManager, "Bottom Sheet Dialog Fragment")
        }
        binding.ivNotification.setOnClickListener {
            Intent(applicationContext, Notifications::class.java).also {
                startActivity(it)

            }
        }
        binding.ivWhatsapp.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=+9390255762&text=Hi%20there"))
            startActivity(browserIntent)
        }

        binding.actCity?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d("FirstFrag","$p0")
                if(p0?.length!! > 2){
                    viewModel.citiesLiveData(p0.toString())
                }

            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
        binding.actCity?.setOnItemClickListener() { parent, _, position, id ->
            val selectedPoi = parent.adapter.getItem(position) as SuggestionCityResponse.Data?
            binding.actCity?.setText(selectedPoi?.location)
            ConstantUtils.hideKeyboard(applicationContext, binding.actCity)
            selectedPoi?.location?.let { sharedViewModel.setText(it) }
        }


        viewModel.citiesLiveData.observe(this){
            try {
                val adapter = SuggestionCityAdapter(
                    applicationContext,
                    R.layout.row_item_auto_completed,
                    it?.data
                )
                binding.actCity?.setAdapter(adapter)
                binding.actCity?.threshold = 3

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }


    fun navScreenLoad(screenType: String) {
        //val navController = findNavController(R.id.nav_view)

        when (screenType) {
            "Home" -> {
                navController.navigate(R.id.navigation_home)
            }

            "My Account" -> {
                Intent(applicationContext, MyAccount::class.java).also {
                    startActivity(it)

                }
            }

            "Settings" -> {
                Intent(applicationContext, SettingsActivity::class.java).also {
                    startActivity(it)

                }
            }
            "Wallet" -> {
                Intent(applicationContext, WalletActivity::class.java).also {
                    startActivity(it)

                }
            }
            "Help" -> {
                Intent(applicationContext, HelpActivity::class.java).also {
                    startActivity(it)

                }
            }
        }
    }

    override fun onBackPressed() {
       // super.onBackPressed()
        exitDialog()
    }

    private fun exitDialog(){
        val dialogBuilder = AlertDialog.Builder(this@HomeActivity)
        dialogBuilder.setTitle("Exit")
        dialogBuilder.setMessage("Are you sure want to exit this app?")
        dialogBuilder.setPositiveButton("OK", { dialog, whichButton ->
            finishAffinity()
            dialog.dismiss()
        })
        dialogBuilder.setNegativeButton("Cancel", { dialog, whichButton ->
            dialog.dismiss()
        })
        val b = dialogBuilder.create()
        b.show()
    }

    
}