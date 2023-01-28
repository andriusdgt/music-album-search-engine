package com.andriusdgt.musicalbumengine.user

import org.springframework.http.*
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler


@ControllerAdvice
class UserControllerErrorHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(value = [UserNotFoundException::class])
    protected fun handleConflict(ex: RuntimeException?, request: WebRequest?): ResponseEntity<Any> {
        return handleExceptionInternal(
            ex!!,
            "{\"message\":\"User does not exist\"}",
            HttpHeaders().apply { this.contentType = MediaType.APPLICATION_JSON },
            HttpStatus.BAD_REQUEST,
            request!!
        )
    }

}
