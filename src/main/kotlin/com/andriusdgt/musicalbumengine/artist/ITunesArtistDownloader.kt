package com.andriusdgt.musicalbumengine.artist

import com.andriusdgt.musicalbumengine.HttpClientWrapper
import com.andriusdgt.musicalbumengine.TooManyRequestsException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service

@Service
class ITunesArtistDownloader(
    private val httpClientWrapper: HttpClientWrapper,
    private val objectMapper: ObjectMapper
) : ArtistDownloader {

    @Throws(TooManyRequestsException::class)
    override suspend fun download(name: String): List<Artist> {
        val responseJsonTree = httpClientWrapper.doGet("https://itunes.apple.com/search?entity=allArtist&term=$name")
        val artistsJson = responseJsonTree.get("results").toString()
        return objectMapper.readValue<List<ITunesArtist>>(artistsJson)
            .map { Artist(it.artistId, it.amgArtistId, it.artistName) }
    }

}
