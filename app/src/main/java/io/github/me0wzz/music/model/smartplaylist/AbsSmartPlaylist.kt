package io.github.me0wzz.music.model.smartplaylist

import androidx.annotation.DrawableRes
import io.github.me0wzz.music.R
import io.github.me0wzz.music.model.AbsCustomPlaylist

abstract class AbsSmartPlaylist(
    name: String,
    @DrawableRes val iconRes: Int = R.drawable.ic_queue_music
) : AbsCustomPlaylist(
    id = PlaylistIdGenerator(name, iconRes),
    name = name
)