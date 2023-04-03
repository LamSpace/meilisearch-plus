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

package io.github.lamtong.msp.core.properties;

import io.github.lamtong.msp.core.annotation.Index;
import io.github.lamtong.msp.core.mapper.BaseMapper;

/**
 * Core properties of {@code MeiliSearch-Plus}.
 *
 * @author Lam Tong
 * @version 1.0.0
 * @since 1.0.0.SNAPSHOT
 */
public final class Properties {

    private static final Properties INSTANCE = new Properties();

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

    private Properties() {
    }

    public static Properties getProperties() {
        return INSTANCE;
    }

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

    public boolean isAutoUpdatePrimaryKeyOfIndex() {
        return autoUpdatePrimaryKeyOfIndex;
    }

    public void setAutoUpdatePrimaryKeyOfIndex(boolean autoUpdatePrimaryKeyOfIndex) {
        this.autoUpdatePrimaryKeyOfIndex = autoUpdatePrimaryKeyOfIndex;
    }

    public boolean isAutoUpdateSettingsOfIndex() {
        return autoUpdateSettingsOfIndex;
    }

    public void setAutoUpdateSettingsOfIndex(boolean autoUpdateSettingsOfIndex) {
        this.autoUpdateSettingsOfIndex = autoUpdateSettingsOfIndex;
    }

}
