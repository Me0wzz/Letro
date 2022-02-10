package io.github.me0wzz.music.extensions

import io.github.me0wzz.music.model.Song
import io.github.me0wzz.music.util.MusicUtil

val Song.uri get() = MusicUtil.getSongFileUri(songId = id)