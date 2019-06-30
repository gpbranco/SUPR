package com.impraise.supr.game.domain

import com.impraise.supr.data.Result
import com.impraise.supr.data.ResultList
import com.impraise.supr.game.helper.GameTestHelper
import com.impraise.supr.game.scenes.data.model.Member
import com.impraise.supr.game.scenes.domain.*
import com.impraise.suprdemo.scenes.domain.model.Round
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argWhere
import com.nhaarman.mockito_kotlin.mock
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock

/**
 * Created by guilhermebranco on 3/11/18.
 */
class CreateGameUseCaseTest {

    @Mock
    private lateinit var membersPaginatedUseCase: SplitIntoGroupsUseCase

    private lateinit var useCase: CreateGameUseCase

    @Before
    fun setup() {
        val members = members()
        membersPaginatedUseCase = mock()
        createGame(members)
    }

    @Test
    fun `should create game with five rounds`() {
        val testObserver = useCase.get(Unit).test()

        testObserver.assertComplete()
        testObserver.assertValue {
            val success = it as Result.Success
            success.data.currentState.currentRound != Round.INVALID_ROUND
        }

        testObserver.assertValue {
            val success = it as Result.Success
            success.data.currentState.totalRounds == 5
        }
    }

    @Test
    fun `should limit number of rounds`() {
        val expectedRoundsCount = 10L
        createGame(GameTestHelper.members(1..50), expectedRoundsCount)

        val testObserver = useCase.get(Unit).test()

        testObserver.assertComplete()
        testObserver.assertValue {
            val success = it as Result.Success
            success.data.currentState.totalRounds == expectedRoundsCount.toInt()
        }
    }

    @Test
    fun `should filter groups without at least one avatar`() {
        val noAvatar = Member("NO_AVATAR", "NO_AVATAR")
        val list = members().toMutableList()
        val expectedSize = list.size
        val listWithAtLeastOneAvatar = (1..4).map {
            if (it == 1) Member(it.toString(), it.toString())
            else noAvatar
        }

        val listWithoutAvatar = (1..4).map {
            noAvatar
        }

        list += listWithoutAvatar
        list += listWithAtLeastOneAvatar

        val condition = mock<RoundCreationHelper.Condition<Member>>().apply {
            given(this.satisfied(argWhere {
                it != noAvatar
            })).willReturn(true)
        }

        val result = GameCreationHelper(condition).filterGroupsWithoutAvatar(list)

        Assert.assertEquals(expectedSize + 1, result.size)
    }

    private fun members(): List<List<Member>> {
        return GameTestHelper.members()
    }

    private fun createGame(members: List<List<Member>>, maxRoundsCount: Long = 5) {
        given(membersPaginatedUseCase.get(any())).willReturn(Single.just(ResultList.Success(members)))
        useCase = CreateGameUseCase(membersPaginatedUseCase,
                CreateRoundUseCase(roundCreationHelper = RoundCreationHelper(GameTestHelper.alwaysTrueCondition())),
                GameCreationHelper(GameTestHelper.alwaysTrueCondition()),
                maxRoundsCount,
                Schedulers.trampoline(),
                Schedulers.trampoline())
    }
}