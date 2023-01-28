package com.andriusdgt.musicalbumengine.artist

import com.andriusdgt.musicalbumengine.TooManyRequestsException
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
class ArtistService(
    @Value("\${artist.db.ttl-in-seconds}") private val artistDbTtlInSeconds: Long,
    @Value("\${artist.cache.ttl-in-seconds}") private val artistCacheTtlInSeconds: Long,
    private val redisTemplate: RedisTemplate<String, String>,
    private val artistDownloader: ArtistDownloader,
    private val artistRepository: ArtistRepository
) {

    val artistCacheTTL: Duration = Duration.of(artistCacheTtlInSeconds, ChronoUnit.SECONDS)

    @Transactional
    suspend fun getArtists(name: String): List<Artist> {
        val cachedArtists: List<String> = redisTemplate.opsForList().range("artists:$name", 0, 49)!!
        if (cachedArtists.isNotEmpty()) {
            logger.debug { "Cache value for key \"artists:$name\" is $cachedArtists" }
            redisTemplate.expire("artists:$name", artistCacheTTL)
            return cachedArtists.map { it.cacheValueToArtist() }
        }

        with(getArtistsFromRepository(name)) {
            var refreshedArtists = emptyList<Artist>()
            if (this.isEmpty() || areOutdated(this)) {
                logger.info { "Artists by name \"$name\" not found or out of date, downloading" }
                refreshedArtists = getRefreshedArtists(name).orElse(emptyList())
            }
            if (refreshedArtists.isEmpty()) {
                logger.info { "Artists by name \"$name\" cache got stale, updating" }
                storeInCache(name, this.map { it.toArtistCacheKey() })
            }
            return refreshedArtists.ifEmpty { this }
        }
    }

    private fun storeInCache(key: String, artists: List<String>) {
        redisTemplate.opsForList().rightPushAll("artists:$key", *artists.toTypedArray())
        redisTemplate.expire("artists:$key", artistCacheTTL)
    }

    private suspend fun getArtistsFromRepository(name: String): List<Artist> =
        withContext(Dispatchers.IO) { artistRepository.findTop50ByNameContainingIgnoreCase(name) }

    private fun areOutdated(artists: List<Artist>) =
        artists.any { it.lastUpdated.plusSeconds(artistDbTtlInSeconds).isBefore(LocalDateTime.now(ZoneOffset.UTC)) }

    private suspend fun getRefreshedArtists(name: String): Optional<List<Artist>> {
        try {
            return Optional.of(
                artistDownloader.download(name).also {
                    withContext(Dispatchers.IO) {
                        logger.info { "Updating artist database and cache for entry \"$name\"" }
                        artistRepository.saveAll(it)
                        storeInCache(name, it.map { it.toArtistCacheKey() })

                    }
                }
            )
        } catch (ex: TooManyRequestsException) {
            logger.warn { ex }
        }
        return Optional.empty()
    }

    private fun Artist.toArtistCacheKey() = "${this.amgId}#${this.name}"

    private fun String.cacheValueToArtist(): Artist {
        val artistTokens = this.split("#")
        return Artist(0L, artistTokens[0].toLong(), artistTokens[1])
    }
}
