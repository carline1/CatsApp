package com.example.catsapp.api.models.res

data class CategoryFilterResponse(
    val id: Int,
    val name: String
) {
    override fun toString(): String {
        return name
    }
}