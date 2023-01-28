package com.andriusdgt.musicalbumengine

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class HttpClientWrapper(engine: HttpClientEngine, private val objectMapper: ObjectMapper) {

    private val httpClient = HttpClient(engine) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    @Throws(TooManyRequestsException::class)
    suspend fun doGet(url: String): JsonNode {
        val response: HttpResponse = try {
            httpClient.get(url)
        } catch (ex: ClientRequestException) {
            if (ex.response.status == HttpStatusCode.TooManyRequests)
                throw TooManyRequestsException()
            else
                throw ex
        }
        val responseJson: String = response.receive()
        return objectMapper.readTree(responseJson)
    }

}
