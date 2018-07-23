/*
 * Copyright 2018 Google, Inc.
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

package io.plaidapp.core.designernews.data.stories

import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.data.stories.model.Story
import retrofit2.Response
import java.io.IOException

/**
 * Data source class that handles work with Designer News API.
 */
class StoriesRemoteDataSource(private val service: DesignerNewsService) {

    suspend fun loadTopStories(page: Int): Result<List<Story>> {
        val response = service.getTopStories(page).await()
        return getResult(response) {
            Result.Error(
                IOException("Error getting top stories ${response.code()} ${response.message()}")
            )
        }
    }

    suspend fun search(query: String, page: Int): Result<List<Story>> {
        val response = service.search(query, page).await()
        return getResult(response) {
            Result.Error(
                IOException("Error searching $query ${response.code()} ${response.message()}")
            )
        }
    }

    private fun getResult(
        response: Response<List<Story>>,
        onError: () -> Result.Error
    ): Result<List<Story>> {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                return Result.Success(body)
            }
        }
        return onError.invoke()
    }

    companion object {
        @Volatile
        private var INSTANCE: StoriesRemoteDataSource? = null

        fun getInstance(service: DesignerNewsService): StoriesRemoteDataSource {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StoriesRemoteDataSource(service).also { INSTANCE = it }
            }
        }
    }
}