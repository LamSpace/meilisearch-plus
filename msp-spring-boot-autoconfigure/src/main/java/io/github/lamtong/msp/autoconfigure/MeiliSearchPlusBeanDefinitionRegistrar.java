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

import io.github.lamtong.msp.core.exception.MSPException;
import io.github.lamtong.msp.core.mapper.BaseMapper;
import io.github.lamtong.msp.core.mapper.MapperImplementationFactory;
import io.github.lamtong.msp.core.mapper.MapperScanner;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Bean definition registrar of {@code MeiliSearch-Plus}, registering implementation of {@code Mapper} interface into
 * {@code IOC} container so that mapper implementations can be autowired via {@code @Autowired} or {@code @Resource}.
 *
 * @author Lam Tong
 * @version 1.0.0
 * @see ImportBeanDefinitionRegistrar
 * @since 1.0.0.SNAPSHOT
 */
public class MeiliSearchPlusBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    private static final List<Class<? extends BaseMapper<?>>> MAPPER_CLASSES = new ArrayList<>();

    /**
     * Acquires {@code Mapper} interface classes and returns in a {@link List}.
     *
     * @return {@code Mapper} interface classes in a {@link List}
     */
    public static List<Class<? extends BaseMapper<?>>> getMapperClasses() {
        return Collections.unmodifiableList(MAPPER_CLASSES);
    }

    @Override
    public void registerBeanDefinitions(@Nonnull AnnotationMetadata metadata,
                                        @Nonnull BeanDefinitionRegistry registry) {
        List<String> packageNames = this.extractPackageNames(metadata);
        this.doRegisterBeanDefinitions(packageNames, registry);
    }

    /**
     * Extracts package name from {@link ScanMapper} and returns package names in a {@link List} of {@link String}.
     *
     * @param metadata metadata of class annotated with {@link ScanMapper}
     * @return package name in a {@link List} of {@link String}
     */
    private List<String> extractPackageNames(@Nonnull AnnotationMetadata metadata) {
        String annotationName = ScanMapper.class.getName();
        Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(annotationName, false);
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(annotationAttributes);
        if (attributes == null) {
            throw new MSPException("Fails to register bean definition due to null attributes.");
        }
        String[] values = attributes.getStringArray("value");
        List<String> packageNames = new ArrayList<>(Arrays.asList(values));
        String[] basePackages = attributes.getStringArray("basePackages");
        packageNames.addAll(Arrays.asList(basePackages));
        Class<?>[] basePackageClasses = attributes.getClassArray("basePackageClasses");
        for (Class<?> packageClass : basePackageClasses) {
            packageNames.add(packageClass.getPackage().getName());
        }
        if (packageNames.isEmpty()) {
            packageNames.add(ClassUtils.getPackageName(metadata.getClassName()));
        }
        return Collections.unmodifiableList(packageNames);
    }

    /**
     * Registers bean definition into {@code IOC} container with components from specified packages.
     *
     * @param packageNames package name for component to be registered
     * @param registry     bean definition register
     */
    private void doRegisterBeanDefinitions(List<String> packageNames,
                                           @Nonnull BeanDefinitionRegistry registry) {
        List<Class<? extends BaseMapper<?>>> mapperClasses = MapperScanner.scanMapperClasses(packageNames);
        MAPPER_CLASSES.addAll(mapperClasses);
        List<Class<? extends BaseMapper<?>>> list = MapperImplementationFactory.get(mapperClasses);
        for (Class<? extends BaseMapper<?>> clazz : list) {
            String className = clazz.getSimpleName();
            AbstractBeanDefinition definition = BeanDefinitionBuilder.rootBeanDefinition(clazz).getBeanDefinition();
            registry.registerBeanDefinition(className, definition);
        }
    }

}
