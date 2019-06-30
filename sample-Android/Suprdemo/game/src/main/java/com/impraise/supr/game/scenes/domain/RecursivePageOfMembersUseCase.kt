package com.impraise.supr.game.scenes.domain

import com.impraise.supr.common.Pagination
import com.impraise.supr.data.PaginatedRepository
import com.impraise.supr.data.PaginatedResult
import com.impraise.supr.data.ResultList
import com.impraise.supr.domain.ReactiveUseCase
import com.impraise.supr.game.scenes.data.model.Member
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.*

class RecursivePageOfMembersUseCase(
        private val repository: PaginatedRepository<Member>,
        private val randomPageGenerator: RandomPageGenerator,
        private val numberOfCalls: Int = 5) : ReactiveUseCase<Unit, ResultList<Member>> {

    override fun get(param: Unit): Single<ResultList<Member>> {
        var currentState = CurrentState()
        return Flowable.range(0, numberOfCalls)
                .concatMap {
                    repository.fetch(Pagination(50, after = randomPageGenerator.randomPage(currentState.total).toString()))
                            .doOnNext {
                                val totalCount = when (it) {
                                    is PaginatedResult.Success -> it.pageDetail.totalCount
                                    else -> currentState.total
                                }
                                currentState = currentState.copy(count = currentState.count + 1, total = totalCount)
                            }
                }
                .takeUntil { currentState.count == numberOfCalls }
                .map {
                    when (it) {
                        is PaginatedResult.Success -> {
                            it.data
                        }

                        is PaginatedResult.Error -> emptyList()
                    }
                }
                .reduce(mutableListOf<Member>()) { combined, next ->
                    combined.apply {
                        addAll(next)
                    }
                }
                .map {
                    ResultList.Success(it)
                }
    }
}

data class CurrentState(val count: Int = 0, val total: Int = 0, val items: List<Member> = emptyList())

class RandomPageGeneratorDefault : RandomPageGenerator {

    override fun randomPage(max: Int): Int {
        return if (max <= 0) 0
        else (0..max).random()
    }

    private fun IntRange.random() =
            Random().nextInt((endInclusive + 1) - start) + start

}