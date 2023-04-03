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

import io.github.lamtong.msp.core.exception.MSPException;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simple factory to generate implementations for {@code Mapper} interfaces.
 *
 * @author Lam Tong
 * @version 1.0.0
 * @since 1.0.0.SNAPSHOT
 */
public final class MapperImplementationFactory {

    /**
     * Default Constructor
     */
    private MapperImplementationFactory() {
    }

    /**
     * Acquires implementations of given interfaces and returns them in a {@link List} with specified mapper classes in
     * an array of {@link Class}.
     *
     * @param classes interfaces to generate implementations
     * @return implementations in a {@link List}
     */
    @SuppressWarnings(value = {"unchecked"})
    public static List<Class<? extends BaseMapper<?>>> get(Class<? extends BaseMapper<?>>... classes) {
        return get(Arrays.asList(classes));
    }

    /**
     * Acquires implementations of given interfaces and returns them in a {@link List} with specified mapper classes in
     * a {@link List} of {@link Class}.
     *
     * @param mappers interfaces to generate implementations
     * @return implementations in a {@link List}
     */
    public static List<Class<? extends BaseMapper<?>>> get(List<Class<? extends BaseMapper<?>>> mappers) {
        return mappers.stream()
                .map(MapperImplementationFactory::get)
                .collect(Collectors.toList());
    }

    /**
     * Accepts a mapper interface {@code Class}, which extends {@link BaseMapper}, and generates an implementation of
     * the {@code Mapper} interface.
     * <p/>
     * Before generating an implementation object, mapper interface {@code Class} should be checked as follows:
     * <ol>
     *     <li>throws an exception if and only if mapper interface {@code Class} is not an interface; or</li>
     *     <li>throws an exception if and only if mapper interface {@code Class} does not directly extends
     *     {@link BaseMapper}; or</li>
     *     <li>generates an implementation instance for given {@code Mapper} interface.</li>
     * </ol>
     *
     * @param mapper {@code Class} object, extending {@link BaseMapper} interface directly
     * @param <T>    generic type parameter
     * @return an implementation of given mapper interface
     */
    @NotNull
    @SuppressWarnings(value = {"unchecked"})
    public static <T> Class<T> get(@NotNull Class<T> mapper) {
        if (!mapper.isInterface()) {
            throw new MSPException(String.format("Mapper [%s] is not an interface.",
                    mapper.getSimpleName()));
        }
        boolean match = Stream.of(mapper.getInterfaces())
                .anyMatch(clazz -> clazz.isAssignableFrom(BaseMapper.class));
        if (!match) {
            throw new MSPException(String.format("Mapper [%s] does not extend BaseMapper directly.",
                    mapper.getSimpleName()));
        }
        return (Class<T>) generateClass((Class<? extends BaseMapper<?>>) mapper);
    }

    /**
     * Generates an implementation class based on given {@code Mapper} class, using {@code javassist}, which directly
     * extends {@link BaseMapper}.
     *
     * @param mapper {@code Class} interface which extends {@link BaseMapper}
     * @return an implementation class which implements given {@code Mapper} class.
     */
    private static Class<? extends BaseMapper<?>> generateClass(Class<? extends BaseMapper<?>> mapper) {
        Class<? extends BaseMapper<?>> implClass;
        try {
            String packageName = extractPackageName(mapper);
            String interfaceName = extractInterfaceName(mapper);
            String implClassName = packageName.concat(".").concat(interfaceName).concat("Impl");
            ClassPool classPool = ClassPool.getDefault();
            CtClass impl = classPool.makeClass(implClassName);
            impl.setInterfaces(new CtClass[]{classPool.get(mapper.getName())});
            //noinspection unchecked
            implClass = (Class<? extends BaseMapper<?>>) impl.toClass();
        } catch (NotFoundException | CannotCompileException e) {
            throw new MSPException(String.format("Error while generating implementation of Interface [%s], cause: %s.",
                    mapper.getSimpleName(), e.getMessage()));
        }
        return implClass;
    }

    /**
     * Extracts package name for given {@code Mapper} class, which extends {@link BaseMapper}.
     *
     * @param mapper {@code Class} instance which extends {@link BaseMapper}
     * @return package name for given {@code Class}
     */
    @NotNull
    private static String extractPackageName(@NotNull Class<? extends BaseMapper<?>> mapper) {
        return mapper.getPackage().getName();
    }

    /**
     * Extracts interface name for given {@code Mapper} class, which extends {@link BaseMapper}.
     *
     * @param mapper {@code Class} instance which extends {@link BaseMapper}
     * @return interface name for given {@code Class}
     */
    @NotNull
    @Contract(pure = true)
    private static String extractInterfaceName(@NotNull Class<? extends BaseMapper<?>> mapper) {
        return mapper.getSimpleName();
    }

}
