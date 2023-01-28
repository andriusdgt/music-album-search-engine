package com.andriusdgt.musicalbumengine.artist

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

@ExtendWith(MockitoExtension::class)
class ArtistServiceTest {

    @Mock
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @Mock
    private lateinit var listOperations: ListOperations<String, String>

    @Mock
    private lateinit var artistDownloader: ArtistDownloader

    @Mock
    private lateinit var artistRepository: ArtistRepository

    lateinit var service: ArtistService

    @BeforeEach
    fun setUp() {
        service = ArtistService(120L, 120L, redisTemplate, artistDownloader, artistRepository)
        `when`(redisTemplate.opsForList()).thenReturn(listOperations)
        `when`(listOperations.range(any(), any(), any())).thenReturn(emptyList())
    }

    @Test
    fun `gets artist from cache`(): Unit = runBlocking {
        val artistName = "Kalush"
        val expectedArtist = Artist(0L, 2L, artistName)
        `when`(listOperations.range("artists:$artistName", 0, 49))
            .thenReturn(listOf("${expectedArtist.amgId}#${expectedArtist.name}"))

        val actualArtists = service.getArtists(expectedArtist.name)

        assertEquals(listOf(expectedArtist), actualArtists.map { it.copy(lastUpdated = expectedArtist.lastUpdated) })
        assertThat(actualArtists[0].lastUpdated).isCloseToUtcNow(within(1, ChronoUnit.SECONDS))
    }

    @Test
    fun `gets artists from repository`(): Unit = runBlocking {
        val searchedArtist = "queen"
        val expectedArtists = listOf(Artist(1, 2, searchedArtist))
        `when`(artistRepository.findTop50ByNameContainingIgnoreCase(any())).thenReturn(expectedArtists)

        val actualArtists = service.getArtists(searchedArtist)

        assertEquals(expectedArtists, actualArtists)
        verify(artistRepository).findTop50ByNameContainingIgnoreCase(searchedArtist)
        verify(artistDownloader, never()).download(any())
    }

    @Test
    fun `downloads and updates artists in repository if missing`(): Unit = runBlocking {
        val searchedArtist = "queen"
        val expectedArtists = listOf(Artist(1, 2, searchedArtist))
        `when`(artistRepository.findTop50ByNameContainingIgnoreCase(any())).thenReturn(emptyList())
        `when`(artistDownloader.download(any())).thenReturn(expectedArtists)

        val actualArtists = service.getArtists(searchedArtist)

        assertEquals(expectedArtists, actualArtists)
        verify(artistDownloader).download(searchedArtist)
        verify(artistRepository).saveAll(expectedArtists)
    }

    @Test
    fun `downloads and updates artists in repository if any is out of date`(): Unit = runBlocking {
        val searchedArtist = "queen"
        val outOfDateArtist = Artist(1, 2, searchedArtist, listOf(), LocalDateTime.MIN)
        val artist = Artist(1, 2, "queens of roses", listOf(), LocalDateTime.now())
        val savedArtists = listOf(artist, outOfDateArtist)
        val expectedArtists = listOf(artist, outOfDateArtist.copy(lastUpdated = LocalDateTime.now()))
        `when`(artistRepository.findTop50ByNameContainingIgnoreCase(any())).thenReturn(savedArtists)
        `when`(artistDownloader.download(any())).thenReturn(expectedArtists)

        val actualArtists = service.getArtists(searchedArtist)

        assertEquals(expectedArtists, actualArtists)
        verify(artistRepository).findTop50ByNameContainingIgnoreCase(searchedArtist)
        verify(artistDownloader).download(searchedArtist)
        verify(artistRepository).saveAll(expectedArtists)
    }


}
