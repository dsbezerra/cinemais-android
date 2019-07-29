package com.diegobezerra.core.cinemais.domain.model

data class SessionMatcher(val filters: HashSet<String>) {

    fun matches(session: Session): Boolean {
        return if (filters.isNotEmpty()) {
            val properties = session.getProperties()
            properties.containsAll(filters)
        } else {
            true
        }
    }

}