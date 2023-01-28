package com.andriusdgt.musicalbumengine.config

import com.andriusdgt.musicalbumengine.HttpClientWrapper
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HttpConfig {

    @Bean
    fun httpClientEngine(): HttpClientEngine = CIO.create()

    @Bean
    fun httpClientWrapper(om: ObjectMapper): HttpClientWrapper = HttpClientWrapper(httpClientEngine(), om)

}
