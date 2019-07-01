package com.impraise.supr.game.domain

import com.impraise.supr.common.Pagination
import com.impraise.supr.data.PageDetail
import com.impraise.supr.data.PaginatedRepository
import com.impraise.supr.data.PaginatedResult
import com.impraise.supr.data.ResultList
import com.impraise.supr.game.scenes.data.model.Member
import com.impraise.supr.game.scenes.domain.LoadRecursiveMembersUseCase
import com.impraise.supr.game.scenes.domain.RandomPageGenerator
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Flowable
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*

class LoadRecursiveUseCaseTest {

    @Mock
    lateinit var repository: PaginatedRepository<Member>

    @Mock
    lateinit var randomPageGenerator: RandomPageGenerator

    private lateinit var useCase: LoadRecursiveMembersUseCase

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        clearInvocations(repository)
        mockRepositoryCalls()
        useCase = LoadRecursiveMembersUseCase(repository, randomPageGenerator = randomPageGenerator)
    }

    @Test
    fun `performs calls until reaches the limit`() {
        useCase.get(Unit)
                .test().assertComplete()

        verify(repository, times(5)).fetch(any())
    }

    @Test
    fun `combines items from all the calls`() {
        val testObserver = useCase.get(Unit).test()
        testObserver.assertValue {
            (it as? ResultList.Success)?.let { result ->
                result.data.size == 5
            } ?: false
        }
    }

    @Test
    fun `uses RandomPageGenerator for every call`() {
        useCase.get(Unit)
                .test().assertComplete()

        verify(randomPageGenerator, times(5)).randomPage(any())
    }

    @Test
    fun `after first call uses totalCount from server as max value for random pages`() {
        val firstPageResult = PageDetail(hasNextPage = true, totalCount = 30)
        repository.stub {
            on { fetch(any()) }.thenReturn(Flowable.just(
                    PaginatedResult.Success(listOf(Member("", "")), firstPageResult)))
        }

        useCase = LoadRecursiveMembersUseCase(repository, randomPageGenerator = randomPageGenerator)

        useCase.get(Unit).test()

        val captor = argumentCaptor<Int>()

        verify(randomPageGenerator, atLeastOnce()).randomPage(captor.capture())

        Assert.assertEquals(0, captor.firstValue)
        Assert.assertEquals(30, captor.secondValue)
    }

    @Test
    fun `result combines all data returned from every call`() {
        val testObserver = useCase.get(Unit).test()
        testObserver.assertValue {
            val success = it as ResultList.Success
            success.data.containsAll((0..4).map { member -> Member(member.toString(), member.toString()) })
        }
    }

    private fun mockRepositoryCalls() {
        (0..4).forEach { count ->
            repository.stub {
                on { fetch(Pagination(50, count.toString())) }.thenReturn(Flowable.just(
                        PaginatedResult.Success(listOf(Member(count.toString(), count.toString())), PageDetail(totalCount = count + 1))))
            }
        }

        randomPageGenerator = spy(object : RandomPageGenerator {

            private val values = ArrayDeque<Int>().apply {
                (0..6).forEach {
                    this.add(it)
                }
            }

            override fun randomPage(max: Int): Int {
                return values.pop()
            }
        })
    }
}