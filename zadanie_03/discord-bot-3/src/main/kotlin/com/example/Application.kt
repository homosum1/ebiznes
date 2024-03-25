package com.example

import com.example.plugins.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.headers
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.websocket.*
import io.ktor.server.websocket.WebSockets
import io.ktor.websocket.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive


fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(WebSockets)

    configureRouting()

    launch {
        connectToDiscordGateway()
    }
}

suspend fun connectToDiscordGateway() {
    val discordBotToken = "PLACE_FOR_BOT_TOKEN"

    try {
        val client = HttpClient(CIO) {
            install(io.ktor.client.plugins.websocket.WebSockets)
        }

        println("CREATING WEBSOCKET")
        client.webSocket(
            method = HttpMethod.Get,
            host = "gateway.discord.gg",
            path = "/?v=6&encoding=json"
        ) {

            val session = this

            val identifyPayload = """
                    {
                        "op": 2,
                        "d": {
                            "token": "$discordBotToken",
                            "intents": 513,
                            "properties": {
                                """ + "\"\$os\": \"linux\"," + """
                                """ + "\"\$browser\": \"my_discord_bot\"," + """
                                """ + "\"\$device\": \"my_discord_bot\"" + """
                            }
                        }
                    }
                    """.trimIndent()

            session.send(Frame.Text(identifyPayload))

//            withTimeoutOrNull(3600000) {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()

                    println(text)

                    try {
                        val jsonElement = Json.parseToJsonElement(text)


                        if (jsonElement.jsonObject["t"]?.jsonPrimitive?.content == "MESSAGE_CREATE") {
                            val data = jsonElement.jsonObject["d"] ?: break;


                            val messageContent = data.jsonObject["content"]?.jsonPrimitive?.content ?: "Empty mes"
                            val author = data.jsonObject["author"]?.jsonObject
                            val username = author?.get("username")?.jsonPrimitive?.content ?: "???"

                            val mentions = data.jsonObject["mentions"]?.jsonArray ?: break;

                            val usernames = mentions.mapNotNull {
                                mention -> mention.jsonObject["username"]?.jsonPrimitive?.content
                            }


                            val isReksioMentioned = usernames.any {
                                it  == "reksio"
                            }

                            println("from: $username, message: $messageContent, mentioned: $isReksioMentioned")

                            if(isReksioMentioned) {
                                if(messageContent.contains("echo")) {
                                    sendMessageToDiscord(discordBotToken, "Hau Hau Hau!!!")
                                }

                                if(messageContent.contains("fetch")) {
                                    sendMessageToDiscord(discordBotToken, "🪃")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("Parsing error")
                    }
                }
            }
//            }

        }

    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}

suspend fun sendMessageToDiscord(discordBotToken: String, message: String) {
    val apiUrl = "https://discordapp.com/api/channels/PLACE_FOR_CHANNEL_ID/messages"
    val client = HttpClient(CIO)

    try {
        val response: HttpResponse = client.post(apiUrl) {
            headers {
                append(HttpHeaders.Authorization, "Bot $discordBotToken")
                append(HttpHeaders.ContentType, ContentType.Application.Json)
                append(HttpHeaders.UserAgent, "reksio")
            }
            setBody("{\"content\":\"$message\"}")
        }

        println("Message sent successfully: $message")
    } catch (e: Exception) {
        println("Error sending message: ${e.message}")
    } finally {
        client.close()
    }
}