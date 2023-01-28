package com.andriusdgt.musicalbumengine.user

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class UserController(private val userService: UserService) {

    @PostMapping
    fun create(): User = userService.create()

}
