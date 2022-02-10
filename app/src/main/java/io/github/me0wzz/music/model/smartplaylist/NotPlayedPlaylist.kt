package io.github.me0wzz.music.model.smartplaylist

import io.github.me0wzz.music.App
import io.github.me0wzz.music.R
import io.github.me0wzz.music.model.Song
import kotlinx.parcelize.Parcelize

@Parcelize
class NotPlayedPlaylist : AbsSmartPlaylist(
    name = App.getContext().getString(R.string.not_recently_played),
    iconRes = R.drawable.ic_watch_later
) {
    override fun songs(): List<Song> {
        return topPlayedRepository.notRecentlyPlayedTracks()
    }
}