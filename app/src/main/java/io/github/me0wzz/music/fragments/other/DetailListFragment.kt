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
package io.github.me0wzz.music.fragments.other

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import io.github.me0wzz.music.*
import io.github.me0wzz.music.adapter.album.AlbumAdapter
import io.github.me0wzz.music.adapter.artist.ArtistAdapter
import io.github.me0wzz.music.adapter.song.ShuffleButtonSongAdapter
import io.github.me0wzz.music.adapter.song.SongAdapter
import io.github.me0wzz.music.databinding.FragmentPlaylistDetailBinding
import io.github.me0wzz.music.db.toSong
import io.github.me0wzz.music.extensions.dipToPix
import io.github.me0wzz.music.extensions.surfaceColor
import io.github.me0wzz.music.fragments.base.AbsMainActivityFragment
import io.github.me0wzz.music.interfaces.IAlbumClickListener
import io.github.me0wzz.music.interfaces.IArtistClickListener
import io.github.me0wzz.music.interfaces.ICabCallback
import io.github.me0wzz.music.interfaces.ICabHolder
import io.github.me0wzz.music.model.Album
import io.github.me0wzz.music.model.Artist
import io.github.me0wzz.music.util.RetroColorUtil
import io.github.me0wzz.music.util.RetroUtil
import com.afollestad.materialcab.attached.AttachedCab
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.afollestad.materialcab.createCab
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis


class DetailListFragment : AbsMainActivityFragment(R.layout.fragment_playlist_detail),
    IArtistClickListener, IAlbumClickListener, ICabHolder {
    private val args by navArgs<DetailListFragmentArgs>()
    private var _binding: FragmentPlaylistDetailBinding? = null
    private val binding get() = _binding!!
    private var showClearHistoryOption = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlaylistDetailBinding.bind(view)
        when (args.type) {
            TOP_ARTISTS,
            RECENT_ARTISTS,
            TOP_ALBUMS,
            RECENT_ALBUMS,
            FAVOURITES -> {
                enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
                returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
            }
            else -> {
                enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
                returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
            }
        }
        mainActivity.setSupportActionBar(binding.toolbar)
        binding.progressIndicator.hide()
        when (args.type) {
            TOP_ARTISTS -> loadArtists(R.string.top_artists, TOP_ARTISTS)
            RECENT_ARTISTS -> loadArtists(R.string.recent_artists, RECENT_ARTISTS)
            TOP_ALBUMS -> loadAlbums(R.string.top_albums, TOP_ALBUMS)
            RECENT_ALBUMS -> loadAlbums(R.string.recent_albums, RECENT_ALBUMS)
            FAVOURITES -> loadFavorite()
            HISTORY_PLAYLIST -> {
                loadHistory()
                showClearHistoryOption = true // Reference to onCreateOptionsMenu
            }
            LAST_ADDED_PLAYLIST -> lastAddedSongs()
            TOP_PLAYED_PLAYLIST -> topPlayed()
        }

        binding.recyclerView.adapter?.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                val height = dipToPix(52f)
                binding.recyclerView.updatePadding(bottom = height.toInt())
            }
        })
        binding.appBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(requireContext())
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (!handleBackPress()) {
                remove()
                findNavController().navigateUp()
            }
        }
    }

    private fun lastAddedSongs() {
        binding.toolbar.setTitle(R.string.last_added)
        val songAdapter = ShuffleButtonSongAdapter(
            requireActivity(),
            mutableListOf(),
            R.layout.item_list, this
        )
        binding.recyclerView.apply {
            adapter = songAdapter
            layoutManager = linearLayoutManager()
            scheduleLayoutAnimation()
        }
        libraryViewModel.recentSongs().observe(viewLifecycleOwner) { songs ->
            songAdapter.swapDataSet(songs)
        }
    }

    private fun topPlayed() {
        binding.toolbar.setTitle(R.string.my_top_tracks)
        val songAdapter = ShuffleButtonSongAdapter(
            requireActivity(),
            mutableListOf(),
            R.layout.item_list, this
        )
        binding.recyclerView.apply {
            adapter = songAdapter
            layoutManager = linearLayoutManager()
        }
        libraryViewModel.playCountSongs().observe(viewLifecycleOwner) { songs ->
            songAdapter.swapDataSet(songs)
        }
    }

    private fun loadHistory() {
        binding.toolbar.setTitle(R.string.history)

        val songAdapter = ShuffleButtonSongAdapter(
            requireActivity(),
            mutableListOf(),
            R.layout.item_list, this
        )
        binding.recyclerView.apply {
            adapter = songAdapter
            layoutManager = linearLayoutManager()
        }

        libraryViewModel.observableHistorySongs().observe(viewLifecycleOwner) {
            songAdapter.swapDataSet(it)
            binding.empty.isVisible = it.isEmpty()
        }

    }

    private fun loadFavorite() {
        binding.toolbar.setTitle(R.string.favorites)
        val songAdapter = SongAdapter(
            requireActivity(),
            mutableListOf(),
            R.layout.item_list, this
        )
        binding.recyclerView.apply {
            adapter = songAdapter
            layoutManager = linearLayoutManager()
        }
        libraryViewModel.favorites().observe(viewLifecycleOwner) { songEntities ->
            val songs = songEntities.map { songEntity -> songEntity.toSong() }
            songAdapter.swapDataSet(songs)
        }
    }


    private fun loadArtists(title: Int, type: Int) {
        binding.toolbar.setTitle(title)
        val artistAdapter = artistAdapter(listOf())
        binding.recyclerView.apply {
            adapter = artistAdapter
            layoutManager = gridLayoutManager()
        }
        libraryViewModel.artists(type).observe(viewLifecycleOwner) { artists ->
            artistAdapter.swapDataSet(artists)
        }
    }

    private fun loadAlbums(title: Int, type: Int) {
        binding.toolbar.setTitle(title)
        val albumAdapter = albumAdapter(listOf())
        binding.recyclerView.apply {
            adapter = albumAdapter
            layoutManager = gridLayoutManager()
        }
        libraryViewModel.albums(type).observe(viewLifecycleOwner) { albums ->
            albumAdapter.swapDataSet(albums)
        }
    }

    private fun artistAdapter(artists: List<Artist>): ArtistAdapter = ArtistAdapter(
        requireActivity(),
        artists,
        R.layout.item_grid_circle,
        this, this@DetailListFragment
    )

    private fun albumAdapter(albums: List<Album>): AlbumAdapter = AlbumAdapter(
        requireActivity(),
        albums,
        R.layout.item_grid,
        this, this@DetailListFragment
    )

    private fun linearLayoutManager(): LinearLayoutManager =
        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

    private fun gridLayoutManager(): GridLayoutManager =
        GridLayoutManager(requireContext(), gridCount(), GridLayoutManager.VERTICAL, false)

    private fun gridCount(): Int {
        if (RetroUtil.isTablet()) {
            return if (RetroUtil.isLandscape()) 6 else 4
        }
        return if (RetroUtil.isLandscape()) 4 else 2
    }


    override fun onArtist(artistId: Long, view: View) {
        findNavController().navigate(
            R.id.artistDetailsFragment,
            bundleOf(EXTRA_ARTIST_ID to artistId),
            null,
            FragmentNavigatorExtras(view to artistId.toString())
        )
    }

    override fun onAlbumClick(albumId: Long, view: View) {
        findNavController().navigate(
            R.id.albumDetailsFragment,
            bundleOf(EXTRA_ALBUM_ID to albumId),
            null,
            FragmentNavigatorExtras(
                view to albumId.toString()
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private var cab: AttachedCab? = null

    private fun handleBackPress(): Boolean {
        cab?.let {
            if (it.isActive()) {
                it.destroy()
                return true
            }
        }
        return false
    }

    override fun openCab(menuRes: Int, callback: ICabCallback): AttachedCab {
        cab?.let {
            println("Cab")
            if (it.isActive()) {
                it.destroy()
            }
        }
        cab = createCab(R.id.toolbar_container) {
            menu(menuRes)
            closeDrawable(R.drawable.ic_close)
            backgroundColor(literal = RetroColorUtil.shiftBackgroundColor(surfaceColor()))
            slideDown()
            onCreate { cab, menu -> callback.onCabCreated(cab, menu) }
            onSelection {
                callback.onCabItemClicked(it)
            }
            onDestroy { callback.onCabFinished(it) }
        }
        return cab as AttachedCab
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_clear_history, menu)
        if (showClearHistoryOption) {
            menu.findItem(R.id.action_clear_history).isVisible = true // Show Clear History option
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_clear_history -> {
                if (binding.recyclerView.adapter?.itemCount!! > 0) {
                    libraryViewModel.clearHistory()

                    val snackBar =
                        Snackbar.make(binding.container,
                            getString(R.string.history_cleared),
                            Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.history_undo_button)) {
                                libraryViewModel.restoreHistory()
                            }
                            .setActionTextColor(Color.YELLOW)
                    val snackBarView = snackBar.view
                    snackBarView.translationY =
                        -(resources.getDimension(R.dimen.mini_player_height))
                    snackBar.show()
                }
            }
        }
        return false
    }
}
