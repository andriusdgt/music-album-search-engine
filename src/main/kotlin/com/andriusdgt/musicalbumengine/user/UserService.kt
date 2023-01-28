package com.andriusdgt.musicalbumengine.user

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(private val userRepository: UserRepository) {

    @Transactional
    fun saveFavoriteArtist(artistAmgId: Long, userId: Long) {
        val user = getUser(userId)
        userRepository.save(user.copy(favoriteArtistIds = setOf(artistAmgId).plus(user.favoriteArtistIds)))
    }

    @Transactional(readOnly = true)
    fun getFavoriteArtists(userId: Long): Set<Long> {
        return getUser(userId).favoriteArtistIds
    }

    @Transactional
    fun create(): User = userRepository.save(User())

    private fun getUser(userId: Long): User {
        val user = userRepository.findById(userId)
        if (user.isEmpty)
            throw UserNotFoundException()
        return user.get()
    }

}
