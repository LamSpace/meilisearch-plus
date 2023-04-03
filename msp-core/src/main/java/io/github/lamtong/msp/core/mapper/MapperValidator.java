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

import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.exceptions.MeilisearchException;
import com.meilisearch.sdk.model.Settings;
import com.meilisearch.sdk.model.TaskInfo;
import io.github.lamtong.msp.core.annotation.*;
import io.github.lamtong.msp.core.context.ClientContext;
import io.github.lamtong.msp.core.context.IndexContext;
import io.github.lamtong.msp.core.exception.MSPException;
import io.github.lamtong.msp.core.properties.Properties;
import org.reflections.ReflectionUtils;
import org.reflections.util.ReflectionUtilsPredicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Validator to validate {@code Mapper} interfaces defined by programmers externally with implementation itself,
 * checking the generic type parameter of {@link BaseMapper} when extending it.
 *
 * @author Lam Tong
 * @version 1.0.0
 * @see BaseMapper
 * @since 1.0.0.SNAPSHOT
 */
public final class MapperValidator {

    private static final Logger logger = LoggerFactory.getLogger(MapperValidator.class);

    private static final ThreadLocal<Boolean> PRI_KEY_ANNO = new ThreadLocal<>();

    private static final ThreadLocal<String> INDEX_UID = new ThreadLocal<>();

    private static final ThreadLocal<String> PRI_KEY_FIELD = new ThreadLocal<>();

    private MapperValidator() {
    }

    /**
     * Validates {@code Mapper} interface. More precisely, validates the generic type parameter of {@link BaseMapper}
     * when {@code Mapper} interface extending that interface. If validation succeed, binds implementation of that
     * {@code Mapper} interface with corresponded {@link Index} instance.
     *
     * @param clazz          {@code Mapper} interface class, extending {@link BaseMapper}
     * @param implementation {@code Mapper} interface implementation
     */
    public static void validate(Class<? extends BaseMapper<?>> clazz, BaseMapper<?> implementation) {
        List<Type> typeList = Stream.of(clazz.getGenericInterfaces())
                .filter(type -> type.getTypeName().startsWith(BaseMapper.class.getName()))
                .collect(Collectors.toList());
        Type baseMapperType = typeList.get(0);
        try {
            Type[] actualTypeArguments = ((ParameterizedTypeImpl) baseMapperType).getActualTypeArguments();
            Class<?> actualTypeArgument = (Class<?>) actualTypeArguments[0];
            com.meilisearch.sdk.Index index = doValidate(actualTypeArgument);
            IndexContext.set(implementation.getClass(), index);
        } catch (ClassCastException e) {
            throw new MSPException(String.format("[%s] may not specify generic type of [%s].",
                    clazz.getSimpleName(), baseMapperType.getTypeName()));
        } finally {
            PRI_KEY_ANNO.remove();
            INDEX_UID.remove();
            PRI_KEY_FIELD.remove();
        }
    }

    /**
     * <p/>
     * Validates generic type parameter of {@link BaseMapper} when extending that interface, which must:
     * <ul>
     *     <li>annotated with annotation {@link Index}; and</li>
     *     <li>there exists only one field which is annotated with {@link PrimaryKey} or whose name ends with {@code id}
     *     in a case-insensitive case.</li>
     * </ul>
     * When validation succeeds, updates primary key of {@link Index}.
     *
     * @param argumentClass generic type parameter of {@link BaseMapper} to be validated
     * @return {@link com.meilisearch.sdk.Index} instance
     * @throws MSPException if and only if validation fails
     */
    private static com.meilisearch.sdk.Index doValidate(Class<?> argumentClass) throws MSPException {
        Properties properties = Properties.getProperties();
        boolean classWithIndex = properties.isClassWithIndex();
        if (!doValidateTypeAnnotation(argumentClass)) {
            if (classWithIndex) {
                throw new MSPException(String.format("Class [%s] does not annotated with Annotation [@Index].",
                        argumentClass.getSimpleName()));
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Class {} is not annotated with [@Index], it is recommended to annotate this class with [@Index].",
                            argumentClass.getSimpleName());
                }
            }
        }
        postValidateTypeAnnotation(argumentClass);
        if (!doValidateTypeFields(argumentClass)) {
            throw new MSPException(String.format("Class [%s] must provide field ending with 'id' in a case-insensitive case" +
                    " or field annotated with [@PrimaryKey].", argumentClass.getSimpleName()));
        }
        postValidateTypeFields(argumentClass);
        return updateIndexInfo(argumentClass);
    }

    /**
     * Validates whether generic type parameter of {@link BaseMapper} is annotated with {@link Index}.
     *
     * @param argumentClass generic type parameter of {@link BaseMapper} to be validated
     * @return true if and only if generic type parameter of {@link BaseMapper} is annotated with {@link Index};
     * otherwise returns false.
     */
    private static boolean doValidateTypeAnnotation(Class<?> argumentClass) {
        //noinspection unchecked
        return ReflectionUtils.getAnnotations(argumentClass)
                .stream()
                .anyMatch(annotation -> annotation.annotationType().equals(Index.class));
    }

    /**
     * Extracts value of {@link Index} annotation which is annotated on generic type parameter of {@link BaseMapper} and
     * stores it in a {@link ThreadLocal} variable.
     *
     * @param argumentClass generic type parameter of {@link BaseMapper}
     */
    private static void postValidateTypeAnnotation(Class<?> argumentClass) {
        Properties properties = Properties.getProperties();
        boolean classWithIndex = properties.isClassWithIndex();
        boolean useClassNameAsDefault = properties.isUseClassNameAsDefault();
        if (classWithIndex) {
            Index indexAnnotation = extractTypeAnnotation(argumentClass);
            String indexName = indexAnnotation.value();
            if ("".equals(indexName)) {
                if (useClassNameAsDefault) {
                    String classSimpleName = argumentClass.getSimpleName();
                    indexName = decapitalize(classSimpleName);
                } else {
                    throw new MSPException(String.format("[@Index] annotated on class [%s] must assign a not-empty value.",
                            argumentClass.getSimpleName()));
                }
            }
            INDEX_UID.set(indexName);
        } else {
            String classSimpleName = argumentClass.getSimpleName();
            String defaultIndexName = decapitalize(classSimpleName);
            INDEX_UID.set(defaultIndexName);
        }
    }

    /**
     * Extracts an instance of {@link Index}, which is annotated on generic type parameter of {@link BaseMapper}.
     *
     * @param argumentClass generic type parameter of {@link BaseMapper} to be validated
     * @return {@link Index} instance
     */
    private static Index extractTypeAnnotation(Class<?> argumentClass) {
        //noinspection unchecked
        return ((Index) ReflectionUtils.getAnnotations(argumentClass)
                .stream()
                .filter(annotation -> annotation.annotationType().equals(Index.class))
                .collect(Collectors.toList()).get(0));
    }

    /**
     * Validates fields of generic type parameters of {@link BaseMapper} under any one of cases as below:
     * <ul>
     *     <li>there exists only one field annotated with {@link PrimaryKey}; or</li>
     *     <li>there exists only one field whose name ends with {@code id} in a case-insensitive case.</li>
     * </ul>
     * The former case will be taken into consideration with high priority. If no field is annotated with {@link PrimaryKey},
     * the latter case will be considered.
     *
     * @param argumentClass generic type parameter of {@link BaseMapper}
     * @return true if and only if there exists a primary key for given class; otherwise return false.
     */
    private static boolean doValidateTypeFields(Class<?> argumentClass) {
        //noinspection unchecked
        Optional<Field> annotatedField = ReflectionUtils.getFields(argumentClass,
                        ReflectionUtilsPredicates.withAnnotation(PrimaryKey.class))
                .stream()
                .findAny();
        if (annotatedField.isPresent()) {
            PRI_KEY_ANNO.set(true);
            return true;
        }
        PRI_KEY_ANNO.set(false);
        //noinspection unchecked
        return ReflectionUtils.getFields(argumentClass)
                .stream()
                .map(Field::getName)
                .anyMatch(s -> s.toLowerCase().endsWith("id"));
    }

    /**
     * Extracts field of primary key of given class and stores the name of that in a {@link ThreadLocal} variable.
     *
     * @param argumentClass generic type parameter of {@link BaseMapper}
     */
    private static void postValidateTypeFields(Class<?> argumentClass) {
        String primaryKeyField;
        if (PRI_KEY_ANNO.get()) {
            //noinspection unchecked
            Set<Field> fields = ReflectionUtils.getFields(argumentClass,
                    ReflectionUtilsPredicates.withAnnotation(PrimaryKey.class));
            // if there exists multiple fields annotated with @PrimaryKey, throws an exception
            if (fields.size() > 1) {
                throw new MSPException(String.format("Class [%s] provides multiple fields annotated with [@PrimaryKey].",
                        argumentClass.getSimpleName()));
            } else {
                primaryKeyField = fields.iterator().next().getName();
            }
        } else {
            //noinspection unchecked
            List<Field> idFields = ReflectionUtils.getFields(argumentClass)
                    .stream()
                    .filter(field -> field.getName().toLowerCase().endsWith("id"))
                    .collect(Collectors.toList());
            // if there exists multiple fields ended with 'id' in a case-insensitive case, throws an exception
            if (idFields.size() > 1) {
                throw new MSPException(String.format("Class [%s] provides multiple fields ended with 'id' in a case-insensitive manner.",
                        argumentClass.getSimpleName()));
            } else {
                primaryKeyField = idFields.get(0).getName();
            }
        }
        PRI_KEY_FIELD.set(primaryKeyField);
    }

    /**
     * Updates primary key of related {@link Index} and acquires an instance of {@link Index}.
     *
     * @param argumentClass generic type parameter of {@link BaseMapper}
     * @return {@link com.meilisearch.sdk.Index} instance
     */
    private static com.meilisearch.sdk.Index updateIndexInfo(Class<?> argumentClass) {
        com.meilisearch.sdk.Index index;
        TaskInfo taskInfo;
        String indexUid = INDEX_UID.get();
        String primaryKeyField = PRI_KEY_FIELD.get();
        try {
            Properties properties = Properties.getProperties();
            boolean autoUpdatePrimaryKeyOfIndex = properties.isAutoUpdatePrimaryKeyOfIndex();
            if (autoUpdatePrimaryKeyOfIndex) {
                Client client = ClientContext.getClient();
                boolean match = Stream.of(client.getIndexes().getResults())
                        .anyMatch(i -> i.getUid().equals(indexUid));
                if (match) {
                    // update primary key with an existing index
                    // task may fail due to existing documents in the index
                    taskInfo = client.updateIndex(indexUid, primaryKeyField);
                } else {
                    // create an index with specified primary key
                    taskInfo = client.createIndex(indexUid, primaryKeyField);
                }
                TimeUnit.MILLISECONDS.sleep(100);
                String status = client.getTask(taskInfo.getTaskUid()).getStatus();
                if (logger.isInfoEnabled()) {
                    logger.info("Index [{}] updates primary key [{}], taskUid = [{}], status = [{}].",
                            indexUid, primaryKeyField, taskInfo.getTaskUid(), status);
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Unable to update primary key of index [{}] cause autoUpdatePrimaryKeyOfIndex is false. " +
                            "Primary key of index [{}] would be updated manually.", indexUid, indexUid);
                }
            }
            index = updateIndexSettings(argumentClass);
        } catch (MeilisearchException | InterruptedException e) {
            throw new MSPException(e);
        } finally {
            INDEX_UID.remove();
            PRI_KEY_FIELD.remove();
        }
        return index;
    }

    /**
     * Updates settings of {@link com.meilisearch.sdk.Index} and returns instance of that.
     *
     * @param argumentClass generic type parameter of {@link BaseMapper}
     * @return {@link com.meilisearch.sdk.Index} instance.
     */
    private static com.meilisearch.sdk.Index updateIndexSettings(Class<?> argumentClass) {
        Properties properties = Properties.getProperties();
        boolean autoUpdateSettingsOfIndex = properties.isAutoUpdateSettingsOfIndex();
        com.meilisearch.sdk.Index index;
        String indexUid = INDEX_UID.get();
        try {
            Client client = ClientContext.getClient();
            index = client.getIndex(indexUid);
            if (autoUpdateSettingsOfIndex) {
                String[] distinctAttribute = extractDistinctAttribute(argumentClass);
                String[] displayedAttributes = extractDisplayedAttributes(argumentClass);
                String[] filterableAttributes = extractFilterableAttributes(argumentClass);
                String[] searchableAttributes = extractSearchableAttributes(argumentClass);
                String[] sortableAttributes = extractSortableAttributes(argumentClass);
                String[] stopWordAttributes = extractStopWordAttributes(argumentClass);
                Settings settings = new Settings();
                if (distinctAttribute.length != 0) {
                    settings.setDistinctAttribute(distinctAttribute[0]);
                }
                if (displayedAttributes.length != 0) {
                    settings.setDisplayedAttributes(displayedAttributes);
                }
                if (filterableAttributes.length != 0) {
                    settings.setFilterableAttributes(filterableAttributes);
                }
                if (searchableAttributes.length != 0) {
                    settings.setSearchableAttributes(searchableAttributes);
                }
                if (sortableAttributes.length != 0) {
                    settings.setSortableAttributes(sortableAttributes);
                }
                if (stopWordAttributes.length != 0) {
                    settings.setStopWords(stopWordAttributes);
                }
                index.updateSettings(settings);
                if (logger.isInfoEnabled()) {
                    logger.info("Settings of index [{}] have been updated.", index.getUid());
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Unable to update settings of index [{}] cause autoUpdateSettingsOfIndex is false. " +
                            "Settings of index [{}] would be updated manually.", index.getUid(), index.getUid());
                }
            }
        } catch (MeilisearchException | MSPException e) {
            throw new MSPException(e);
        }
        return index;
    }

    /**
     * Extracts distinct attribute of given class and returns field's name in an array pf {@link String}.
     *
     * @param argumentClass generic type parameter of {@link BaseMapper}
     * @return distinct attribute in an array of {@link String}
     */
    private static String[] extractDistinctAttribute(Class<?> argumentClass) {
        String[] strings = extractFields(argumentClass, Distinct.class);
        if (strings.length > 1) {
            throw new MSPException(String.format("Class [%s] contains multiple fields annotated with [@Distinct], please check.",
                    argumentClass.getSimpleName()));
        }
        return strings;
    }

    /**
     * Extracts displayed attributes of given class and returns fields' name in an array of {@link String}.
     *
     * @param argumentClass generic type parameter of {@link BaseMapper}
     * @return displayed attributes in an array of {@link String}
     */
    private static String[] extractDisplayedAttributes(Class<?> argumentClass) {
        return extractFields(argumentClass, Displayed.class);
    }

    /**
     * Extracts filterable attributes of given class and returns fields' name in an array of {@link String}.
     *
     * @param argumentClass generic type parameter of {@link BaseMapper}
     * @return filterable attributes in an array of {@link String}
     */
    private static String[] extractFilterableAttributes(Class<?> argumentClass) {
        return extractFields(argumentClass, Filterable.class);
    }

    /**
     * Extracts searchable attributes of given class and returns fields' name in an array of {@link String}.
     *
     * @param argumentClass generic type parameter of {@link BaseMapper}
     * @return searchable attributes in an array of {@link String}
     */
    private static String[] extractSearchableAttributes(Class<?> argumentClass) {
        return extractFields(argumentClass, Searchable.class);
    }

    /**
     * Extracts sortable attributes of given class and returns fields' name in an array of {@link String}.
     *
     * @param argumentClass generic type parameter of {@link BaseMapper}
     * @return sortable attributes in an array of {@link String}
     */
    private static String[] extractSortableAttributes(Class<?> argumentClass) {
        return extractFields(argumentClass, Sortable.class);
    }

    /**
     * Extracts stop-word attributes of given class and returns fields' name in an array of {@link String}.
     *
     * @param argumentClass generic type parameter of {@link BaseMapper}
     * @return stop-word attributes in an array of {@link String}
     */
    private static String[] extractStopWordAttributes(Class<?> argumentClass) {
        return extractFields(argumentClass, StopWord.class);
    }

    /**
     * Extract fields' name of given class according to specified annotation and returns names in an array of
     * {@link String}.
     *
     * @param argumentClass class to be extracted
     * @param annotation    specified annotation
     * @return fields' name in an array of {@link String}
     */
    private static String[] extractFields(Class<?> argumentClass, Class<? extends Annotation> annotation) {
        //noinspection unchecked
        return ReflectionUtils.getFields(argumentClass,
                        ReflectionUtilsPredicates.withAnnotation(annotation))
                .stream()
                .map(Field::getName)
                .toArray(String[]::new);
    }

    /**
     * Utility method to take a string and convert it to normal Java variable name capitalization.  This normally means
     * converting the first character from upper case to lower case, but in the (unusual) special case when there is
     * more than one character and both the first and second characters are upper case, we leave it alone.
     * <p>
     * Thus "FooBah" becomes "fooBah" and "X" becomes "x", but "URL" stays as "URL".
     *
     * @param name The string to be decapitalized.
     * @return The decapitalized version of the string.
     */
    private static String decapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        if (name.length() > 1 &&
                Character.isUpperCase(name.charAt(1)) &&
                Character.isUpperCase(name.charAt(0))) {

            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

}
