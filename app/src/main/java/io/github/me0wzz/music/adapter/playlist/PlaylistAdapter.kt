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
package io.github.me0wzz.music.adapter.playlist

import android.graphics.Color
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.setPadding
import androidx.fragment.app.FragmentActivity
import io.github.me0wzz.music.R
import io.github.me0wzz.music.adapter.base.AbsMultiSelectAdapter
import io.github.me0wzz.music.adapter.base.MediaEntryViewHolder
import io.github.me0wzz.music.db.PlaylistEntity
import io.github.me0wzz.music.db.PlaylistWithSongs
import io.github.me0wzz.music.db.toSongs
import io.github.me0wzz.music.extensions.dipToPix
import io.github.me0wzz.music.extensions.hide
import io.github.me0wzz.music.extensions.show
import io.github.me0wzz.music.glide.GlideApp
import io.github.me0wzz.music.glide.playlistPreview.PlaylistPreview
import io.github.me0wzz.music.helper.SortOrder.PlaylistSortOrder
import io.github.me0wzz.music.helper.menu.PlaylistMenuHelper
import io.github.me0wzz.music.helper.menu.SongsMenuHelper
import io.github.me0wzz.music.interfaces.ICabHolder
import io.github.me0wzz.music.interfaces.IPlaylistClickListener
import io.github.me0wzz.music.model.Song
import io.github.me0wzz.music.util.MusicUtil
import io.github.me0wzz.music.util.PreferenceUtil
import me.zhanghai.android.fastscroll.PopupTextProvider

class PlaylistAdapter(
    override val activity: FragmentActivity,
    var dataSet: List<PlaylistWithSongs>,
    private var itemLayoutRes: Int,
    ICabHolder: ICabHolder?,
    private val listener: IPlaylistClickListener
) : AbsMultiSelectAdapter<PlaylistAdapter.ViewHolder, PlaylistWithSongs>(
    activity,
    ICabHolder,
    R.menu.menu_playlists_selection
), PopupTextProvider {

    init {
        setHasStableIds(true)
    }

    fun swapDataSet(dataSet: List<PlaylistWithSongs>) {
        this.dataSet = dataSet
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].playlistEntity.playListId
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false)
        return createViewHolder(view)
    }

    fun createViewHolder(view: View): ViewHolder {
        return ViewHolder(view)
    }

    private fun getPlaylistTitle(playlist: PlaylistEntity): String {
        return if (TextUtils.isEmpty(playlist.playlistName)) "-" else playlist.playlistName
    }

    private fun getPlaylistText(playlist: PlaylistWithSongs): String {
        return MusicUtil.getPlaylistInfoString(activity, playlist.songs.toSongs())
    }

    override fun getPopupText(position: Int): String {
        val sectionName: String = when (PreferenceUtil.playlistSortOrder) {
            PlaylistSortOrder.PLAYLIST_A_Z, PlaylistSortOrder.PLAYLIST_Z_A -> dataSet[position].playlistEntity.playlistName
            PlaylistSortOrder.PLAYLIST_SONG_COUNT, PlaylistSortOrder.PLAYLIST_SONG_COUNT_DESC -> dataSet[position].songs.size.toString()
            else -> {
                return ""
            }
        }
        return MusicUtil.getSectionName(sectionName)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = dataSet[position]
        holder.itemView.isActivated = isChecked(playlist)
        holder.title?.text = getPlaylistTitle(playlist.playlistEntity)
        holder.text?.text = getPlaylistText(playlist)
        val isChecked = isChecked(playlist)
        if (isChecked) {
            holder.menu?.hide()
        } else {
            holder.menu?.show()
        }
        GlideApp.with(activity)
            .load(
                if (itemLayoutRes == R.layout.item_list) {
                    holder.image?.setPadding(activity.dipToPix(8F).toInt())
                    R.drawable.ic_playlist_play
                } else PlaylistPreview(playlist)
            )
            .playlistOptions()
            .into(holder.image!!)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getIdentifier(position: Int): PlaylistWithSongs {
        return dataSet[position]
    }

    override fun getName(playlist: PlaylistWithSongs): String {
        return playlist.playlistEntity.playlistName
    }

    override fun onMultipleItemAction(menuItem: MenuItem, selection: List<PlaylistWithSongs>) {
        when (menuItem.itemId) {
            else -> SongsMenuHelper.handleMenuClick(
                activity,
                getSongList(selection),
                menuItem.itemId
            )
        }
    }

    private fun getSongList(playlists: List<PlaylistWithSongs>): List<Song> {
        val songs = mutableListOf<Song>()
        playlists.forEach {
            songs.addAll(it.songs.toSongs())
        }
        return songs
    }

    inner class ViewHolder(itemView: View) : MediaEntryViewHolder(itemView) {
        init {
            menu?.setOnClickListener { view ->
                val popupMenu = PopupMenu(activity, view)
                popupMenu.inflate(R.menu.menu_item_playlist)
                popupMenu.setOnMenuItemClickListener { item ->
                    PlaylistMenuHelper.handleMenuClick(activity, dataSet[layoutPosition], item)
                }
                popupMenu.show()
            }

            imageTextContainer?.apply {
                cardElevation = 0f
                setCardBackgroundColor(Color.TRANSPARENT)
            }
        }

        override fun onClick(v: View?) {
            if (isInQuickSelectMode) {
                toggleChecked(layoutPosition)
            } else {
                ViewCompat.setTransitionName(itemView, "playlist")
                listener.onPlaylistClick(dataSet[layoutPosition], itemView)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            toggleChecked(layoutPosition)
            return true
        }
    }

    companion object {
        val TAG: String = PlaylistAdapter::class.java.simpleName
    }
}
