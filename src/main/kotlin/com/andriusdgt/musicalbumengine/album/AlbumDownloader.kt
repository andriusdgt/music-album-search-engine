package com.andriusdgt.musicalbumengine.album

interface AlbumDownloader {

    suspend fun downloadTopFive(artistAmgId: Long): List<Album>

}
