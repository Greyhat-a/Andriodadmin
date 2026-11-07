package com.nexus.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoadmapTest {

    @Test
    fun phasesAreNumberedOneToTenWithoutGaps() {
        assertEquals((1..10).toList(), Roadmap.phases.map { it.number })
    }

    @Test
    fun everyPhaseHasTitleAndSummary() {
        Roadmap.phases.forEach {
            assertTrue("Phase ${it.number} title", it.title.isNotBlank())
            assertTrue("Phase ${it.number} summary", it.summary.isNotBlank())
        }
    }

    @Test
    fun foundationAndAcademyAreAvailableInThisBuild() {
        val available = Roadmap.phases.filter { it.state == PhaseState.AVAILABLE }
        assertEquals(listOf(1, 9), available.map { it.number })
    }

    @Test
    fun lookupByNumberReturnsMatchingPhase() {
        assertEquals("Temporary ADMIN", Roadmap.phase(4).title)
    }
}
