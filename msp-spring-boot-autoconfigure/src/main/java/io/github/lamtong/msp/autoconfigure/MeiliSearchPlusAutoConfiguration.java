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

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Config;
import com.meilisearch.sdk.json.JacksonJsonHandler;
import io.github.lamtong.msp.core.context.ClientContext;
import io.github.lamtong.msp.core.mapper.BaseMapper;
import io.github.lamtong.msp.core.mapper.MapperImplementationFactory;
import io.github.lamtong.msp.core.mapper.MapperScanner;
import io.github.lamtong.msp.core.mapper.MapperValidator;
import io.github.lamtong.msp.core.properties.Properties;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnJava;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.system.JavaVersion;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;

/**
 * {@code Autoconfiguration} of {@code MeiliSearch-Plus}.
 *
 * @author Lam Tong
 * @version 1.0.0
 * @since 1.0.0.SNAPSHOT
 */
@AutoConfiguration
@EnableConfigurationProperties(value = MeiliSearchPlusProperties.class)
@ConditionalOnClass(value = {MapperScanner.class, MapperImplementationFactory.class, MapperValidator.class})
@ConditionalOnJava(value = JavaVersion.EIGHT)
public class MeiliSearchPlusAutoConfiguration implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(MeiliSearchPlusAutoConfiguration.class);

    private final MeiliSearchPlusProperties properties;

    private ApplicationContext applicationContext;

    public MeiliSearchPlusAutoConfiguration(MeiliSearchPlusProperties properties) {
        this.properties = properties;
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.postHandle();
    }

    /**
     * Post handle of {@link MeiliSearchPlusAutoConfiguration}.
     */
    private void postHandle() {
        this.setClient();
        this.flushProperties();
        this.validateMappers();
    }

    /**
     * Sets {@code Client} instance of {@code MeiliSearch}.
     */
    private void setClient() {
        String hostUrl = this.properties.getHostUrl();
        String apiKey = this.properties.getApiKey();
        if (hostUrl == null || "".equals(hostUrl.trim())) {
            throw new IllegalArgumentException("MeiliSearch host url is null or empty.");
        }
        if (apiKey == null || "".equals(apiKey.trim())) {
            throw new IllegalArgumentException("MeiliSearch api key is null or empty.");
        }
        Config config = new Config(hostUrl, apiKey, new JacksonJsonHandler());
        Client client = new Client(config);
        ClientContext.setClient(client);
        if (logger.isInfoEnabled()) {
            logger.info("MeiliSearch client has been set.");
        }
    }

    /**
     * Flushes properties of {@link MeiliSearchPlusProperties} to {@link Properties}.
     */
    private void flushProperties() {
        Properties prop = Properties.getProperties();
        prop.setClassWithIndex(this.properties.isClassWithIndex());
        prop.setUseClassNameAsDefault(this.properties.isUseClassNameAsDefault());
        prop.setAutoUpdateSettingsOfIndex(this.properties.isAutoUpdateSettingsOfIndex());
        prop.setAutoUpdatePrimaryKeyOfIndex(this.properties.isAutoUpdatePrimaryKeyOfIndex());
        prop.setSynchronizeOperations(this.properties.isSynchronizeOperations());
        if (logger.isInfoEnabled()) {
            logger.info("Properties has been flushed.");
        }
    }

    /**
     * Validates {@code Mapper} interface classes with corresponding implementation initialize by {@code Spring}.
     */
    private void validateMappers() {
        List<Class<? extends BaseMapper<?>>> mapperClasses = MeiliSearchPlusBeanDefinitionRegistrar.getMapperClasses();
        for (Class<? extends BaseMapper<?>> mapperClass : mapperClasses) {
            BaseMapper<?> bean = this.applicationContext.getBean(mapperClass);
            MapperValidator.validate(mapperClass, bean);
            if (logger.isInfoEnabled()) {
                logger.info("Mapper [{}] has been validated with implementation [{}].",
                        mapperClass.getName(), bean.getClass().getName());
            }
        }
    }

}
