package com.impraise.supr.game.scenes.domain

import android.util.Log
import com.impraise.supr.common.Pagination
import com.impraise.supr.data.PaginatedRepository
import com.impraise.supr.data.PaginatedResult
import com.impraise.supr.data.ResultList
import com.impraise.supr.domain.ReactiveUseCase
import com.impraise.supr.game.scenes.data.model.Member
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.*

class LoadRecursiveMembersUseCase(
        private val repository: PaginatedRepository<Member>,
        private val randomPageGenerator: RandomPageGenerator,
        private val numberOfCalls: Int = 5) : ReactiveUseCase<Unit, ResultList<Member>> {

    companion object {
        private const val TAG = "LoadRecursiveMembers"
        private const val PAGE_SIZE = 50
    }

    override fun get(param: Unit): Single<ResultList<Member>> {
        return fetch(CurrentState())
                .flatMap { currentState ->
                    Flowable.range(0, numberOfCalls)
                            .concatMap {
                                if (it == 0) Flowable.just(currentState)
                                else fetch(currentState.copy(count = it)).toFlowable()
                            }
                            .takeUntil { currentState.count == numberOfCalls }
                            .map {
                                currentState.items
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

    private fun fetch(currentState: CurrentState): Single<CurrentState> {
        return repository.fetch(Pagination(PAGE_SIZE, after = randomPageGenerator.randomPage(currentState.total).toString()))
                .map { paginatedResult ->
                    when (paginatedResult) {
                        is PaginatedResult.Success -> {
                            currentState.copy(count = currentState.count, total = paginatedResult.pageDetail.totalCount, items = paginatedResult.data)
                        }
                        is PaginatedResult.Error -> {
                            Log.e(TAG, paginatedResult.error.message)
                            currentState
                        }
                    }
                }.singleOrError()
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