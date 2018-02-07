package com.github.julienfauvel.localisation4ltrophy.models

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson

data class Coordonnee(val ville: String, val longitude: Float, val latitude: Float){
    class Deserializer: ResponseDeserializable<Coordonnee> {
        override fun deserialize(content: String): Coordonnee {
            return Gson().fromJson(content, Coordonnee::class.java)
        }
    }
}
