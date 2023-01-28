package com.andriusdgt.musicalbumengine.album

import com.andriusdgt.musicalbumengine.artist.Artist
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.persistence.*

@Entity
@Table(name = "album")
@Serializable
data class Album(

    @Id
    val id: Long,

    val name: String,

    @ManyToOne
    @JoinColumn(name = "album_id")
    @kotlinx.serialization.Transient
    val artist: Artist? = null,

    @kotlinx.serialization.Transient
    val lastUpdated: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)

)
