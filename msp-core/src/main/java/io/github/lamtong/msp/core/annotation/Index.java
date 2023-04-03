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

import io.github.lamtong.msp.core.properties.Properties;

import java.lang.annotation.*;

/**
 * Annotation which indicates an {@link com.meilisearch.sdk.Index} for {@code MeiliSearch}, annotated with entity
 * classes.
 *
 * @author Lam Tong
 * @version 1.0.0
 * @since 1.0.0.SNAPSHOT
 */
@Documented
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface Index {

    /**
     * Unique identification of {@link com.meilisearch.sdk.Index} object. It is strongly recommended that value should
     * be assigned to a specified value, otherwise class name will be used as default index name. See
     * {@link Properties#useClassNameAsDefault} and {@link Properties#classWithIndex}.
     *
     * @return uid
     */
    String value() default "";

}
