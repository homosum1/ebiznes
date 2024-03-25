package com.example.plugins

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*

import io.ktor.websocket.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.plugins.websocket.WebSockets


import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive


fun Application.configureRouting() {
    val discordBotToken = "SECRET_TOKEN"

    val apiUrl = "https://discordapp.com/api/channels/PLACE_FOR_CHANNEL_ID/messages"


    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        post("/send-message") {

            val message = call.receiveText()
            val messageBody = "{\"content\":\"${message}\"}"

            println(messageBody)

            try {
                val client = HttpClient(CIO)
                val response: HttpResponse = client.post(apiUrl) {
                    headers {
                        append(HttpHeaders.Authorization, "Bot $discordBotToken")
                        append(HttpHeaders.ContentType, ContentType.Application.Json)
                        append(HttpHeaders.UserAgent, "reksio")
                    }
                    setBody(messageBody)
                }
                client.close()

                call.respondText("Message sent successfully, body: ${message}")
            } catch (e: Exception) {
                call.respondText("Blad: ${e.message}, body: ${message}", status = HttpStatusCode.InternalServerError)
            }
        }

//        webSocket("/receive-messages") {
        get("/test") {

        }

    }
}
