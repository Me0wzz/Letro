/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package io.github.me0wzz.music.fragments.base

import android.os.Bundle
import android.view.*
import androidx.annotation.NonNull
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import io.github.me0wzz.appthemehelper.common.ATHToolbarActivity
import io.github.me0wzz.appthemehelper.util.ToolbarContentTintHelper
import io.github.me0wzz.music.R
import io.github.me0wzz.music.databinding.FragmentMainRecyclerBinding
import io.github.me0wzz.music.dialogs.CreatePlaylistDialog
import io.github.me0wzz.music.dialogs.ImportPlaylistDialog
import io.github.me0wzz.music.extensions.accentColor
import io.github.me0wzz.music.extensions.dip
import io.github.me0wzz.music.helper.MusicPlayerRemote
import io.github.me0wzz.music.interfaces.IScrollHelper
import io.github.me0wzz.music.util.PreferenceUtil
import io.github.me0wzz.music.util.ThemedFastScroller.create
import com.google.android.material.transition.MaterialFadeThrough
import me.zhanghai.android.fastscroll.FastScroller
import me.zhanghai.android.fastscroll.FastScrollerBuilder

abstract class AbsRecyclerViewFragment<A : RecyclerView.Adapter<*>, LM : RecyclerView.LayoutManager> :
    AbsMainActivityFragment(R.layout.fragment_main_recycler), IScrollHelper {

    private var _binding: FragmentMainRecyclerBinding? = null
    private val binding get() = _binding!!
    protected var adapter: A? = null
    protected var layoutManager: LM? = null
    val shuffleButton get() = binding.shuffleButton
    abstract val isShuffleVisible: Boolean

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMainRecyclerBinding.bind(view)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        enterTransition = MaterialFadeThrough().addTarget(binding.recyclerView)
        reenterTransition = MaterialFadeThrough().addTarget(binding.recyclerView)
        mainActivity.setSupportActionBar(toolbar)
        mainActivity.supportActionBar?.title = null
        initLayoutManager()
        initAdapter()
        setUpRecyclerView()
        setupToolbar()
        binding.shuffleButton.fitsSystemWindows = PreferenceUtil.isFullScreenMode
        // Add listeners when shuffle is visible
        if (isShuffleVisible) {
            binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        binding.shuffleButton.hide()
                    } else if (dy < 0) {
                        binding.shuffleButton.show()
                    }

                }
            })
            binding.shuffleButton.apply {
                setOnClickListener {
                    onShuffleClicked()
                }
                accentColor()
            }
        } else {
            binding.shuffleButton.isVisible = false
        }
        libraryViewModel.getFabMargin().observe(viewLifecycleOwner) {
            binding.shuffleButton.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = it
            }
        }
    }

    open fun onShuffleClicked() {
    }

    val toolbar: Toolbar get() = binding.appBarLayout.toolbar

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            findNavController().navigate(
                R.id.action_search,
                null,
                navOptions
            )
        }
        val appName = resources.getString(titleRes)
        binding.appBarLayout.title = appName
    }

    abstract val titleRes: Int

    private fun setUpRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = this@AbsRecyclerViewFragment.layoutManager
            adapter = this@AbsRecyclerViewFragment.adapter
            create(this)
        }
        checkForPadding()
    }

    protected open fun createFastScroller(recyclerView: RecyclerView): FastScroller {
        return FastScrollerBuilder(recyclerView).useMd2Style().build()
    }

    private fun initAdapter() {
        adapter = createAdapter()
        adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkIsEmpty()
                checkForPadding()
            }
        })
    }

    protected open val emptyMessage: Int
        @StringRes get() = R.string.empty

    private fun getEmojiByUnicode(unicode: Int): String {
        return String(Character.toChars(unicode))
    }

    private fun checkIsEmpty() {
        binding.emptyText.setText(emptyMessage)
        binding.empty.isVisible = adapter!!.itemCount == 0
    }

    private fun checkForPadding() {
        val itemCount: Int = adapter?.itemCount ?: 0

        binding.recyclerView.updatePadding(
            bottom = if (itemCount > 0 && MusicPlayerRemote.playingQueue.isNotEmpty()) {
                dip(R.dimen.mini_player_height_expanded)
            } else {
                dip(R.dimen.bottom_nav_height)
            }
        )
    }

    private fun initLayoutManager() {
        layoutManager = createLayoutManager()
    }

    protected abstract fun createLayoutManager(): LM

    @NonNull
    protected abstract fun createAdapter(): A

    override fun onQueueChanged() {
        super.onQueueChanged()
        checkForPadding()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        checkForPadding()
    }

    protected fun invalidateLayoutManager() {
        initLayoutManager()
        binding.recyclerView.layoutManager = layoutManager
    }

    protected fun invalidateAdapter() {
        initAdapter()
        checkIsEmpty()
        binding.recyclerView.adapter = adapter
    }

    val recyclerView get() = binding.recyclerView

    val container get() = binding.root

    override fun scrollToTop() {
        recyclerView.scrollToPosition(0)
        binding.appBarLayout.setExpanded(true, true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        ToolbarContentTintHelper.handleOnPrepareOptionsMenu(requireActivity(), toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
        ToolbarContentTintHelper.handleOnCreateOptionsMenu(
            requireContext(),
            toolbar,
            menu,
            ATHToolbarActivity.getToolbarBackgroundColor(toolbar)
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> findNavController().navigate(
                R.id.settingsActivity,
                null,
                navOptions
            )
            R.id.action_import_playlist -> ImportPlaylistDialog().show(
                childFragmentManager,
                "ImportPlaylist"
            )
            R.id.action_add_to_playlist -> CreatePlaylistDialog.create(emptyList()).show(
                childFragmentManager,
                "ShowCreatePlaylistDialog"
            )
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
