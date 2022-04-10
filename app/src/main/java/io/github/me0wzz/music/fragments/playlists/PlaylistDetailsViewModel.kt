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
package io.github.me0wzz.music.fragments.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.me0wzz.music.db.PlaylistWithSongs
import io.github.me0wzz.music.db.SongEntity
import io.github.me0wzz.music.interfaces.IMusicServiceEventListener
import io.github.me0wzz.music.model.Song
import io.github.me0wzz.music.repository.RealRepository
import io.github.me0wzz.music.repository.RealRoomRepository

class PlaylistDetailsViewModel(
    private val realRepository: RealRepository,
    private var playlist: PlaylistWithSongs
) : ViewModel() {
    fun getSongs(): LiveData<List<SongEntity>> =
        realRepository.playlistSongs(playlist.playlistEntity.playListId)

    fun playlistExists(): LiveData<Boolean> =
        realRepository.checkPlaylistExists(playlist.playlistEntity.playListId)
}
