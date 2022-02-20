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
package io.github.me0wzz.music.activities

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.contains
import androidx.navigation.ui.setupWithNavController
import io.github.me0wzz.music.*
import io.github.me0wzz.music.activities.base.AbsSlidingMusicPanelActivity
import io.github.me0wzz.music.databinding.SlidingMusicPanelLayoutBinding
import io.github.me0wzz.music.extensions.*
import io.github.me0wzz.music.helper.MusicPlayerRemote
import io.github.me0wzz.music.helper.SearchQueryHelper.getSongs
import io.github.me0wzz.music.interfaces.IScrollHelper
import io.github.me0wzz.music.model.CategoryInfo
import io.github.me0wzz.music.model.Song
import io.github.me0wzz.music.repository.PlaylistSongsLoader
import io.github.me0wzz.music.service.MusicService
import io.github.me0wzz.music.util.AppRater
import io.github.me0wzz.music.util.NavigationUtil
import io.github.me0wzz.music.util.PreferenceUtil
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get

class MainActivity : AbsSlidingMusicPanelActivity(), OnSharedPreferenceChangeListener {
    companion object {
        const val TAG = "MainActivity"
        const val EXPAND_PANEL = "expand_panel"
    }

    override fun createContentView(): SlidingMusicPanelLayoutBinding {
        return wrapSlidingMusicPanel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTaskDescriptionColorAuto()
        hideStatusBar()
        updateTabs()
        AppRater.appLaunched(this)

        setupNavigationController()
        if (!hasPermissions()) {
            findNavController(R.id.fragment_container).navigate(R.id.permissionFragment)
        }
        if (BuildConfig.VERSION_CODE > PreferenceUtil.lastVersion && !BuildConfig.DEBUG) {
            NavigationUtil.gotoWhatNews(this)
        }
    }

    private fun setupNavigationController() {
        val navController = findNavController(R.id.fragment_container)
        val navInflater = navController.navInflater
        val navGraph = navInflater.inflate(R.navigation.main_graph)

        val categoryInfo: CategoryInfo = PreferenceUtil.libraryCategory.first { it.visible }
        if (categoryInfo.visible) {
            if (!navGraph.contains(PreferenceUtil.lastTab)) PreferenceUtil.lastTab =
                categoryInfo.category.id
            navGraph.setStartDestination(
                if (PreferenceUtil.rememberLastTab) {
                    PreferenceUtil.lastTab.let {
                        if (it == 0) {
                            categoryInfo.category.id
                        } else {
                            it
                        }
                    }
                } else categoryInfo.category.id
            )
        }
        navController.graph = navGraph
        bottomNavigationView.setupWithNavController(navController)
        // Scroll Fragment to top
        bottomNavigationView.setOnItemReselectedListener {
            currentFragment(R.id.fragment_container).apply {
                if (this is IScrollHelper) {
                    scrollToTop()
                }
            }
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == navGraph.startDestinationId) {
                currentFragment(R.id.fragment_container)?.enterTransition = null
            }
            when (destination.id) {
                R.id.action_home, R.id.action_song, R.id.action_album, R.id.action_artist, R.id.action_folder, R.id.action_playlist, R.id.action_genre, R.id.action_search -> {
                    // Save the last tab
                    if (PreferenceUtil.rememberLastTab) {
                        saveTab(destination.id)
                    }
                    // Show Bottom Navigation Bar
                    setBottomNavVisibility(visible = true, animate = true)
                }
                R.id.playing_queue_fragment -> {
                    setBottomNavVisibility(visible = false, hideBottomSheet = true)
                }
                else -> setBottomNavVisibility(
                    visible = false,
                    animate = true
                ) // Hide Bottom Navigation Bar
            }
        }
    }

    private fun saveTab(id: Int) {
        PreferenceUtil.lastTab = id
    }

    override fun onSupportNavigateUp(): Boolean =
        findNavController(R.id.fragment_container).navigateUp()

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        PreferenceUtil.registerOnSharedPreferenceChangedListener(this)
        val expand = intent?.extra<Boolean>(EXPAND_PANEL)?.value ?: false
        if (expand && PreferenceUtil.isExpandPanel) {
            fromNotification = true
            slidingPanel.bringToFront()
            expandPanel()
            intent?.removeExtra(EXPAND_PANEL)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceUtil.unregisterOnSharedPreferenceChangedListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == GENERAL_THEME || key == MATERIAL_YOU || key == WALLPAPER_ACCENT || key == BLACK_THEME || key == ADAPTIVE_COLOR_APP || key == USER_NAME || key == TOGGLE_FULL_SCREEN || key == TOGGLE_VOLUME || key == ROUND_CORNERS || key == CAROUSEL_EFFECT || key == NOW_PLAYING_SCREEN_ID || key == TOGGLE_GENRE || key == BANNER_IMAGE_PATH || key == PROFILE_IMAGE_PATH || key == CIRCULAR_ALBUM_ART || key == KEEP_SCREEN_ON || key == TOGGLE_SEPARATE_LINE || key == TOGGLE_HOME_BANNER || key == TOGGLE_ADD_CONTROLS || key == ALBUM_COVER_STYLE || key == HOME_ARTIST_GRID_STYLE || key == ALBUM_COVER_TRANSFORM || key == DESATURATED_COLOR || key == EXTRA_SONG_INFO || key == TAB_TEXT_MODE || key == LANGUAGE_NAME || key == LIBRARY_CATEGORIES || key == CUSTOM_FONT || key == APPBAR_MODE || key == CIRCLE_PLAY_BUTTON) {
            postRecreate()
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        intent ?: return
        handlePlaybackIntent(intent)
    }

    private fun handlePlaybackIntent(intent: Intent) {
        lifecycleScope.launch(IO) {
            val uri: Uri? = intent.data
            val mimeType: String? = intent.type
            var handled = false
            if (intent.action != null &&
                intent.action == MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH
            ) {
                val songs: List<Song> = getSongs(intent.extras!!)
                if (MusicPlayerRemote.shuffleMode == MusicService.SHUFFLE_MODE_SHUFFLE) {
                    MusicPlayerRemote.openAndShuffleQueue(songs, true)
                } else {
                    MusicPlayerRemote.openQueue(songs, 0, true)
                }
                handled = true
            }
            if (uri != null && uri.toString().isNotEmpty()) {
                MusicPlayerRemote.playFromUri(uri)
                handled = true
            } else if (MediaStore.Audio.Playlists.CONTENT_TYPE == mimeType) {
                val id = parseLongFromIntent(intent, "playlistId", "playlist")
                if (id >= 0L) {
                    val position: Int = intent.getIntExtra("position", 0)
                    val songs: List<Song> = PlaylistSongsLoader.getPlaylistSongList(get(), id)
                    MusicPlayerRemote.openQueue(songs, position, true)
                    handled = true
                }
            } else if (MediaStore.Audio.Albums.CONTENT_TYPE == mimeType) {
                val id = parseLongFromIntent(intent, "albumId", "album")
                if (id >= 0L) {
                    val position: Int = intent.getIntExtra("position", 0)
                    val songs = libraryViewModel.albumById(id).songs
                    MusicPlayerRemote.openQueue(
                        songs,
                        position,
                        true
                    )
                    handled = true
                }
            } else if (MediaStore.Audio.Artists.CONTENT_TYPE == mimeType) {
                val id = parseLongFromIntent(intent, "artistId", "artist")
                if (id >= 0L) {
                    val position: Int = intent.getIntExtra("position", 0)
                    val songs: List<Song> = libraryViewModel.artistById(id).songs
                    MusicPlayerRemote.openQueue(
                        songs,
                        position,
                        true
                    )
                    handled = true
                }
            }
            if (handled) {
                setIntent(Intent())
            }
        }
    }

    private fun parseLongFromIntent(
        intent: Intent,
        longKey: String,
        stringKey: String
    ): Long {
        var id = intent.getLongExtra(longKey, -1)
        if (id < 0) {
            val idString = intent.getStringExtra(stringKey)
            if (idString != null) {
                try {
                    id = idString.toLong()
                } catch (e: NumberFormatException) {
                    println(e.message)
                }
            }
        }
        return id
    }
}
