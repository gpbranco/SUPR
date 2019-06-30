package com.impraise.suprdemo.scenes.domain

import com.impraise.supr.game.scenes.data.model.Member
import com.impraise.supr.game.scenes.domain.RoundCreationHelper

class ImageAvailableCondition : RoundCreationHelper.Condition<Member> {

    companion object {
        private val REGEX = "^((?!(image_not_available|.gif)).)*\$".toRegex()
    }

    override fun satisfied(param: Member): Boolean {
        return param.avatarUrl.isNotEmpty()
                && param.avatarUrl.matches(REGEX)
    }
}