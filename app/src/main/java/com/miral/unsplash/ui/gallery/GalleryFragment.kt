package com.miral.unsplash.ui.gallery

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.miral.unsplash.R
import com.miral.unsplash.data.photo.model.Photo
import com.miral.unsplash.data.topic.model.Topic
import com.miral.unsplash.databinding.FragmentGalleryBinding
import com.miral.unsplash.ui.gallery.adapter.GalleryAdapter
import com.miral.unsplash.ui.gallery.adapter.GalleryLoadStateAdapter
import com.miral.unsplash.ui.gallery.adapter.GalleryTopicsAdapter
import com.miral.unsplash.utils.collectWhileStarted
import com.miral.unsplash.utils.dpToPx
import com.miral.unsplash.utils.hide
import com.miral.unsplash.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class GalleryFragment : Fragment(R.layout.fragment_gallery),
    GalleryTopicsAdapter.OnItemClickListener, GalleryAdapter.OnItemClickListener {
    private val viewModel: GalleryViewModel by viewModels()
    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private lateinit var photosAdapter: GalleryAdapter
    private lateinit var topicsAdapter: GalleryTopicsAdapter

    @ExperimentalCoroutinesApi
    @FlowPreview
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGalleryBinding.bind(view)

        setupPhotosAdapter()
        observeViewModel()
        photosLoadStateListener()
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    private fun observeViewModel() {

        viewModel.topicsFlow.collectWhileStarted(viewLifecycleOwner) {
            when (it) {
                is GalleryViewModel.TopicsEvent.Success -> {
                    setupTopicsAdapter(it.topics)
                    binding.tvTopicsErrorMessage.hide()
                }
                is GalleryViewModel.TopicsEvent.Loading -> {
                    binding.progressCircularLoading.show()
                }
                is GalleryViewModel.TopicsEvent.Failure -> {
                    binding.apply {
                        tvTopicsErrorMessage.show()
                        tvTopicsErrorMessage.text = it.errorText
                    }
                }
                is GalleryViewModel.TopicsEvent.Empty -> {
                    Toast.makeText(requireContext(), "Topics is Empty", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.photosFlow.collectLatest {
                photosAdapter.submitData(it)
            }
        }

        viewModel.galleryEvent.collectWhileStarted(viewLifecycleOwner) { event ->
            when (event) {
                is GalleryViewModel.GalleryEvent.NavigateToPhotoDetailsFragment -> {
                    val action = GalleryFragmentDirections.actionGalleryFragmentToPhotoDetailsFragment(event.photo)
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun setupPhotosAdapter() {
        photosAdapter = GalleryAdapter(this)
        val headerAdapter = GalleryLoadStateAdapter{ photosAdapter.retry() }
        val footerAdapter = GalleryLoadStateAdapter{ photosAdapter.retry() }
        val concatAdapter = photosAdapter.withLoadStateHeaderAndFooter(
            header = headerAdapter,
            footer = footerAdapter,
        )
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        staggeredGridLayoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE

        binding.apply {
            rvGallery.layoutManager = staggeredGridLayoutManager
            rvGallery.setHasFixedSize(true)
            rvGallery.adapter = concatAdapter
            rvGallery.addItemDecoration(GalleryGridSpacingItemDecoration(16.dpToPx(requireContext())))
            btnRetry.setOnClickListener {
                viewModel.getTopics()
                photosAdapter.retry()
            }
        }
    }

    private fun setupTopicsAdapter(topics: List<Topic>) {
        topicsAdapter = GalleryTopicsAdapter(topics, this)
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL

        binding.apply {
            rvGalleryTags.layoutManager = layoutManager
            rvGalleryTags.setHasFixedSize(true)
            rvGalleryTags.adapter = topicsAdapter
        }
    }

    private fun photosLoadStateListener() {
        photosAdapter.addLoadStateListener { combinedLoadStates ->
            binding.apply {
                rvGallery.isVisible = combinedLoadStates.source.refresh is LoadState.NotLoading
                progressCircularLoading.isVisible = combinedLoadStates.source.refresh is LoadState.Loading
                btnRetry.isVisible = combinedLoadStates.source.refresh is LoadState.Error
                tvErrorMessage.isVisible = combinedLoadStates.source.refresh is LoadState.Error
            }
        }
    }

    override fun onItemClick(id: String) {
        viewModel.onTopicSelected(id)
        (binding.rvGallery.layoutManager as StaggeredGridLayoutManager)
            .scrollToPositionWithOffset(0, 0)
    }

    override fun onPhotoClick(photo: Photo) {
        viewModel.onPhotoSelected(photo)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}