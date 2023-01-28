package com.andriusdgt.musicalbumengine.artist

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ArtistRepository : CrudRepository<Artist, Long> {

    fun findByAmgId(amgId: Long): Optional<Artist>
    fun findTop50ByNameContainingIgnoreCase(name: String): List<Artist>

}
