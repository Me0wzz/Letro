package io.github.me0wzz.music.interfaces

import android.view.View
import io.github.me0wzz.music.db.PlaylistWithSongs

interface IPlaylistClickListener {
    fun onPlaylistClick(playlistWithSongs: PlaylistWithSongs, view: View)
}