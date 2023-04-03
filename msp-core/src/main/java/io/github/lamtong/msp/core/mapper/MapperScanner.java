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

package io.github.lamtong.msp.core.mapper;

import io.github.lamtong.msp.core.annotation.Mapper;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * {@code Scanner} of {@code Mapper} interface, which extends {@link BaseMapper}.
 *
 * @author Lam Tong
 * @version 1.0.0
 * @since 1.0.0.SNAPSHOT
 */
public final class MapperScanner {

    private static final Logger logger = LoggerFactory.getLogger(MapperScanner.class);

    private MapperScanner() {
    }

    /**
     * Scans {@code Mapper} interfaces annotated with {@link Mapper} according to specified package name and returns
     * {@code Mapper} classes in a {@link List}.
     *
     * @param packageName specified package name to scan
     * @return {@code Mapper} classes in a {@link List}
     */
    public static List<Class<? extends BaseMapper<?>>> scanMapperClasses(String packageName) {
        List<Class<? extends BaseMapper<?>>> list = new LinkedList<>();
        Reflections reflections = new Reflections(packageName, Scanners.values());
        Set<Class<?>> classSet = reflections.getTypesAnnotatedWith(Mapper.class);
        for (Class<?> next : classSet) {
            //noinspection unchecked
            list.add((Class<? extends BaseMapper<?>>) next);
        }
        if (logger.isInfoEnabled()) {
            logger.info("Scanned mapper classes of package [{}], mapper classes = {}.",
                    packageName, classSet);
        }
        return list;
    }

    /**
     * Scans {@code Mapper} interfaces annotated with {@link Mapper} according to specified package names and returns
     * {@code Mapper} classes in a {@link List}.
     *
     * @param packageNames specified package names in an array to scan
     * @return {@code Mapper} classes in a {@link List}
     * @see #scanMapperClasses(List)
     */
    public static List<Class<? extends BaseMapper<?>>> scanMapperClasses(String... packageNames) {
        return scanMapperClasses(Arrays.asList(packageNames));
    }

    /**
     * Scans {@code Mapper} interfaces annotated with {@link Mapper} according to specified package names and returns
     * {@code Mapper} classes in a {@link List}.
     *
     * @param packageNames specified package names in a {@link List} to scan
     * @return {@code Mapper} classes in a {@link List}
     * @see #scanMapperClasses(String[])
     */
    public static List<Class<? extends BaseMapper<?>>> scanMapperClasses(List<String> packageNames) {
        List<Class<? extends BaseMapper<?>>> list = new ArrayList<>();
        for (String packageName : packageNames) {
            List<Class<? extends BaseMapper<?>>> mapperClasses = scanMapperClasses(packageName);
            list.addAll(mapperClasses);
        }
        return list;
    }

}
