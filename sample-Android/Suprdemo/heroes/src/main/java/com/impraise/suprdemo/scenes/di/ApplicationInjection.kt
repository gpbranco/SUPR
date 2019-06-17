package com.impraise.suprdemo.scenes.di

import com.impraise.supr.data.PaginatedRepository
import com.impraise.supr.data.ResultList
import com.impraise.supr.domain.ReactiveUseCase
import com.impraise.supr.game.scenes.data.model.Member
import com.impraise.supr.game.scenes.domain.*
import com.impraise.supr.game.scenes.presentation.GamePresenter
import com.impraise.supr.game.scenes.presentation.GameScene
import com.impraise.suprdemo.scenes.data.MarvelApiRepository
import com.impraise.suprdemo.scenes.data.NetworkInterceptor
import com.impraise.suprdemo.scenes.domain.ImageAvailableCondition
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.Module
import org.koin.dsl.module.module
import java.lang.ref.WeakReference

val applicationComponent: List<Module>
    get() = listOf(gameModule)


private val gameModule = module {
    viewModel {
        GameScene(get(), get())
    }
    factory { CreateGameUseCase(get(), get(), get()) }
    factory { CreateRoundUseCase(roundCreationHelper = get()) }
    factory<ReactiveUseCase<Unit, ResultList<List<Member>>>> { LoadRandomPageOfMembersUseCase(get()) }
    factory<PaginatedRepository<Member>> { MarvelApiRepository(get()) }
    factory { GamePresenter(WeakReference(get())) }
    single { RoundCreationHelper(get()) }
    single { GameCreationHelper(get()) }
    single {
        OkHttpClient.Builder()
                .addInterceptor(NetworkInterceptor())
                .addInterceptor(get())
                .build()
    }
    single<Interceptor> { HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    single<RoundCreationHelper.Condition<Member>> { ImageAvailableCondition() }
}