package com.andriusdgt.musicalbumengine.artist

import com.andriusdgt.musicalbumengine.HttpClientWrapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import java.time.LocalDateTime
import java.time.ZoneOffset

@ExtendWith(MockitoExtension::class)
class ITunesArtistDownloaderTest {

    @Mock
    private lateinit var httpClientWrapper: HttpClientWrapper

    lateinit var downloader: ITunesArtistDownloader

    @BeforeEach
    fun setUp() {
        downloader = ITunesArtistDownloader(httpClientWrapper, jacksonObjectMapper())
    }

    @Test
    fun `returns empty list on empty response`(): Unit = runBlocking {
        val responseJson = """{"resultCount":0,"results":[]}"""
        val expectedArtistName = "queen"

        `when`(httpClientWrapper.doGet(any())).thenReturn(jacksonObjectMapper().readTree(responseJson))

        val actualArtists = downloader.download(expectedArtistName)

        assertThat(actualArtists).isEmpty()
        verify(httpClientWrapper)
            .doGet("https://itunes.apple.com/search?entity=allArtist&term=$expectedArtistName")
    }

    @Test
    fun `returns artists`(): Unit = runBlocking {
        val expectedArtists = listOf(Artist(11, 110, "ABBA"), Artist(12, 111, "ABBA-DJ"))

        `when`(httpClientWrapper.doGet(any())).thenReturn(jacksonObjectMapper().readTree(createArtistsResponseJson()))

        val actualArtists = downloader.download("abba")

        assertThat(actualArtists)
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("lastUpdated")
            .isEqualTo(expectedArtists)
        assertThat(actualArtists)
            .extracting("lastUpdated", LocalDateTime::class.java)
            .allMatch { it.isAfter(LocalDateTime.now(ZoneOffset.UTC).minusSeconds(2)) }
    }

    private fun createArtistsResponseJson() = """{"resultCount":2,"results":[
        {"wrapperType":"artist", "artistType":"Artist", "artistName":"ABBA", "artistLinkUrl":"https://music.apple.com/us/artist/abba/372976?uo=4", "artistId":11, "amgArtistId":110, "primaryGenreName":"Pop", "primaryGenreId":14}, 
        {"wrapperType":"artist", "artistType":"Artist", "artistName":"ABBA-DJ", "artistLinkUrl":"https://music.apple.com/us/artist/abba-dj/105311823?uo=4", "artistId":12, "amgArtistId":111, "primaryGenreName":"Dance", "primaryGenreId":17}
    ]}
    """

}
