package com.andriusdgt.musicalbumengine.user

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class UserControllerTest {

    @Mock
    private lateinit var userService: UserService

    lateinit var controller: UserController

    @BeforeEach
    fun setUp() {
        controller = UserController(userService)
    }

    @Test
    fun `Creates user`() {
        controller.create()

        verify(userService).create()
    }

}
