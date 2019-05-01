package com.diegobezerra.core.cinemais.domain.model

data class Posters(
    val small: String? = null,
    val medium: String? = null,
    val large: String? = null
) {
    companion object {
        const val SMALL = "pequeno"
        const val MEDIUM = "medio"
        const val LARGE = "grande"
    }

    fun best(wifi: Boolean = false): String? {
        if (!large.isNullOrEmpty() && wifi) {
            return large
        }

        if (!medium.isNullOrEmpty()) {
            return medium
        }

        return small
    }
}