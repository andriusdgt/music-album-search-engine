package com.andriusdgt.musicalbumengine.album

import com.andriusdgt.musicalbumengine.HttpClientWrapper
import com.andriusdgt.musicalbumengine.artist.Artist
import com.andriusdgt.musicalbumengine.artist.ITunesArtist
import com.andriusdgt.musicalbumengine.TooManyRequestsException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service
import java.util.stream.Collectors.toList
import java.util.stream.StreamSupport

@Service
class ITunesAlbumDownloader(
    private val httpClientWrapper: HttpClientWrapper,
    private val objectMapper: ObjectMapper
) : AlbumDownloader {

    private val expectedArtistCount = 1
    private val expectedAlbumCount = 5

    @Throws(TooManyRequestsException::class)
    override suspend fun downloadTopFive(artistAmgId: Long): List<Album> {
        val responseJsonTree =
            httpClientWrapper.doGet("https://itunes.apple.com/lookup?amgArtistId=${artistAmgId}&entity=album&limit=5")
        val jsonArray = StreamSupport.stream(responseJsonTree.get("results").spliterator(), false).collect(toList())
        return if (containsAlbums(jsonArray.size)) {
            jsonArray
                .subList(1, countAlbums(jsonArray.size) + 1)
                .map { it.toAlbum(jsonArray[0].toArtist()) }
        } else emptyList()
    }

    private fun containsAlbums(elementCount: Int) = elementCount > expectedArtistCount

    private fun countAlbums(elementCount: Int) =
        if (elementCount <= expectedAlbumCount + expectedArtistCount)
            elementCount - expectedArtistCount
        else
            expectedAlbumCount

    private fun JsonNode.toArtist(): Artist =
        objectMapper.readValue<ITunesArtist>(this.toString()).toArtist()

    private fun JsonNode.toAlbum(artist: Artist): Album =
        objectMapper.readValue<ITunesAlbum>(this.toString()).toAlbum(artist)

    private fun ITunesArtist.toArtist(): Artist =
        Artist(this.artistId, this.amgArtistId, this.artistName)

    private fun ITunesAlbum.toAlbum(artist: Artist): Album =
        Album(this.collectionId, this.collectionName, artist)

}
