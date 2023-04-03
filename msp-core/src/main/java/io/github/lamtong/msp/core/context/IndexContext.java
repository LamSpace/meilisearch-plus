/*
Copyright 2023 the original author, Lam Tong

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package io.github.lamtong.msp.core.context;

import com.meilisearch.sdk.Index;
import io.github.lamtong.msp.core.mapper.BaseMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Context which holds mapping relation between {@code Mapper} and {@link Index} instance.
 *
 * @author Lam Tong
 * @version 1.0.0
 * @see Index
 * @see BaseMapper
 * @since 1.0.0.SNAPSHOT
 */
public final class IndexContext {

    @SuppressWarnings(value = {"rawtypes"})
    private static final Map<Class<? extends BaseMapper>, Index> MAPPER = new ConcurrentHashMap<>();

    private IndexContext() {
    }

    /**
     * Binds an {@link Index} instance to a specified {@link Class}, which extends {@link BaseMapper}.
     *
     * @param clazz {@code Class} to bind
     * @param index {@link Index} instance to bind
     */
    @SuppressWarnings(value = {"rawtypes"})
    public static void set(Class<? extends BaseMapper> clazz,
                           Index index) {
        MAPPER.put(clazz, index);
    }

    /**
     * Acquires an {@link Index} instance according to a specified {@code Class} instance, which extends
     * {@link BaseMapper}.
     *
     * @param clazz specified {@code Class}
     * @return {@link Index} instance binding with the class
     */
    @SuppressWarnings(value = {"rawtypes"})
    public static Index get(Class<? extends BaseMapper> clazz) {
        return MAPPER.get(clazz);
    }

}
