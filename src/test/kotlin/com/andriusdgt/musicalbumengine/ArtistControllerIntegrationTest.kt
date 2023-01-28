package com.andriusdgt.musicalbumengine

import com.andriusdgt.musicalbumengine.artist.ArtistController
import com.andriusdgt.musicalbumengine.config.RedisIntegrationTestFixture
import com.andriusdgt.musicalbumengine.user.UserController
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest(classes = [RedisIntegrationTestFixture::class])
class ArtistControllerIntegrationTest {

    @Autowired
    private lateinit var userController: UserController

    @Autowired
    private lateinit var artistController: ArtistController

    @Test
    fun getsArtists(): Unit = runBlocking {
        val artists = artistController.getArtists("Queen")

        assertThat(artists)
            .hasSizeGreaterThan(3)
            .hasSizeLessThanOrEqualTo(50)
        assertThat(artists[0].name).isEqualTo("Queen")
    }

    @Test
    fun getsTopAlbums(): Unit = runBlocking {
        val artists = artistController.getArtists("Queen")
        val topAlbums = artistController.getTopAlbums(artists.find { it.name == "Queen" }!!.amgId)

        assertThat(topAlbums)
            .hasSizeLessThanOrEqualTo(5)
        assertThat(topAlbums.map { it.name })
            .anyMatch { it.contains("Bohemian Rhapsody") }
    }

    @Test
    @Transactional
    @Disabled("Hibernate value cast exceptions fail the test")
    fun savesArtistToFavorites() {
        val favoriteArtistAmgId = 1000L

        userController.create()
        artistController.saveArtistToFavorites(favoriteArtistAmgId, 1L)
        val favoriteArtists = artistController.getFavoriteArtists(1L)

        assertThat(favoriteArtists).isEqualTo(setOf(favoriteArtistAmgId))
    }

}
