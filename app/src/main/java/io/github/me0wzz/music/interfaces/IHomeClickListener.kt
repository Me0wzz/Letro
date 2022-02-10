package io.github.me0wzz.music.interfaces

import io.github.me0wzz.music.model.Album
import io.github.me0wzz.music.model.Artist
import io.github.me0wzz.music.model.Genre

interface IHomeClickListener {
    fun onAlbumClick(album: Album)

    fun onArtistClick(artist: Artist)

    fun onGenreClick(genre: Genre)
}