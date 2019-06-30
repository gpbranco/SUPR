package com.impraise.supr.game.domain

import com.impraise.supr.data.PageDetail
import com.impraise.supr.data.PaginatedRepository
import com.impraise.supr.data.PaginatedResult
import com.impraise.supr.data.ResultList
import com.impraise.supr.game.scenes.data.model.Member
import com.impraise.supr.game.scenes.domain.RandomPageGenerator
import com.impraise.supr.game.scenes.domain.RecursivePageOfMembersUseCase
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Flowable
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class LoadRecursiveUseCaseTest {

    @Mock
    lateinit var repository: PaginatedRepository<Member>

    @Mock
    lateinit var randomPageGenerator: RandomPageGenerator

    private lateinit var useCase: RecursivePageOfMembersUseCase

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        clearInvocations(repository)
        repository.stub {
            on { fetch(any()) }.thenReturn(Flowable.just(
                    PaginatedResult.Success(listOf(Member("", "")), PageDetail())))
        }

        randomPageGenerator.stub {
            on {
                randomPage(any())
            }.thenReturn(1)
        }
        useCase = RecursivePageOfMembersUseCase(repository, randomPageGenerator = randomPageGenerator)
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

        useCase = RecursivePageOfMembersUseCase(repository, randomPageGenerator = randomPageGenerator)

        useCase.get(Unit).test()

        val captor = argumentCaptor<Int>()

        verify(randomPageGenerator, atLeastOnce()).randomPage(captor.capture())

        Assert.assertEquals(0, captor.firstValue)
        Assert.assertEquals(30, captor.secondValue)

        /*argumentCaptor<Pagination> {
            verify(repository) {
                this.mock.fetch(capture())
            }
            Assert.assertNotNull(firstValue)
        }

        argumentCaptor<Int> {
            verify(randomPageGenerator) {
                this.mock.randomPage(capture())
            }
            Assert.assertEquals(0, firstValue)
            Assert.assertEquals(30, secondValue)
        }*/
    }
}