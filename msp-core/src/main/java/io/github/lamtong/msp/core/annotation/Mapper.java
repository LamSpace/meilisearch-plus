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

package io.github.lamtong.msp.core.annotation;

import io.github.lamtong.msp.core.mapper.BaseMapper;

import java.lang.annotation.*;

/**
 * {@code Annotation} which indicates {@code Mapper} interface for {@code MeiliSearch}.
 * <h2>Usage</h2>
 * {@link Mapper} should be annotated with interfaces which extend {@link BaseMapper} to inherit
 * <strong>CRUD</strong> capacities. To some degree, {@link Mapper} works the same as {@code @Mapper} annotation in
 * {@code Mybatis}.
 *
 * @author Lam Tong
 * @version 1.0.0
 * @since 1.0.0.SNAPSHOT
 */
@Documented
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface Mapper {
}
