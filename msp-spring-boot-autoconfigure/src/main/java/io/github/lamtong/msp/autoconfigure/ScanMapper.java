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

package io.github.lamtong.msp.autoconfigure;

import io.github.lamtong.msp.core.annotation.Mapper;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Annotation which is enabled to scan {@code Mapper} interfaces which are annotated with {@link Mapper} to generate
 * implementations of these interfaces.
 *
 * @author Lam Tong
 * @version 1.0.0
 * @since 1.0.0.SNAPSHOT
 */
@Documented
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
@Import(value = {MeiliSearchPlusBeanDefinitionRegistrar.class})
public @interface ScanMapper {

    /**
     * Packages' name of {@code Mapper} to scan. If null is set to this field, then package name of type which is
     * annotated with {@link ScanMapper} will be used as default. {@code AliasFor} {@link #basePackages()}.
     *
     * @return packages to scan
     */
    String[] value() default {};

    /**
     * Packages' name of {@code Mapper} to scan. If null is set to this field, then package name of type which is *
     * annotated with {@link ScanMapper} will be used as default. {@code AliasFor} {@link #value()}.
     *
     * @return packages to scan
     */
    String[] basePackages() default {};

    /**
     * Mapper classes to scan.
     *
     * @return classes to scan
     */
    Class<?>[] basePackageClasses() default {};

}
