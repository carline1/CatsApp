package com.example.catsapp.api.models.res

data class BreedFilterResponse(
    val id: String,
    val name: String
) {
    override fun toString(): String {
        return name
    }
}