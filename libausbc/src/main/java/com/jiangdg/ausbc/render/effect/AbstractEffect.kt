/*
 * Copyright 2017-2023 Jiangdg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jiangdg.ausbc.render.effect

import android.content.Context
import com.jiangdg.ausbc.render.internal.AbstractFboRender

/** 抽象效果类，从AbstractFboRender扩展而来
 *
 * @author Created by jiangdg on 2022/1/26
 */
abstract class AbstractEffect(ctx: Context) : AbstractFboRender(ctx) {

    /**
     * 获取效果id
     *
     * @return effect id
     */
    abstract fun getId(): Int

    /**
     * 获取分类id
     *
     * @return effect classify id
     */
    abstract fun getClassifyId(): Int
}