package com.impraise.supr.game.scenes.domain

import com.impraise.supr.data.ResultList
import com.impraise.supr.domain.ReactiveUseCase
import com.impraise.supr.game.scenes.data.model.Member
import io.reactivex.Flowable
import io.reactivex.Single

/**
 * Created by guilhermebranco on 3/10/18.
 */
class SplitIntoGroupsUseCase(
        private val loadRecursiveMembersUseCase: LoadRecursiveMembersUseCase,
        private val threshold: Int = 5) : ReactiveUseCase<Unit, ResultList<List<Member>>> {

    override fun get(param: Unit): Single<ResultList<List<Member>>> {
        return loadRecursiveMembersUseCase.get(Unit).toFlowable()
                .flatMap { result ->
                    when (result) {
                        is ResultList.Success -> {
                            val members = result.data.toMutableList()
                            members.shuffle()
                            Flowable.fromIterable(members)
                        }
                        is ResultList.Error -> Flowable.fromIterable(emptyList())
                    }
                }
                .buffer(threshold)
                .toList()
                .map {
                    ResultList.Success(it)
                }
    }
}