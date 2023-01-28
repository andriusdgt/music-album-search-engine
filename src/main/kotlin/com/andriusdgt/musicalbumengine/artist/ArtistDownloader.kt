package com.andriusdgt.musicalbumengine.artist

interface ArtistDownloader {

    suspend fun download(name: String): List<Artist>

}
