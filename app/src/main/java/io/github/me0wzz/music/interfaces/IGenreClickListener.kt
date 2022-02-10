package io.github.me0wzz.music.interfaces

import android.view.View
import io.github.me0wzz.music.model.Genre

interface IGenreClickListener {
    fun onClickGenre(genre: Genre, view: View)
}