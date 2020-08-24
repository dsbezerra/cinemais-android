package com.diegobezerra.cinemaisapp.ui.movie.playingcinemas

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.core.view.isGone
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.transition.TransitionManager
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.databinding.FragmentPlayingCinemasBinding
import com.diegobezerra.cinemaisapp.ui.movie.playingcinemas.PlayingCinemasViewModel.Companion.STATE_SCHEDULE
import com.diegobezerra.cinemaisapp.ui.schedule.ScheduleAdapter
import com.diegobezerra.cinemaisapp.ui.schedule.filters.ScheduleFiltersAdapter
import com.diegobezerra.cinemaisapp.widget.SpaceItemDecoration
import com.diegobezerra.core.event.EventObserver
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class PlayingCinemasFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val playingCinemasViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory)
            .get(PlayingCinemasViewModel::class.java)
    }

    private val playingCinemasAdapter by lazy { PlayingCinemasAdapter(playingCinemasViewModel) }

    private val filtersAdapter by lazy { ScheduleFiltersAdapter(playingCinemasViewModel) }

    private lateinit var binding: FragmentPlayingCinemasBinding

    private var behavior: BottomSheetBehavior<*>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPlayingCinemasBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@PlayingCinemasFragment
            viewModel = playingCinemasViewModel
        }

        binding.tabs.setupWithViewPager(binding.viewpager)
        binding.playingCinemas.run {
            adapter = playingCinemasAdapter
            setHasFixedSize(true)
        }

        binding.filtersRecyclerView.apply {
            adapter = filtersAdapter
            addItemDecoration(
                SpaceItemDecoration(
                    resources.getDimension(R.dimen.spacing_small).toInt(),
                    resources.getDimension(R.dimen.spacing_medium).toInt(),
                    addSpaceAboveFirstItem = true,
                    addSpaceBelowLastItem = true
                )
            )
            setHasFixedSize(true)
        }

        playingCinemasViewModel.filters.observe(viewLifecycleOwner, {
            filtersAdapter.submitList(it)
        })

        playingCinemasViewModel.cinemas.observe(viewLifecycleOwner, {
            playingCinemasAdapter.data = it
            playingCinemasAdapter.notifyDataSetChanged()
        })

        playingCinemasViewModel.schedule.observe(viewLifecycleOwner, { schedule ->
            binding.viewpager.adapter =
                ScheduleAdapter(childFragmentManager, requireActivity(), schedule, true)
        })

        playingCinemasViewModel.state.observe(viewLifecycleOwner, {
            // Make sure our views are visible just in case the last onSlide
            // set them to invisible.
            interpolateChildViews(1f)
            updateViews(it)
        })

        playingCinemasViewModel.toggleSheetAction.observe(viewLifecycleOwner, EventObserver {
            toggleSheet()
        })

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        behavior = BottomSheetBehavior.from(binding.sheet)
        behavior?.addBottomSheetCallback(
            object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    // No-op
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    interpolateChildViews(slideOffset)
                }
            }
        )
    }

    fun peek(movieId: Int) {
        behavior?.let {
            val finalPeekHeight = resources.getDimension(R.dimen.playing_rooms_peek_height)
                .toInt()
            ValueAnimator.ofInt(0, finalPeekHeight)
                .apply {
                    duration = 1000
                    startDelay = 250
                    interpolator = FastOutSlowInInterpolator()
                    addUpdateListener { animator ->
                        it.peekHeight = animator.animatedValue as Int
                    }
                    doOnEnd {
                        playingCinemasViewModel.onReady(movieId)
                    }
                }
                .start()
        }
    }

    private fun toggleSheet() {
        behavior?.let {
            when (it.state) {
                STATE_EXPANDED -> it.state = STATE_COLLAPSED
                STATE_COLLAPSED -> it.state = STATE_EXPANDED
                else -> Unit // No-op
            }
        }
    }

    private fun interpolateChildViews(offset: Float) {
        binding.expandOrCollapseArrow.rotation = offset * 180f
        if (!binding.scheduleContainer.isGone) {
            binding.scheduleContainer.alpha = offset
            binding.filterButton.alpha = offset
        }
        if (!binding.playingCinemas.isGone) {
            binding.playingCinemas.alpha = offset
        }
    }

    private fun updateViews(state: Int) {
        TransitionManager.beginDelayedTransition(binding.sheet)
        binding.tabs.scaleX = if (state == STATE_SCHEDULE) {
            1f
        } else {
            0f
        }
    }

}
