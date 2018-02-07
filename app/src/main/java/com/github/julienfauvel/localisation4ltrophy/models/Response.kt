package com.github.julienfauvel.localisation4ltrophy.models

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson

data class Message(val error: ErrorMessage) {
    class Deserializer: ResponseDeserializable<Message> {
        override fun deserialize(content: String): Message {
            return Gson().fromJson(content, Message::class.java)
        }
    }
}
data class ErrorMessage(val erreur: Boolean)