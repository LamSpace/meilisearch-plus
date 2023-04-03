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

import io.github.lamtong.msp.core.annotation.Index;
import io.github.lamtong.msp.core.mapper.BaseMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties of {@code MeiliSearch-Plus} for autoconfiguration.
 *
 * @author Lam Tong
 * @version 1.0.0
 * @since 1.0.0.SNAPSHOT
 */
@ConfigurationProperties(prefix = MeiliSearchPlusProperties.PROPERTIES_PREFIX)
public class MeiliSearchPlusProperties {

    public static final String PROPERTIES_PREFIX = "msp";

    /**
     * Class of generic type parameter of {@link BaseMapper} must be annotated with {@link Index} or not.
     */
    private boolean classWithIndex = true;

    /**
     * Use class name as default {@code Index} name or not where class is annotated with {@link Index} when
     * {@code value} of {@link Index} is empty.
     */
    private boolean useClassNameAsDefault = false;

    /**
     * Automatic-update primary key of {@link com.meilisearch.sdk.Index} when {@code Mapper} interfaces are scanned or
     * not.
     */
    private boolean autoUpdatePrimaryKeyOfIndex = true;

    /**
     * Automatic-update settings of {@link com.meilisearch.sdk.Index} when {@code Mapper} interfaces are scanned or
     * not.
     */
    private boolean autoUpdateSettingsOfIndex = true;

    /**
     * Synchronize operations to {@link com.meilisearch.sdk.Index} or not.
     */
    private boolean synchronizeOperations = false;

    /**
     * Host url of {@code MeiliSearch} server.
     */
    private String hostUrl;

    /**
     * Api key to access {@code MeiliSearch}.
     */
    private String apiKey;

    public boolean isClassWithIndex() {
        return classWithIndex;
    }

    public void setClassWithIndex(boolean classWithIndex) {
        this.classWithIndex = classWithIndex;
    }

    public boolean isUseClassNameAsDefault() {
        return useClassNameAsDefault;
    }

    public void setUseClassNameAsDefault(boolean useClassNameAsDefault) {
        this.useClassNameAsDefault = useClassNameAsDefault;
    }

    public boolean isAutoUpdateSettingsOfIndex() {
        return autoUpdateSettingsOfIndex;
    }

    public void setAutoUpdateSettingsOfIndex(boolean autoUpdateSettingsOfIndex) {
        this.autoUpdateSettingsOfIndex = autoUpdateSettingsOfIndex;
    }

    public boolean isAutoUpdatePrimaryKeyOfIndex() {
        return autoUpdatePrimaryKeyOfIndex;
    }

    public void setAutoUpdatePrimaryKeyOfIndex(boolean autoUpdatePrimaryKeyOfIndex) {
        this.autoUpdatePrimaryKeyOfIndex = autoUpdatePrimaryKeyOfIndex;
    }

    public boolean isSynchronizeOperations() {
        return synchronizeOperations;
    }

    public void setSynchronizeOperations(boolean synchronizeOperations) {
        this.synchronizeOperations = synchronizeOperations;
    }

    public String getHostUrl() {
        return hostUrl;
    }

    public void setHostUrl(String hostUrl) {
        this.hostUrl = hostUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

}
