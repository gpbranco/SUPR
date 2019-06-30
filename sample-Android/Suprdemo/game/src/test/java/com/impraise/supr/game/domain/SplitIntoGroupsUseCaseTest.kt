package com.impraise.supr.game.domain

import com.impraise.supr.data.ResultList
import com.impraise.supr.game.scenes.data.model.Member
import com.impraise.supr.game.scenes.domain.SplitIntoGroupsUseCase
import com.impraise.supr.game.scenes.domain.LoadRecursiveMembersUseCase
import com.nhaarman.mockito_kotlin.stub
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations.initMocks

/**
 * Created by guilhermebranco on 3/10/18.
 */
class SplitIntoGroupsUseCaseTest {

    @Mock
    lateinit var recursivePageOfMembersUseCase: LoadRecursiveMembersUseCase

    private lateinit var useCase: SplitIntoGroupsUseCase

    @Before
    fun setup() {
        initMocks(this)
        mock()
        useCase = SplitIntoGroupsUseCase(recursivePageOfMembersUseCase, threshold = 5)
    }

    @Test
    fun shouldSplitListOfMembers() {
        val testObserver = useCase.get(Unit).test()

        testObserver.assertComplete()
        val result = testObserver.values().first() as ResultList.Success
        result.data.numberOfGroupsEqualsTo(3)
        result.data.numberOfMemberEqualsTo(5, 0)
        result.data.numberOfMemberEqualsTo(5, 1)
        result.data.numberOfMemberEqualsTo(2, 2)
    }

    private fun List<List<Member>>.numberOfGroupsEqualsTo(expected: Int) {
        assertEquals(expected, this.size)
    }

    private fun List<List<Member>>.numberOfMemberEqualsTo(expected: Int, groupIndex: Int) {
        assertEquals(expected, this[groupIndex].size)
    }

    private fun mock() {
        val members = (1..12).map { Member(it.toString(), it.toString()) }
        recursivePageOfMembersUseCase.stub {
            on {
                get(Unit)
            }.thenReturn(Single.just(ResultList.Success(members)))
        }
    }
}