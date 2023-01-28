package com.andriusdgt.musicalbumengine.album

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AlbumRepository : CrudRepository<Album, Long>
