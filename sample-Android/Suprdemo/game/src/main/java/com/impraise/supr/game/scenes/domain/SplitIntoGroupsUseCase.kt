package com.impraise.supr.game.scenes.domain

import android.util.Log
import com.impraise.supr.data.ResultList
import com.impraise.supr.data.either
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

    companion object {
        private const val TAG = "SplitIntoGroupsUseCase"
    }

    override fun get(param: Unit): Single<ResultList<List<Member>>> {
        return loadRecursiveMembersUseCase.get(Unit).toFlowable()
                .flatMap { result ->
                    Flowable.fromIterable(result.either({
                        it.toMutableList().apply {
                            shuffle()
                        }
                    }, {
                        Log.e(TAG, "Error:", it)
                        emptyList<Member>()
                    }))
                }
                .buffer(threshold)
                .toList()
                .map {
                    ResultList.Success(it)
                }
    }
}