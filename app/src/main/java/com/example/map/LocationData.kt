package com.example.map

data class LocationData(
    val baterai: Double? = 0.0,
    val status: String? = "",
    val jarak: Double? = 0.0,
    val latitude: Double? = 0.0,
    val longitude: Double? = 0.0,
    val nama: String? = ""
) {
    constructor() : this(0.0, "", 0.0, 0.0, 0.0, "")
}