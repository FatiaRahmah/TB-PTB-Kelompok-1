package com.example.rumafrontend.data.repository

import com.example.rumafrontend.data.model.Store
import com.example.rumafrontend.data.model.OverpassResponse
import com.example.rumafrontend.network.OverpassClient

object StoreRepository {

    suspend fun getNearbyStores(
        lat: Double,
        lon: Double
    ): List<Store> {

        val query = """
            [out:json][timeout:25];
            (
              node["shop"="convenience"](around:5000,$lat,$lon);
              node["shop"="supermarket"](around:5000,$lat,$lon);
              node["amenity"="marketplace"](around:5000,$lat,$lon);
            );
            out body;
        """.trimIndent()

        val response: OverpassResponse =
            OverpassClient.api.searchStores(query)

        return response.elements.mapNotNull { element ->
            val name = element.tags?.get("name") ?: return@mapNotNull null
            val latEl = element.lat ?: return@mapNotNull null
            val lonEl = element.lon ?: return@mapNotNull null

            Store(
                name = name,
                lat = latEl,
                lon = lonEl
            )
        }
    }
}