package com.github.julienfauvel.localisation4ltrophy.services

import android.app.Activity
import android.view.View
import com.github.julienfauvel.localisation4ltrophy.models.Coordonnee
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result


class CoordonneeService {
    companion object {
        fun getCoordonnee(activity: Activity, view: View): Triple<Request, Response, Result<Coordonnee, FuelError>> {
            return "http://api.4lentraid.com/coordonnee".httpGet().responseObject(Coordonnee.Deserializer())
        }
    }
}