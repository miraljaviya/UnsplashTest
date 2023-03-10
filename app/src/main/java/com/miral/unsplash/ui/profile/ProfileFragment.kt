package com.miral.unsplash.ui.profile

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.miral.unsplash.R
import com.miral.unsplash.data.user.model.User
import com.miral.unsplash.databinding.FragmentProfileBinding
import com.miral.unsplash.utils.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private val viewModel: ProfileViewModel by viewModels()
    private val args: ProfileFragmentArgs by navArgs()
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)
        val user = args.user

        initView(user)
        observeViewModel()
        setHasOptionsMenu(true)
    }

    private fun initView(user: User) {
        binding.apply {
            ivProfilePicture.loadProfilePicture(user)
            tvName.text = user.name
            if (user.location != null) {
                tvLocation.text = user.location
                tvLocation.setOnClickListener { viewModel.onLocationClick(user.location) }
            } else {
                tvLocation.hide()
            }
            if (user.bio != null) tvBio.text = user.bio else tvBio.hide()
            tvItemPhotos.text = (user.total_photos ?: 0).toPrettyString()
            tvItemLikes.text = (user.total_likes ?: 0).toPrettyString()
            tvItemCollection.text = (user.total_collections ?: 0).toPrettyString()
        }
    }

    private fun observeViewModel() {
        viewModel.profileEvent.collectWhileStarted(viewLifecycleOwner) { event ->
            when (event) {
                is ProfileViewModel.ProfileEvent.NavigateToMaps -> {
                    requireContext().openLocationInMaps(event.location)
                }
                is ProfileViewModel.ProfileEvent.NavigateToBrowser -> {
                    requireContext().openProfileInBrowser(event.links)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_profile, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_open_in_browser -> {
                if (args.user.links == null) {
                    true
                } else {
                    viewModel.onOpenInBrowser(args.user.links!!)
                    true
                }
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}