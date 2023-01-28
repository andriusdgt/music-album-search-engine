package com.andriusdgt.musicalbumengine.album

import com.andriusdgt.musicalbumengine.TooManyRequestsException
import com.andriusdgt.musicalbumengine.artist.Artist
import com.andriusdgt.musicalbumengine.artist.ArtistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

private val logger = KotlinLogging.logger {}

@Service
class AlbumService(
    @Value("\${album.db.ttl-in-seconds}") private val albumDbTtlInSeconds: Long,
    @Value("\${album.cache.ttl-in-seconds}") private val albumCacheTtlInSeconds: Long,
    private val redisTemplate: RedisTemplate<String, String>,
    private val albumDownloader: AlbumDownloader,
    private val artistRepository: ArtistRepository,
    private val albumRepository: AlbumRepository
) {

    val albumCacheTtl: Duration = Duration.of(albumCacheTtlInSeconds, ChronoUnit.SECONDS)

    @Transactional
    suspend fun getTopAlbums(artistAmgId: Long): List<Album> {
        val cachedTopAlbums: List<String> = redisTemplate.opsForList().range("topAlbums:artist:$artistAmgId", 0, 49)!!
        if (cachedTopAlbums.isNotEmpty()) {
            logger.debug { "Cache value for key \"topAlbums:artist:$artistAmgId\" is $cachedTopAlbums" }
            redisTemplate.expire("topAlbums:artist:$artistAmgId", albumCacheTtl)
            return cachedTopAlbums.map { it.cacheValueToAlbum() }
        }
        with(getArtistFromRepository(artistAmgId)) {
            if (this.isPresent) {
                val savedTopAlbums: List<Album> = this.get().topAlbums
                var refreshedAlbums = emptyList<Album>()
                if (savedTopAlbums.isEmpty() || areOutdated(savedTopAlbums)) {
                    logger.info { "Top albums by artistAmgId \"$artistAmgId\" not found or out of date, downloading" }
                    refreshedAlbums = getRefreshedAlbums(artistAmgId, this.get()).orElse(emptyList())
                }
                if (refreshedAlbums.isEmpty()) {
                    logger.info { "Top albums by artistAmgId \"$artistAmgId\" cache got stale, updating" }
                    storeInCache(artistAmgId, savedTopAlbums.map { it.toAlbumCacheKey() })
                }
                return refreshedAlbums.ifEmpty { savedTopAlbums }
            }
            return emptyList()
        }
    }

    private suspend fun getArtistFromRepository(artistAmgId: Long) =
        withContext(Dispatchers.IO) { artistRepository.findByAmgId(artistAmgId) }

    private fun areOutdated(albums: List<Album>) =
        albums.any { it.lastUpdated.plusSeconds(albumDbTtlInSeconds).isBefore(LocalDateTime.now(ZoneOffset.UTC)) }

    private suspend fun getRefreshedAlbums(artistAmgId: Long, artist: Artist): Optional<List<Album>> {
        try {
            return Optional.of(
                albumDownloader.downloadTopFive(artistAmgId).also {
                    withContext(Dispatchers.IO) {
                        logger.info { "Updating top albums database and cache for artistAmgId \"$artistAmgId\"" }
                        artistRepository.save(artist)
                        albumRepository.saveAll(it)
                        storeInCache(artistAmgId, it.map { it.toAlbumCacheKey() })
                    }
                })
        } catch (ex: TooManyRequestsException) {
            logger.warn { ex }
        }
        return Optional.empty()
    }

    private fun storeInCache(key: Long, albums: List<String>) {
        redisTemplate.opsForList().rightPushAll("topAlbums:artist:$key", *albums.toTypedArray())
        redisTemplate.expire("topAlbums:artist:$key", albumCacheTtl)
    }

    private fun Album.toAlbumCacheKey() = "${this.id}#${this.name}"

    private fun String.cacheValueToAlbum(): Album {
        val albumTokens = this.split("#")
        return Album(albumTokens[0].toLong(), albumTokens[1], null)
    }
}
