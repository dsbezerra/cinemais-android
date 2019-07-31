package com.diegobezerra.core.cinemais.domain.model

data class SessionMatcher(
    val filters: HashSet<String>,
    val movieId: Int? = null
) {

    fun matches(session: Session): Boolean {
        if (onlyForMovie() && session.movieId != movieId) {
            return false
        }

        return if (filters.isNotEmpty()) {
            val properties = session.getProperties()
            properties.containsAll(filters)
        } else {
            true
        }
    }

    private fun onlyForMovie(): Boolean {
        return movieId != null
    }

}