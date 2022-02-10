package io.github.me0wzz.music.glide.playlistPreview

import io.github.me0wzz.music.db.PlaylistEntity
import io.github.me0wzz.music.db.PlaylistWithSongs
import io.github.me0wzz.music.db.toSongs
import io.github.me0wzz.music.model.Song

class PlaylistPreview(val playlistWithSongs: PlaylistWithSongs) {

    val playlistEntity: PlaylistEntity get() = playlistWithSongs.playlistEntity
    val songs: List<Song> get() = playlistWithSongs.songs.toSongs()

    override fun equals(other: Any?): Boolean {
        if (other is PlaylistPreview) {
            if (other.playlistEntity.playListId != playlistEntity.playListId) {
                return false
            }
            if (other.songs.size != songs.size) {
                return false
            }
            return true
        }
        return false
    }

    override fun hashCode(): Int {
        var result = playlistEntity.playListId.hashCode()
        result = 31 * result + playlistWithSongs.songs.size
        return result
    }
}