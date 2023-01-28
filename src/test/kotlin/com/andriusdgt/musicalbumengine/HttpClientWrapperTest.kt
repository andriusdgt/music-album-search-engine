package com.andriusdgt.musicalbumengine

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.engine.mock.*
import io.ktor.client.features.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class HttpClientWrapperTest {

    @Test
    fun `executes REST GET call against an url and returns result in a JsonNode`() {
        runBlocking {
            val expectedUrl = "https://example.com/"
            val responseJson = """{"resultCount":0,"results":[]}"""

            val mockEngine = MockEngine { request ->
                assertEquals(expectedUrl, request.url.toString())

                respond(
                    content = ByteReadChannel(responseJson),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }

            val wrapper = HttpClientWrapper(mockEngine, jacksonObjectMapper())

            assertEquals(wrapper.doGet(expectedUrl), jacksonObjectMapper().readTree(responseJson))
        }
    }

    @Test
    fun `throws exception if client returns 429 status code`() {
        runBlocking {
            val mockEngine = MockEngine {
                respond(
                    content = ByteReadChannel(""),
                    status = HttpStatusCode.TooManyRequests,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }

            val wrapper = HttpClientWrapper(mockEngine, jacksonObjectMapper())

            assertThrows<TooManyRequestsException> {
                wrapper.doGet("https://example.com/")
            }
        }
    }

    @Test
    fun `will let exception pass through if client returns another error`() {
        runBlocking {
            val mockEngine = MockEngine {
                respond(
                    content = ByteReadChannel(""),
                    status = HttpStatusCode.BadRequest,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }

            val wrapper = HttpClientWrapper(mockEngine, jacksonObjectMapper())

            assertThrows<ClientRequestException> {
                wrapper.doGet("https://example.com/")
            }
        }
    }


}
