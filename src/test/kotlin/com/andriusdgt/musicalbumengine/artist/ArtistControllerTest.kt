package com.andriusdgt.musicalbumengine.artist

import com.andriusdgt.musicalbumengine.album.AlbumService
import com.andriusdgt.musicalbumengine.user.UserService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
class ArtistControllerTest {

    @Mock
    private lateinit var artistService: ArtistService

    @Mock
    private lateinit var albumService: AlbumService

    @Mock
    private lateinit var userService: UserService

    lateinit var controller: ArtistController

    @BeforeEach
    fun setUp() {
        controller = ArtistController(artistService, albumService, userService)
    }

    @Test
    fun `gets artists`() = runTest {
        val expectedArtistName = "Queen"

        controller.getArtists(expectedArtistName)

        verify(artistService).getArtists(expectedArtistName.lowercase())
    }

    @Test
    fun `gets top albums`() = runTest {
        val expectedAmgId: Long = 777

        controller.getTopAlbums(expectedAmgId)

        verify(albumService).getTopAlbums(expectedAmgId)
    }

    @Test
    fun `saves favorite artists`() {
        val expectedAmgId: Long = 42
        val expectedUserId: Long = 300

        controller.saveArtistToFavorites(expectedAmgId, expectedUserId)

        verify(userService).saveFavoriteArtist(expectedAmgId, expectedUserId)
    }

    @Test
    fun `gets favorite artists`() {
        val expectedUserId: Long = 999

        controller.getFavoriteArtists(expectedUserId)

        verify(userService).getFavoriteArtists(expectedUserId)
    }

}
