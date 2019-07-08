package com.diegobezerra.core.cinemais.domain.model

data class Images(
    val backdrops: List<String> = listOf(),
    val posters: List<String> = listOf()
) {

    fun exists(): Boolean = backdrops.isNotEmpty() || posters.isNotEmpty()

    fun getBackdrop(index: Int): String? {
        return get(index, backdrops)
    }

    fun getPoster(index: Int): String? {
        return get(index, posters)
    }

    private fun get(index: Int, src: List<String>): String? {
        if (src.isEmpty()) {
            return null
        }
        val clamped = index.coerceIn(0, src.size - 1)
        return if (clamped >= 0 && clamped < src.size) {
            src[clamped]
        } else {
            null
        }
    }

    fun get(index: Int): String? = getBackdrop(index) ?: getPoster(index)

    fun getRandom(): String? = getRandomBackdrop() ?: getRandomPoster()

    fun getRandomBackdrop(): String? = rand(backdrops)

    fun getRandomPoster(): String? = rand(posters)

    private fun rand(src: List<String>): String? {
        return try {
            src.random()
        } catch (e: NoSuchElementException) {
            null
        }
    }
}