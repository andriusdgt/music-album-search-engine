package com.andriusdgt.musicalbumengine.artist

import com.andriusdgt.musicalbumengine.album.Album
import com.andriusdgt.musicalbumengine.album.AlbumService
import com.andriusdgt.musicalbumengine.user.UserService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/artist")
class ArtistController(
    private val artistService: ArtistService,
    private val albumService: AlbumService,
    private val userService: UserService
) {

    @GetMapping("/name/{name}")
    suspend fun getArtists(@PathVariable name: String): List<Artist> = artistService.getArtists(name.lowercase())

    @GetMapping("/{amgId}/album/top")
    suspend fun getTopAlbums(@PathVariable amgId: Long): List<Album> = albumService.getTopAlbums(amgId)

    @PutMapping("/favorite/{amgId}")
    fun saveArtistToFavorites(@PathVariable amgId: Long, @RequestParam(value = "userId") userId: Long) {
        userService.saveFavoriteArtist(amgId, userId)
    }

    @GetMapping("/favorite")
    fun getFavoriteArtists(@RequestParam(value = "userId") userId: Long): Set<Long> =
        userService.getFavoriteArtists(userId)

}
