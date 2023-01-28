package com.andriusdgt.musicalbumengine.artist

import com.andriusdgt.musicalbumengine.album.Album
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.persistence.*

@Entity
@Table(name = "artist", indexes = [Index(columnList = "amgId")])
@Serializable
data class Artist(

    @Id
    @kotlinx.serialization.Transient
    val id: Long = 0,

    val amgId: Long,

    val name: String,

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "artist", fetch = FetchType.EAGER)
    @kotlinx.serialization.Transient
    val topAlbums: List<Album> = listOf(),

    @kotlinx.serialization.Transient
    val lastUpdated: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)

)
