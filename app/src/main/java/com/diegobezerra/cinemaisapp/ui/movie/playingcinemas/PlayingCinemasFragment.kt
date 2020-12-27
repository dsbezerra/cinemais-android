package com.diegobezerra.cinemaisapp.ui.movie.playingcinemas

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.observe
import androidx.transition.TransitionManager
import com.diegobezerra.cinemaisapp.R
import com.diegobezerra.cinemaisapp.databinding.FragmentPlayingCinemasBinding
import com.diegobezerra.cinemaisapp.ui.movie.playingcinemas.PlayingCinemasViewModel.Companion.STATE_SCHEDULE
import com.diegobezerra.cinemaisapp.ui.schedule.ScheduleAdapter
import com.diegobezerra.cinemaisapp.ui.schedule.filters.ScheduleFiltersAdapter
import com.diegobezerra.cinemaisapp.widget.CinemaisTabLayout.Tab
import com.diegobezerra.cinemaisapp.widget.CinemaisTabLayoutMediator
import com.diegobezerra.cinemaisapp.widget.SpaceItemDecoration
import com.diegobezerra.shared.result.EventObserver
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayingCinemasFragment : Fragment() {

    private lateinit var binding: FragmentPlayingCinemasBinding

    private val playingCinemasViewModel: PlayingCinemasViewModel by viewModels()
    private val playingCinemasAdapter by lazy { PlayingCinemasAdapter(playingCinemasViewModel) }

    private val filtersAdapter by lazy { ScheduleFiltersAdapter(playingCinemasViewModel) }

    private var behavior: BottomSheetBehavior<*>? = null

    private lateinit var mediator: CinemaisTabLayoutMediator
    private lateinit var scheduleAdapter: ScheduleAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPlayingCinemasBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@PlayingCinemasFragment
            viewModel = playingCinemasViewModel
        }

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

        mediator = CinemaisTabLayoutMediator(binding.tabs, binding.viewpager,
            object : CinemaisTabLayoutMediator.TabConfigurationStrategy {
                override fun onConfigureTab(tab: Tab, position: Int) {
                    tab.setText(scheduleAdapter.getPageTitle(position))
                }
            })

        playingCinemasViewModel.filters.observe(viewLifecycleOwner) {
            filtersAdapter.submitList(it)
        }

        playingCinemasViewModel.cinemas.observe(viewLifecycleOwner) {
            playingCinemasAdapter.data = it
            playingCinemasAdapter.notifyDataSetChanged()
        }

        playingCinemasViewModel.schedule.observe(viewLifecycleOwner) { schedule ->
            if (::scheduleAdapter.isInitialized) {
                mediator.detach()
            }
            scheduleAdapter =
                ScheduleAdapter(this, schedule, true)
            binding.viewpager.adapter = scheduleAdapter
            mediator.attach()
        }

        playingCinemasViewModel.state.observe(viewLifecycleOwner) {
            // Make sure our views are visible just in case the last onSlide
            // set them to invisible.
            interpolateChildViews(1f)
            updateViews(it)
        }

        playingCinemasViewModel.toggleSheetAction.observe(viewLifecycleOwner,
            EventObserver {
                toggleSheet()
            })

        return binding.root
    }

    @Suppress("DEPRECATION")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // NOTE: Keeping here because it throws an IllegalStateException
        // if we move it to onViewCreated.
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
        if (binding.scheduleContainer.visibility != View.GONE) {
            binding.scheduleContainer.alpha = offset
            binding.filterButton.alpha = offset
        }
        if (binding.playingCinemas.visibility != View.GONE) {
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
