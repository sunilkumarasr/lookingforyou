package com.stranger_sparks.view.activities.ui.fragments.my_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.stranger_sparks.R
import com.stranger_sparks.databinding.FragmentLoginBinding
import com.stranger_sparks.databinding.FragmentMyAccountBinding
import com.stranger_sparks.fcm.ZegoCallManager
import com.stranger_sparks.utils.Constants
import com.stranger_sparks.utils.SharedPreferenceManager
import com.stranger_sparks.viewmodel.LoginViewModel

class MyAccountFragment : Fragment() {

    lateinit var binding: FragmentMyAccountBinding



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyAccountBinding.inflate(inflater, container, false)
        val viewModel: MyAccountViewModel = ViewModelProvider(this).get(
            MyAccountViewModel::class.java
        )
        viewModel.binding = binding
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel!!.observerEvents.observe(viewLifecycleOwner) { observerEvents ->
            observerEvents?.let { validateViewModelEvents(observerEvents) }
        }


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

    private fun validateViewModelEvents(observerEvents: Constants.ObserverEvents) {
        if (observerEvents === Constants.ObserverEvents.OPEN_SIGN_IN_SCREEN) {

            Navigation.findNavController(binding.root).navigate(R.id.splash_screen_to_login_screen)
        }
    }


    override fun onResume() {
        super.onResume()
        //incoming call received
        incomingCallReceived()
    }

}