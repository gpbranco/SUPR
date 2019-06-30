package com.impraise.supr.game.scenes.domain

interface RandomPageGenerator {

    fun randomPage(max: Int = 0): Int
}