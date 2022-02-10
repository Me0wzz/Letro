package io.github.me0wzz.music.model

import org.jaudiotagger.tag.FieldKey

class AudioTagInfo(
    val filePaths: List<String>?,
    val fieldKeyValueMap: Map<FieldKey, String>?,
    val artworkInfo: ArtworkInfo?
)