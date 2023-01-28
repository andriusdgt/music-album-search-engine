package com.andriusdgt.musicalbumengine.album

import com.andriusdgt.musicalbumengine.TooManyRequestsException
import com.andriusdgt.musicalbumengine.artist.Artist
import com.andriusdgt.musicalbumengine.artist.ArtistRepository
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.data.redis.core.ListOperations
import org.springframework.data.redis.core.RedisTemplate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@ExtendWith(MockitoExtension::class)
class AlbumServiceTest {

    @Mock
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @Mock
    private lateinit var listOperations: ListOperations<String, String>

    @Mock
    private lateinit var albumDownloader: AlbumDownloader

    @Mock
    private lateinit var artistRepository: ArtistRepository

    @Mock
    private lateinit var albumRepository: AlbumRepository

    private lateinit var service: AlbumService

    @BeforeEach
    fun setUp() {
        service = AlbumService(120L, 120L, redisTemplate, albumDownloader, artistRepository, albumRepository)
        `when`(redisTemplate.opsForList()).thenReturn(listOperations)
        `when`(listOperations.range(any(), any(), any())).thenReturn(emptyList())
    }

    @Test
    fun `returns empty album list if artist is not found`(): Unit = runBlocking {
        val albums = listOf(Album(1, "the game"))
        val artist = Artist(1, 2, "queen", albums)
        `when`(artistRepository.findByAmgId(any())).thenReturn(Optional.empty())

        val actualAlbums = service.getTopAlbums(artist.amgId)

        assertThat(actualAlbums).isEmpty()
        verify(artistRepository).findByAmgId(artist.amgId)
        verify(albumDownloader, never()).downloadTopFive(any())
    }

    @Test
    fun `gets top albums from cache`(): Unit = runBlocking {
        val albumId = 2L
        val expectedAlbum = Album(albumId, "the game")
        `when`(listOperations.range("topAlbums:artist:$albumId", 0, 49))
            .thenReturn(listOf("$albumId#${expectedAlbum.name}"))

        val actualAlbums = service.getTopAlbums(albumId)

        assertEquals(listOf(expectedAlbum), actualAlbums.map { it.copy(lastUpdated = expectedAlbum.lastUpdated) })
        assertThat(actualAlbums[0].lastUpdated).isCloseToUtcNow(within(1, ChronoUnit.SECONDS))
    }

    @Test
    fun `gets top albums from repository`(): Unit = runBlocking {
        val expectedAlbums = listOf(Album(1, "the game"))
        val artist = Artist(1, 2, "queen", expectedAlbums)
        `when`(artistRepository.findByAmgId(any())).thenReturn(Optional.of(artist))

        val actualAlbums = service.getTopAlbums(artist.amgId)

        assertEquals(expectedAlbums, actualAlbums)
        verify(artistRepository).findByAmgId(artist.amgId)
        verify(albumDownloader, never()).downloadTopFive(any())
    }

    @Test
    fun `downloads and updates top albums in repository if missing`(): Unit = runBlocking {
        val expectedAlbums = listOf(Album(1, "the game"))
        val artist = Artist(1, 2, "queen", emptyList())
        `when`(artistRepository.findByAmgId(any())).thenReturn(Optional.of(artist))
        `when`(albumDownloader.downloadTopFive(any())).thenReturn(expectedAlbums)

        val actualAlbums = service.getTopAlbums(artist.amgId)

        assertEquals(expectedAlbums, actualAlbums)
        verify(artistRepository).findByAmgId(artist.amgId)
        verify(albumDownloader).downloadTopFive(artist.amgId)
        verify(artistRepository).save(artist)
        verify(albumRepository).saveAll(expectedAlbums)
    }

    @Test
    fun `downloads and updates top albums in repository if any is out of date`(): Unit = runBlocking {
        val outOfDateAlbum = Album(1, "the game", null, LocalDateTime.MIN)
        val album = Album(2, "the works", null, LocalDateTime.now())
        val artist = Artist(1, 2, "queen", listOf(album, outOfDateAlbum))
        val expectedAlbums = listOf(album, outOfDateAlbum.copy(lastUpdated = LocalDateTime.now()))
        `when`(artistRepository.findByAmgId(any())).thenReturn(Optional.of(artist))
        `when`(albumDownloader.downloadTopFive(any())).thenReturn(expectedAlbums)

        val actualAlbums = service.getTopAlbums(artist.amgId)

        assertEquals(expectedAlbums, actualAlbums)
        verify(artistRepository).findByAmgId(artist.amgId)
        verify(albumDownloader).downloadTopFive(artist.amgId)
        verify(artistRepository).save(artist)
        verify(albumRepository).saveAll(expectedAlbums)
    }

    @Test
    fun `fallbacks to top albums from repository if refresh is done too many times`(): Unit = runBlocking {
        val expectedAlbums = listOf(Album(1, "the game", null, LocalDateTime.MIN))
        val artist = Artist(1, 2, "queen", expectedAlbums)
        `when`(artistRepository.findByAmgId(any())).thenReturn(Optional.of(artist))
        `when`(albumDownloader.downloadTopFive(any())).thenThrow(TooManyRequestsException())

        val actualAlbums = service.getTopAlbums(artist.amgId)

        assertEquals(expectedAlbums, actualAlbums)
        verify(artistRepository).findByAmgId(artist.amgId)
        verify(albumDownloader).downloadTopFive(artist.amgId)
    }

    @Test
    fun `repositories are not updated if refresh is done too many times`(): Unit = runBlocking {
        val expectedAlbums = listOf(Album(1, "the game", null, LocalDateTime.MIN))
        val artist = Artist(1, 2, "queen", expectedAlbums)
        `when`(artistRepository.findByAmgId(any())).thenReturn(Optional.of(artist))
        `when`(albumDownloader.downloadTopFive(any())).thenThrow(TooManyRequestsException())

        service.getTopAlbums(artist.amgId)

        verify(artistRepository, never()).save(artist)
        verify(albumRepository, never()).saveAll(expectedAlbums)
    }

}
