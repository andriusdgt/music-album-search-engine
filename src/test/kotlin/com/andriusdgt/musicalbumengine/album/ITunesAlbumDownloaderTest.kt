package com.andriusdgt.musicalbumengine.album

import com.andriusdgt.musicalbumengine.HttpClientWrapper
import com.andriusdgt.musicalbumengine.artist.Artist
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import java.time.LocalDateTime
import java.time.ZoneOffset

@ExtendWith(MockitoExtension::class)
class ITunesAlbumDownloaderTest {

    @Mock
    private lateinit var httpClientWrapper: HttpClientWrapper

    lateinit var downloader: ITunesAlbumDownloader

    @BeforeEach
    fun setUp() {
        downloader = ITunesAlbumDownloader(httpClientWrapper, jacksonObjectMapper())
    }

    @Test
    fun `returns empty list on empty response`(): Unit = runBlocking {
        val responseJson = """{"resultCount":0,"results":[]}"""
        val expectedArtistAmgId: Long = 100

        `when`(httpClientWrapper.doGet(any())).thenReturn(jacksonObjectMapper().readTree(responseJson))

        val actualTopAlbums = downloader.downloadTopFive(expectedArtistAmgId)

        assertThat(actualTopAlbums).isEmpty()
        verify(httpClientWrapper)
            .doGet("https://itunes.apple.com/lookup?amgArtistId=${expectedArtistAmgId}&entity=album&limit=5")
    }

    @Test
    fun `returns empty list when no albums are produced`(): Unit = runBlocking {
        `when`(httpClientWrapper.doGet(any()))
            .thenReturn(jacksonObjectMapper().readTree(createNoneTopAlbumsResponseJson()))

        val actualAlbums = downloader.downloadTopFive(3)

        assertThat(actualAlbums).isEmpty()
    }

    @Test
    fun `returns parsed top 5 albums`(): Unit = runBlocking {
        val artist = Artist(1, 3, "ABBA")
        val expectedAlbums = listOf(
            Album(11, "Foo", artist),
            Album(12, "Bar", artist),
            Album(13, "Baz", artist),
            Album(14, "FooBar", artist),
            Album(15, "FooBaz", artist)
        )

        `when`(httpClientWrapper.doGet(any())).thenReturn(jacksonObjectMapper().readTree(createTopAlbumsResponseJson()))

        val actualAlbums = downloader.downloadTopFive(3)

        assertThat(actualAlbums)
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("artist", "lastUpdated")
            .isEqualTo(expectedAlbums)
        assertThat(actualAlbums.map { it.artist })
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("lastUpdated")
            .containsOnly(artist)
        assertThat(actualAlbums)
            .extracting("lastUpdated", LocalDateTime::class.java)
            .allMatch { it.isAfter(LocalDateTime.now(ZoneOffset.UTC).minusSeconds(2)) }
        assertThat(actualAlbums.map { it.artist })
            .extracting("lastUpdated", LocalDateTime::class.java)
            .allMatch { it.isAfter(LocalDateTime.now(ZoneOffset.UTC).minusSeconds(2)) }
    }

    @Test
    fun `returns as much top albums as API produces`(): Unit = runBlocking {
        val artist = Artist(1, 3, "ABBA")
        val expectedAlbums = listOf(Album(11, "Foo", artist))

        `when`(httpClientWrapper.doGet(any()))
            .thenReturn(jacksonObjectMapper().readTree(createSmallTopAlbumsResponseJson()))

        val actualAlbums = downloader.downloadTopFive(3)

        assertThat(actualAlbums)
            .usingRecursiveFieldByFieldElementComparatorIgnoringFields("artist", "lastUpdated")
            .isEqualTo(expectedAlbums)
    }

    @Test
    fun `returns no more than 5 albums`(): Unit = runBlocking {
        `when`(httpClientWrapper.doGet(any()))
            .thenReturn(jacksonObjectMapper().readTree(createMoreThanFiveTopAlbumsResponseJson()))

        val actualAlbums = downloader.downloadTopFive(3)

        assertThat(actualAlbums).hasSize(5)
    }

    private fun createTopAlbumsResponseJson() = """{"resultCount":6,
    "results": [
        {"wrapperType":"artist", "artistType":"Artist", "artistName":"ABBA", "artistLinkUrl":"example.com", "artistId":1, "amgArtistId":3, "primaryGenreName":"Pop", "primaryGenreId":14}, 
        {"wrapperType":"collection", "collectionType":"Album", "artistId":1, "collectionId":11, "amgArtistId":3, "artistName":"Benny", "collectionName":"Foo", "collectionCensoredName":"Foo", "artistViewUrl":"example.com", "collectionViewUrl":"example.com", "artworkUrl60":"example.com", "artworkUrl100":"example.com", "collectionPrice":9.99, "collectionExplicitness":"notExplicit", "trackCount":18, "copyright":"Foo", "country":"USA", "currency":"USD", "releaseDate":"2008-01-01T08:00:00Z", "primaryGenreName":"Musicals"}, 
        {"wrapperType":"collection", "collectionType":"Album", "artistId":1, "collectionId":12, "amgArtistId":3, "artistName":"ABBA", "collectionName":"Bar", "collectionCensoredName":"Bar", "artistViewUrl":"example.com", "collectionViewUrl":"example.com", "artworkUrl60":"example.com", "artworkUrl100":"example.com", "collectionPrice":9.99, "collectionExplicitness":"notExplicit", "trackCount":19, "copyright":"Foo", "country":"USA", "currency":"USD", "releaseDate":"2014-01-01T08:00:00Z", "primaryGenreName":"Pop"}, 
        {"wrapperType":"collection", "collectionType":"Album", "artistId":2, "collectionId":13, "amgArtistId":30, "artistName":"Benny", "collectionName":"Baz", "collectionCensoredName":"Baz", "artistViewUrl":"example.com", "collectionViewUrl":"example.com", "artworkUrl60":"example.com", "artworkUrl100":"example.com", "collectionPrice":11.99, "collectionExplicitness":"notExplicit", "trackCount":19, "copyright":"Foo", "country":"USA", "currency":"USD", "releaseDate":"2018-07-13T07:00:00Z", "primaryGenreName":"Musicals"}, 
        {"wrapperType":"collection", "collectionType":"Album", "artistId":1, "collectionId":14, "amgArtistId":3, "artistName":"ABBA", "collectionName":"FooBar", "collectionCensoredName":"FooBar", "artistViewUrl":"example.com", "collectionViewUrl":"example.com", "artworkUrl60":"example.com", "artworkUrl100":"example.com", "collectionPrice":9.99, "collectionExplicitness":"notExplicit", "trackCount":11, "copyright":"Foo", "country":"USA", "currency":"USD", "releaseDate":"2021-11-05T07:00:00Z", "primaryGenreName":"Pop"}, 
        {"wrapperType":"collection", "collectionType":"Album", "artistId":2, "collectionId":15, "amgArtistId":30, "artistName":"ABBA", "collectionName":"FooBaz", "collectionCensoredName":"FooBaz", "artistViewUrl":"example.com", "collectionViewUrl":"example.com", "artworkUrl60":"example.com", "artworkUrl100":"example.com", "collectionPrice":9.99, "collectionExplicitness":"notExplicit", "trackCount":20, "copyright":"Foo", "country":"USA", "currency":"USD", "releaseDate":"2008-01-01T08:00:00Z", "primaryGenreName":"Pop"}
    ]}
    """

    private fun createSmallTopAlbumsResponseJson() = """{"resultCount":2,
    "results": [
        {"wrapperType":"artist", "artistType":"Artist", "artistName":"ABBA", "artistLinkUrl":"example.com", "artistId":1, "amgArtistId":3, "primaryGenreName":"Pop", "primaryGenreId":14}, 
        {"wrapperType":"collection", "collectionType":"Album", "artistId":1, "collectionId":11, "amgArtistId":3, "artistName":"Benny", "collectionName":"Foo", "collectionCensoredName":"Foo", "artistViewUrl":"example.com", "collectionViewUrl":"example.com", "artworkUrl60":"example.com", "artworkUrl100":"example.com", "collectionPrice":9.99, "collectionExplicitness":"notExplicit", "trackCount":18, "copyright":"Foo", "country":"USA", "currency":"USD", "releaseDate":"2008-01-01T08:00:00Z", "primaryGenreName":"Musicals"} 
    ]}
    """

    private fun createNoneTopAlbumsResponseJson() = """{"resultCount":1,
    "results": [
        {"wrapperType":"artist", "artistType":"Artist", "artistName":"ABBA", "artistLinkUrl":"example.com", "artistId":1, "amgArtistId":3, "primaryGenreName":"Pop", "primaryGenreId":14} 
    ]}
    """

    private fun createMoreThanFiveTopAlbumsResponseJson() = """{"resultCount":8,
    "results": [
        {"wrapperType":"artist", "artistType":"Artist", "artistName":"ABBA", "artistLinkUrl":"example.com", "artistId":1, "amgArtistId":3, "primaryGenreName":"Pop", "primaryGenreId":14}, 
        {"wrapperType":"collection", "collectionType":"Album", "artistId":1, "collectionId":11, "amgArtistId":3, "artistName":"Benny", "collectionName":"Foo", "collectionCensoredName":"Foo", "artistViewUrl":"example.com", "collectionViewUrl":"example.com", "artworkUrl60":"example.com", "artworkUrl100":"example.com", "collectionPrice":9.99, "collectionExplicitness":"notExplicit", "trackCount":18, "copyright":"Foo", "country":"USA", "currency":"USD", "releaseDate":"2008-01-01T08:00:00Z", "primaryGenreName":"Musicals"}, 
        {"wrapperType":"collection", "collectionType":"Album", "artistId":1, "collectionId":12, "amgArtistId":3, "artistName":"ABBA", "collectionName":"Bar", "collectionCensoredName":"Bar", "artistViewUrl":"example.com", "collectionViewUrl":"example.com", "artworkUrl60":"example.com", "artworkUrl100":"example.com", "collectionPrice":9.99, "collectionExplicitness":"notExplicit", "trackCount":19, "copyright":"Foo", "country":"USA", "currency":"USD", "releaseDate":"2014-01-01T08:00:00Z", "primaryGenreName":"Pop"}, 
        {"wrapperType":"collection", "collectionType":"Album", "artistId":2, "collectionId":13, "amgArtistId":30, "artistName":"Benny", "collectionName":"Baz", "collectionCensoredName":"Baz", "artistViewUrl":"example.com", "collectionViewUrl":"example.com", "artworkUrl60":"example.com", "artworkUrl100":"example.com", "collectionPrice":11.99, "collectionExplicitness":"notExplicit", "trackCount":19, "copyright":"Foo", "country":"USA", "currency":"USD", "releaseDate":"2018-07-13T07:00:00Z", "primaryGenreName":"Musicals"}, 
        {"wrapperType":"collection", "collectionType":"Album", "artistId":1, "collectionId":14, "amgArtistId":3, "artistName":"ABBA", "collectionName":"FooBar", "collectionCensoredName":"FooBar", "artistViewUrl":"example.com", "collectionViewUrl":"example.com", "artworkUrl60":"example.com", "artworkUrl100":"example.com", "collectionPrice":9.99, "collectionExplicitness":"notExplicit", "trackCount":11, "copyright":"Foo", "country":"USA", "currency":"USD", "releaseDate":"2021-11-05T07:00:00Z", "primaryGenreName":"Pop"}, 
        {"wrapperType":"collection", "collectionType":"Album", "artistId":2, "collectionId":15, "amgArtistId":30, "artistName":"ABBA", "collectionName":"FooBaz", "collectionCensoredName":"FooBaz", "artistViewUrl":"example.com", "collectionViewUrl":"example.com", "artworkUrl60":"example.com", "artworkUrl100":"example.com", "collectionPrice":9.99, "collectionExplicitness":"notExplicit", "trackCount":20, "copyright":"Foo", "country":"USA", "currency":"USD", "releaseDate":"2008-01-01T08:00:00Z", "primaryGenreName":"Pop"},
        {"wrapperType":"artist", "artistType":"Artist", "artistName":"ABBA foo", "artistLinkUrl":"example.com", "artistId":2, "amgArtistId":4, "primaryGenreName":"Pop", "primaryGenreId":14}, 
        {"wrapperType":"collection", "collectionType":"Album", "artistId":2, "collectionId":16, "amgArtistId":4, "artistName":"ABBA foo", "collectionName":"FooBarBaz", "collectionCensoredName":"FooBarBaz", "artistViewUrl":"example.com", "collectionViewUrl":"example.com", "artworkUrl60":"example.com", "artworkUrl100":"example.com", "collectionPrice":9.99, "collectionExplicitness":"notExplicit", "trackCount":20, "copyright":"Foo", "country":"USA", "currency":"USD", "releaseDate":"2008-01-01T08:00:00Z", "primaryGenreName":"Pop"}
    ]}
    """

}
