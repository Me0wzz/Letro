package io.github.me0wzz.music.model.smartplaylist

import io.github.me0wzz.music.App
import io.github.me0wzz.music.R
import io.github.me0wzz.music.model.Song
import kotlinx.parcelize.Parcelize

@Parcelize
class ShuffleAllPlaylist : AbsSmartPlaylist(
    name = App.getContext().getString(R.string.action_shuffle_all),
    iconRes = R.drawable.ic_shuffle
) {
    override fun songs(): List<Song> {
        return songRepository.songs()
    }
}