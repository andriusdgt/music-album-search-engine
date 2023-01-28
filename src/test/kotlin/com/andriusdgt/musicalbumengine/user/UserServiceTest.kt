package com.andriusdgt.musicalbumengine.user

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import java.util.*

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    lateinit var service: UserService

    @BeforeEach
    fun setUp(){
        service = UserService(userRepository)
    }

    @Test
    fun `creates user`() {
        val expectedUser = User()
        `when`(userRepository.save(any())).thenReturn(expectedUser)

        val actualUser = service.create()

        assertEquals(expectedUser, actualUser)
        verify(userRepository).save(User())
    }

    @Nested
    inner class FavoriteArtistTests {

        @Test
        fun `saves first favorite artist`() {
            val favoriteArtistId = 1111L
            val presentUser = User(1, emptySet())
            val expectedSavedUser = User(1, setOf(1111L))
            `when`(userRepository.findById(any())).thenReturn(Optional.of(presentUser))

            service.saveFavoriteArtist(favoriteArtistId, presentUser.id)

            verify(userRepository).save(expectedSavedUser)
            verify(userRepository).findById(presentUser.id)
        }

        @Test
        fun `saves new favorite artist`() {
            val favoriteArtistId = 1111L
            val presentUser = User(1, setOf(300L))
            val expectedSavedUser = User(1, setOf(300L, 1111L))
            `when`(userRepository.findById(any())).thenReturn(Optional.of(presentUser))

            service.saveFavoriteArtist(favoriteArtistId, presentUser.id)

            verify(userRepository).save(expectedSavedUser)
        }

        @Test
        fun `favorite artist is not added twice`() {
            val favoriteArtistId = 1111L
            val presentUser = User(1, setOf(1111L))
            val expectedSavedUser = User(1, setOf(1111L))
            `when`(userRepository.findById(any())).thenReturn(Optional.of(presentUser))

            service.saveFavoriteArtist(favoriteArtistId, presentUser.id)

            verify(userRepository).save(expectedSavedUser)
        }

        @Test
        fun `does not save favorite artist for unrecognized user`() {
            `when`(userRepository.findById(any())).thenReturn(Optional.empty())
            assertThrows<UserNotFoundException> {
                service.saveFavoriteArtist(12345L, 1L)
            }
        }
    }

    @Test
    fun `gets user favorite artists`() {
        val user = User(1, setOf(100L, 2442L, 2111L))
        `when`(userRepository.findById(any())).thenReturn(Optional.of(user))

        val actualFavoriteArtists = service.getFavoriteArtists(user.id)

        assertEquals(user.favoriteArtistIds, actualFavoriteArtists)
        verify(userRepository).findById(user.id)
    }

    @Test
    fun `does not get favorite artist for unrecognized user`() {
        `when`(userRepository.findById(any())).thenReturn(Optional.empty())
        assertThrows<UserNotFoundException> {
            service.getFavoriteArtists(12345L)
        }
    }

}
