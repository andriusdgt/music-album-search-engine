package com.andriusdgt.musicalbumengine.artist

import kotlinx.serialization.Serializable

@Serializable
data class ITunesArtist(

    val wrapperType: String,
    val artistType: String,
    val artistName: String,
    val artistLinkUrl: String?,
    val artistId: Long,
    val amgArtistId: Long,
    val primaryGenreName: String?,
    val primaryGenreId: Long?,

)
