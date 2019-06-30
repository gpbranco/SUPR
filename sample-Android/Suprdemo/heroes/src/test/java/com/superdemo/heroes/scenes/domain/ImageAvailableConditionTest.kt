package com.superdemo.heroes.scenes.domain

import com.impraise.supr.game.scenes.data.model.Member
import com.impraise.suprdemo.scenes.domain.ImageAvailableCondition
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class ImageAvailableConditionTest {

    private lateinit var condition: ImageAvailableCondition

    @Before
    fun setup() {
        condition = ImageAvailableCondition()
    }

    @Test
    fun `returns not valid when url contains image_not_available`() {
        val url = "image_not_available"
        assertFalse(condition.satisfied(Member("", url)))
    }

    @Test
    fun `return not valid when url contains extension gif`() {
        val url = ".gif"
        assertFalse(condition.satisfied(Member("", url)))
    }
}