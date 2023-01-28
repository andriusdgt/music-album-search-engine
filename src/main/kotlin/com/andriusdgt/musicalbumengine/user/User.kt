package com.andriusdgt.musicalbumengine.user

import kotlinx.serialization.Serializable
import javax.persistence.*

@Entity
@Table(name = "user")
@Serializable
data class User (

    @Id
    @GeneratedValue
    val id: Long = 0,

    @Column(unique = true)
    @ElementCollection
    @kotlinx.serialization.Transient
    val favoriteArtistIds: Set<Long> = emptySet()

)
