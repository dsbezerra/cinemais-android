package com.diegobezerra.core.cinemais.domain.model

import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for [SessionMatcher]
 */
class SessionMatcherTest {

    val testSessions = listOf<Session>(
        Session(
            movieId = 0,
            cinemaId = 34,
            movieRating = 0,
            movieTitle = "",
            room = 1,
            format = Session.VideoFormat2D,
            version = Session.VersionNational,
            magic = false,
            startTime = "17:22",
            vip = true
        )
    )

    @Test
    fun multipleProperties_sameFilters_match() {
        // When the user is requesting VideoFormat2D and VersionNational
        val matcher = SessionMatcher(
            hashSetOf(
                Session.VideoFormat2D,
                Session.VersionNational
            )
        )
        // There's a match
        Assert.assertTrue(matcher.matches(testSessions[0]))
    }

    @Test
    fun multipleProperties_partialMatch() {
        // When the user is requesting three properties and at least one of them match
        val matcher = SessionMatcher(
            hashSetOf(
                Session.VideoFormat3D,
                Session.VideoFormat2D,
                Session.VersionSubtitled
            )
        )
        // There's a match
        Assert.assertTrue(matcher.matches(testSessions[0]))
    }

    @Test
    fun differentFilters_noMatch() {
        // When the user is requesting VideoFormat3D and VersionSubtitled
        val matcher = SessionMatcher(
            hashSetOf(
                Session.VideoFormat3D,
                Session.VersionSubtitled
            )
        )
        // But testSessions[0] got VersionNational, then there's no match
        Assert.assertFalse(matcher.matches(testSessions[0]))
    }

    @Test
    fun emptyFilters_match() {
        // When the user has not chosen any filters
        val matcher = SessionMatcher(hashSetOf())
        // There's a match
        Assert.assertTrue(matcher.matches(testSessions[0]))
    }
}