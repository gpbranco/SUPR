package com.impraise.supr.game.helper

import com.impraise.supr.game.scenes.data.model.Member
import com.impraise.supr.game.scenes.domain.RoundCreationHelper
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import org.mockito.BDDMockito.given

object GameTestHelper {

    fun members(range: IntRange = (1..5)): List<List<Member>> {
        val members = mutableListOf<List<Member>>()

        (range).forEach { _ ->
            val current = (1..4).map { Member(it.toString(), it.toString()) }
            members.add(current)
        }
        return members
    }

    fun alwaysTrueCondition(): RoundCreationHelper.Condition<Member> {
        return mock<RoundCreationHelper.Condition<Member>>().apply {
            given(this.satisfied(any())).willReturn(true)
        }
    }
}
