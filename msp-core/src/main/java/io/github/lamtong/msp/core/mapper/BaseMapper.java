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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.exceptions.MeilisearchException;
import com.meilisearch.sdk.model.*;
import io.github.lamtong.msp.core.context.IndexContext;
import io.github.lamtong.msp.core.exception.MSPException;
import io.github.lamtong.msp.core.pagination.Page;
import io.github.lamtong.msp.core.util.BaseMapperUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Embedded {@code Mapper} interface, any interface which extends this interface acquires capacities of {@code CRUD}.
 * <p/>
 * <strong>Be Aware</strong>
 * <p/>
 * Due to that {@code MeiliSearch}'s {@link Client} works asynchronously for most usage of {@link Index} except for
 * acquiring data from {@link Index}, methods in {@link BaseMapper} works the same by ignoring {@link TaskInfo} instance
 * returned by {@link Index}'s methods.
 *
 * @param <T> generic type
 * @author Lam Tong
 * @version 1.0.0
 * @since 1.0.0.SNAPSHOT
 */
public interface BaseMapper<T> {

    /**
     * Acquires instance of {@link Index}.
     * <p/>
     * Any method which intends to operate on {@link Index} should acquire an instance of {@link Index} by this method.
     *
     * @return {@link Index} instance
     */
    default Index getIndex() {
        return IndexContext.get(this.getClass());
    }


    /**
     * Inserts a document asynchronously.
     *
     * @param entity document entity to be inserted
     * @see #insertBatch(Object[])
     */
    @SuppressWarnings(value = {"unchecked"})
    default void insert(T entity) {
        insertBatch(entity);
    }

    /**
     * Inserts a document asynchronously with specified primary key.
     *
     * @param entity     document entity to be inserted
     * @param primaryKey primary key field
     */
    default void insert(T entity, String primaryKey) {
        Index index = this.getIndex();
        ObjectMapper mapper = new ObjectMapper();
        try {
            index.addDocuments(mapper.writeValueAsString(entity), primaryKey);
        } catch (MeilisearchException | JsonProcessingException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Inserts documents in batch asynchronously.
     *
     * @param entities document entities to be inserted
     * @see #insertBatch(Collection)
     */
    @SuppressWarnings(value = {"unchecked"})
    default void insertBatch(T... entities) {
        insertBatch(Arrays.asList(entities));
    }

    /**
     * Inserts documents in batch asynchronously.
     *
     * @param entities document entities to be inserted in a collection.
     * @see #insertBatch(Object[])
     */
    default void insertBatch(Collection<T> entities) {
        Index index = this.getIndex();
        ObjectMapper mapper = new ObjectMapper();
        try {
            index.addDocuments(mapper.writeValueAsString(entities));
        } catch (MeilisearchException | JsonProcessingException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Deletes all documents asynchronously.
     */
    default void deleteAll() {
        Index index = this.getIndex();
        try {
            index.deleteAllDocuments();
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Deletes a document according to the specified primary key asynchronously.
     *
     * @param id primary key of document to be deleted
     * @see #deleteById(Object)
     */
    default void deleteById(Serializable id) {
        Index index = this.getIndex();
        try {
            index.deleteDocument(String.valueOf(id));
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Deletes a document according to the primary key of given entity asynchronously.
     *
     * @param entity entity contains primary key of document to be deleted
     * @see #deleteById(Serializable)
     */
    default void deleteById(T entity) {
        Index index = this.getIndex();
        try {
            String id = BaseMapperUtils.getPrimaryKeyValue(entity);
            index.deleteDocument(id);
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Deletes documents according to specified primary keys asynchronously.
     *
     * @param ids primary keys of documents to be deleted
     * @see #deleteByIds(Collection)
     */
    default void deleteByIds(Serializable... ids) {
        deleteByIds(Arrays.asList(ids));
    }

    /**
     * Deletes documents according to specified primary keys asynchronously.
     *
     * @param ids primary keys of documents in a collection to be deleted
     * @see #deleteByIds(Serializable...)
     */
    default void deleteByIds(Collection<? extends Serializable> ids) {
        Index index = this.getIndex();
        try {
            index.deleteDocuments(ids.stream().map(String::valueOf).collect(Collectors.toList()));
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Updates a document by primary key asynchronously.
     *
     * @param entity entity contains primary key, to be updated
     * @see #updateById(Object, String)
     */
    default void updateById(T entity) {
        Index index = this.getIndex();
        ObjectMapper mapper = new ObjectMapper();
        try {
            index.updateDocuments(mapper.writeValueAsString(entity));
        } catch (MeilisearchException | JsonProcessingException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Updates a document by primary key asynchronously with specified primary key.
     *
     * @param entity     entity contains primary key, to be updated
     * @param primaryKey primary key
     * @see #updateById(Object)
     */
    default void updateById(T entity, String primaryKey) {
        Index index = this.getIndex();
        ObjectMapper mapper = new ObjectMapper();
        try {
            index.updateDocuments(mapper.writeValueAsString(entity), primaryKey);
        } catch (MeilisearchException | JsonProcessingException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Acquires a document according to primary key.
     *
     * @param id primary key of document to be acquired
     * @return document or {@code null}
     * @see #selectById(Serializable, String...)
     * @see #selectById(Serializable, Collection)
     */
    default T selectById(Serializable id) {
        Object document;
        Index index = this.getIndex();
        try {
            Class<?> targetClass = BaseMapperUtils.getActualTypeClass(getClass());
            document = index.getDocument(String.valueOf(id), targetClass);
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
        //noinspection unchecked
        return (T) document;
    }

    /**
     * Acquires a document according to primary key with specified fields to be returned in a {@link Map}.
     *
     * @param id     primary key
     * @param fields fields to be returned
     * @return {@link Map} instance containing only specified fields
     * @see #selectById(Serializable)
     * @see #selectById(Serializable, Collection)
     */
    @SuppressWarnings(value = {"unchecked"})
    default Map<String, Object> selectById(Serializable id, String... fields) {
        Map<String, Object> map;
        Index index = this.getIndex();
        try {
            DocumentQuery query = new DocumentQuery();
            query.setFields(fields);
            map = index.getDocument(String.valueOf(id), query, Map.class);
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
        return map;
    }

    /**
     * Acquires a document according to primary key with specified fields in a collection of {@link String} to be
     * returned in a {@link Map}.
     *
     * @param id     primary key
     * @param fields fields to be returned in a collection of {@link String}
     * @return {@link Map} instance containing only specified fields
     * @see #selectById(Serializable)
     * @see #selectById(Serializable, String...)
     */
    default Map<String, Object> selectById(Serializable id, Collection<String> fields) {
        return selectById(id, fields.toArray(new String[0]));
    }

    /**
     * Acquires documents according to primary keys.
     *
     * @param ids primary keys
     * @return {@link List} of document
     * @see #selectByIds(Collection)
     */
    default List<T> selectByIds(Serializable... ids) {
        return this.selectByIds(Arrays.asList(ids));
    }

    /**
     * Acquires documents according to primary keys in a collection.
     *
     * @param ids primary keys in a collection.
     * @return {@link List} of document
     * @see #selectByIds(Serializable...)
     */
    default List<T> selectByIds(Collection<? extends Serializable> ids) {
        List<T> list;
        Index index = this.getIndex();
        Class<?> targetClass = BaseMapperUtils.getActualTypeClass(getClass());
        list = ids.stream()
                .map(id -> {
                    try {
                        Object document = index.getDocument(String.valueOf(id), targetClass);
                        //noinspection unchecked
                        return (T) document;
                    } catch (MeilisearchException e) {
                        throw new MSPException(e);
                    }
                })
                .collect(Collectors.toList());
        return list;
    }

    /**
     * Acquires all documents in related {@link Index} with all fields to be returned.
     *
     * @return all documents in related {@link Index} with all fields to be returned
     * @see #selectList(String...)
     */
    default List<Map<String, Object>> selectList() {
        Class<?> targetClass = BaseMapperUtils.getActualTypeClass(getClass());
        String[] fields = BaseMapperUtils.getAllGenericTypeFields(targetClass);
        return selectList(fields);
    }

    /**
     * Acquires all documents in related {@link Index} with specified fields to be returned.
     *
     * @param fields specified fields to be returned.
     * @return all documents in related {@link Index} with specified fields to be returned.
     * @see #selectList(Integer, Integer, String...)
     */
    default List<Map<String, Object>> selectList(String... fields) {
        return selectList(0, Integer.MAX_VALUE, fields);
    }

    /**
     * Acquires documents in range in related {@link Index} with all fields to be returned.
     *
     * @param offset number of documents to skip
     * @param limit  number of documents to return
     * @return documents in range in related {@link Index} with all fields to be returned.
     * @see #selectList(Integer, Integer, String...)
     */
    default List<Map<String, Object>> selectList(Integer offset,
                                                 Integer limit) {
        Class<?> targetClass = BaseMapperUtils.getActualTypeClass(getClass());
        String[] fields = BaseMapperUtils.getAllGenericTypeFields(targetClass);
        return selectList(offset, limit, fields);
    }

    /**
     * Acquires documents in range in related {@link Index} with specified fields to be returned.
     *
     * @param offset number of documents to skip
     * @param limit  number of documents to return
     * @param fields specified fields to be returned.
     * @return documents in range in related {@link Index} with specified fields to be returned.
     * @apiNote this method corresponds to {@code MeiliSearch} search request {@code /Indexes/{index_uid}/documents}
     * with no limitation by default.
     * @see #selectList()
     * @see #selectList(String...)
     * @see #selectList(Integer, Integer)
     * @see #selectList(String)
     * @see #selectList(SearchRequest)
     */
    default List<Map<String, Object>> selectList(Integer offset,
                                                 Integer limit,
                                                 String... fields) {
        List<Map<String, Object>> list;
        DocumentsQuery query = new DocumentsQuery()
                .setOffset(offset)
                .setLimit(limit)
                .setFields(fields);
        Index index = this.getIndex();
        try {
            //noinspection unchecked
            Map<String, Object>[] results = index.getDocuments(query, Map.class).getResults();
            list = Stream.of(results)
                    .map(HashMap::new)
                    .collect(Collectors.toList());
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
        return list;
    }

    /**
     * Acquires documents which match given query condition with all fields to be returned by default.
     *
     * @param queryString query condition
     * @return documents which match given query condition in a {@link List}
     * @apiNote this method corresponds to {@code MeiliSearch} search request {@code /indexes/{index_uid}/search}, which
     * returns at most 1000 documents by default.
     * @see #selectList(Integer, Integer, String...)
     */
    @SuppressWarnings(value = {"DuplicatedCode"})
    default List<T> selectList(String queryString) {
        List<T> list;
        Index index = this.getIndex();
        Class<?> targetClass = BaseMapperUtils.getActualTypeClass(getClass());
        try {
            //noinspection unchecked
            list = index.search(queryString)
                    .getHits()
                    .stream()
                    .map(map -> BaseMapperUtils.convertFromMapToType(map, ((Class<T>) targetClass)))
                    .collect(Collectors.toList());
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
        return list;
    }

    /**
     * Acquires documents which match given query condition with all fields to be returned by default.
     *
     * @param queryString query condition
     * @param offset      number of document to skip
     * @param limit       number of document to return
     * @return documents which match given query condition in a {@link List}
     * @see #selectList(SearchRequest)
     */
    default List<T> selectList(String queryString,
                               Integer offset,
                               Integer limit) {
        SearchRequest request = SearchRequest.builder()
                .offset(offset)
                .limit(limit)
                .q(queryString)
                .build();
        return selectList(request);
    }

    /**
     * Acquires documents according to given {@link SearchRequest} instance in a {@link List} with all fields to be
     * returned by default where the {@link SearchRequest} contains parameters for pagination like
     * {@link SearchRequest#offset} and {@link SearchRequest#limit}.
     *
     * @param request {@link SearchRequest} instance
     * @return documents in a {@link List}
     * @apiNote this method corresponds to {@code MeiliSearch}' search request {@code /indexes/{index_uid}/search},
     * which returns at most 1000 documents by default.
     * @see #selectList(Integer, Integer, String...)
     */
    @SuppressWarnings(value = {"DuplicatedCode"})
    default List<T> selectList(SearchRequest request) {
        List<T> list;
        Index index = this.getIndex();
        Class<?> targetClass = BaseMapperUtils.getActualTypeClass(getClass());
        try {
            //noinspection unchecked
            list = index.search(request)
                    .getHits()
                    .stream()
                    .map(m -> BaseMapperUtils.convertFromMapToType(m, ((Class<T>) targetClass)))
                    .collect(Collectors.toList());
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
        return list;
    }

    /**
     * Acquires documents which match specified condition {@code queryString} in a {@link String}, and returns documents
     * in a {@link Page}. <br/>
     * <strong>Be aware that</strong> {@code PageSize} and {@code PageNumber} are not provided, values of
     * {@code PageSize} and {@code PageNumber} are <strong>10</strong> and <strong>1</strong> respectively by default.
     *
     * @param queryString specified condition which must be matched
     * @return documents in a {@link Page}
     * @see #selectPage(int, int, String)
     * @see #selectPage(Page, String)
     * @see #selectPage(SearchRequest)
     */
    default Page<T> selectPage(String queryString) {
        return selectPage(10, 1, queryString);
    }

    /**
     * Acquires documents which match specified condition {@code queryString} with specified page size and page number,
     * and returns documents in a {@link Page}.
     *
     * @param pageSize    current page size
     * @param pageNumber  current page number
     * @param queryString specified condition which must be matched
     * @return documents in a {@link Page}
     * @see #selectPage(String)
     * @see #selectPage(Page, String)
     * @see #selectPage(SearchRequest)
     */
    default Page<T> selectPage(int pageSize,
                               int pageNumber,
                               String queryString) {
        Page<T> page = new Page<>(pageSize, pageNumber);
        return selectPage(page, queryString);
    }

    /**
     * Acquires documents which match specified condition {@code queryString} with specified pagination, and returns
     * documents in a {@link Page}.
     *
     * @param page        specified pagination
     * @param queryString specified condition which must be matched
     * @return documents in a {@link Page}
     * @see #selectPage(String)
     * @see #selectPage(int, int, String)
     * @see #selectPage(SearchRequest)
     */
    default Page<T> selectPage(Page<T> page, String queryString) {
        SearchRequest request = SearchRequest.builder()
                .page(page.getPageNumber())
                .hitsPerPage(page.getPageSize())
                .q(queryString)
                .build();
        return selectPage(request);
    }

    /**
     * Acquires documents according to {@link SearchRequest}.
     *
     * @param request instance of {@link SearchRequest}
     * @return documents in a {@link Page}
     * @see #selectPage(String)
     * @see #selectPage(int, int, String)
     * @see #selectPage(Page, String)
     */
    default Page<T> selectPage(SearchRequest request) {
        Page<T> page;
        Index index = this.getIndex();
        Class<?> targetClass = BaseMapperUtils.getActualTypeClass(getClass());
        try {
            SearchResultPaginated paginatedResult = (SearchResultPaginated) index.search(request);
            //noinspection unchecked
            page = BaseMapperUtils.convertToPage(paginatedResult, ((Class<T>) targetClass));
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
        return page;
    }

    /**
     * Gets settings information of {@link Index}.
     *
     * @return settings of {@link Index}
     * @see Index#getSettings()
     */
    default Settings getSettings() {
        Settings settings;
        Index index = this.getIndex();
        try {
            settings = index.getSettings();
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
        return settings;
    }

    /**
     * Updates settings of {@link Index}.
     *
     * @param settings {@link Settings} instances to be updated
     * @see Index#updateSettings(Settings)
     */
    default void updateSettings(Settings settings) {
        Index index = this.getIndex();
        try {
            index.updateSettings(settings);
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Resets settings of {@link Index}.
     *
     * @see Index#resetSettings()
     */
    default void resetSettings() {
        Index index = this.getIndex();
        try {
            index.resetSettings();
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Gets ranking rules of {@link Index} in an array.
     *
     * @return ranking rules of {@link Index} in an array
     * @see Index#getRankingRulesSettings()
     */
    default String[] getRankingRulesSettings() {
        String[] ans;
        Index index = this.getIndex();
        try {
            ans = index.getRankingRulesSettings();
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
        return ans;
    }

    /**
     * Updates ranking rules of {@link Index}.
     *
     * @param rules ranking rules to be updated
     * @see Index#updateRankingRulesSettings(String[])
     */
    default void updateRankingRulesSettings(String[] rules) {
        Index index = this.getIndex();
        try {
            index.updateRankingRulesSettings(rules);
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Resets ranking rules of {@link Index}.
     *
     * @see Index#resetRankingRulesSettings()
     */
    default void resetRankingRulesSettings() {
        Index index = this.getIndex();
        try {
            index.resetRankingRulesSettings();
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Gets synonyms settings of {@link Index} in a {@link Map}.
     *
     * @return synonyms setting of {@link Index} in a {@link Map}
     * @see Index#getSynonymsSettings()
     */
    default Map<String, String[]> getSynonymsSettings() {
        Map<String, String[]> ans;
        Index index = this.getIndex();
        try {
            ans = index.getSynonymsSettings();
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
        return ans;
    }

    /**
     * Updates synonyms setting of {@link Index}.
     *
     * @param setting synonyms setting to be updated
     * @see Index#updateSynonymsSettings(Map)
     */
    default void updateSynonymsSettings(Map<String, String[]> setting) {
        Index index = this.getIndex();
        try {
            index.updateSynonymsSettings(setting);
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Resets synonyms settings of {@link Index}
     *
     * @see Index#resetSynonymsSettings()
     */
    default void resetSynonymsSettings() {
        Index index = this.getIndex();
        try {
            index.resetSynonymsSettings();
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Gets stop-words settings of {@link Index} in an array.
     *
     * @return stop-words settings of {@link Index} in an array
     * @see Index#getStopWordsSettings()
     */
    default String[] getStopWordsSettings() {
        String[] ans;
        Index index = this.getIndex();
        try {
            ans = index.getStopWordsSettings();
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
        return ans;
    }

    /**
     * Updates stop-words settings in {@link Index}.
     *
     * @param setting stop-words settings to be updated
     * @see Index#updateStopWordsSettings(String[])
     */
    default void updateStopWordsSettings(String[] setting) {
        Index index = this.getIndex();
        try {
            index.updateStopWordsSettings(setting);
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Resets stop-words settings in {@link Index}.
     *
     * @see Index#resetStopWordsSettings()
     */
    default void resetStopWordsSettings() {
        Index index = this.getIndex();
        try {
            index.resetStopWordsSettings();
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Gets searchable attributes of {@link Index} in an array.
     *
     * @return search attributes of {@link Index} in an array
     * @see Index#getSearchableAttributesSettings()
     */
    default String[] getSearchableAttributesSettings() {
        String[] ans;
        Index index = this.getIndex();
        try {
            ans = index.getSearchableAttributesSettings();
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
        return ans;
    }

    /**
     * Updates searchable attributes of {@link Index}
     *
     * @param setting searchable attributes to be updated
     * @see Index#updateSearchableAttributesSettings(String[])
     */
    default void updateSearchableAttributesSettings(String[] setting) {
        Index index = this.getIndex();
        try {
            index.updateSearchableAttributesSettings(setting);
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Resets searchable attributes of {@link Index}
     *
     * @see Index#resetSearchableAttributesSettings()
     */
    default void resetSearchableAttributesSettings() {
        Index index = this.getIndex();
        try {
            index.resetSearchableAttributesSettings();
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Gets displayed attributes of {@link Index} in an array.
     *
     * @return displayed attributes of {@link Index} in an array
     * @see Index#getDisplayedAttributesSettings()
     */
    default String[] getDisplayedAttributesSettings() {
        String[] ans;
        Index index = this.getIndex();
        try {
            ans = index.getDisplayedAttributesSettings();
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
        return ans;
    }

    /**
     * Updates displayed attributes of {@link Index}.
     *
     * @param setting displayed attributes to be updated
     * @see Index#updateDisplayedAttributesSettings(String[])
     */
    default void updateDisplayedAttributesSettings(String[] setting) {
        Index index = this.getIndex();
        try {
            index.updateDisplayedAttributesSettings(setting);
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Resets displayed attributes of {@link Index}.
     *
     * @see Index#resetDisplayedAttributesSettings()
     */
    default void resetDisplayedAttributesSettings() {
        Index index = this.getIndex();
        try {
            index.resetDisplayedAttributesSettings();
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Gets filterable attributes of {@link Index} in an array.
     *
     * @return filterable attributes of {@link Index} in an array
     * @see Index#getFilterableAttributesSettings()
     */
    default String[] getFilterableAttributesSettings() {
        String[] ans;
        Index index = this.getIndex();
        try {
            ans = index.getFilterableAttributesSettings();
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
        return ans;
    }

    /**
     * Updates filterable attributes of {@link Index}.
     *
     * @param setting filterable attributes to be updated
     * @see Index#updateFilterableAttributesSettings(String[])
     */
    default void updateFilterableAttributesSettings(String[] setting) {
        Index index = this.getIndex();
        try {
            index.updateFilterableAttributesSettings(setting);
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Resets filterable attributes of {@link Index}.
     *
     * @see Index#resetFilterableAttributesSettings()
     */
    default void resetFilterableAttributesSettings() {
        Index index = this.getIndex();
        try {
            index.resetFilterableAttributesSettings();
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Gets distinct attribute of {@link Index} in a {@link String}
     *
     * @return distinct attribute of {@link Index} in a {@link String}
     * @see Index#getDistinctAttributeSettings()
     */
    default String getDistinctAttributeSettings() {
        String ans;
        Index index = this.getIndex();
        try {
            ans = index.getDistinctAttributeSettings();
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
        return ans;
    }

    /**
     * Updates distinct attribute of {@link Index}.
     *
     * @param setting distinct attributes to be updated
     * @see Index#updateDistinctAttributeSettings(String)
     */
    default void updateDistinctAttributeSettings(String setting) {
        Index index = this.getIndex();
        try {
            index.updateDistinctAttributeSettings(setting);
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Resets distinct attribute of {@link Index}.
     *
     * @see Index#resetDistinctAttributeSettings()
     */
    default void resetDistinctAttributeSettings() {
        Index index = this.getIndex();
        try {
            index.resetDistinctAttributeSettings();
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Gets typo tolerance of {@link Index} in a {@link TypoTolerance} instance.
     *
     * @return typo tolerance of {@link Index} in a {@link TypoTolerance} instance
     * @see Index#getTypoToleranceSettings()
     */
    default TypoTolerance getTypoToleranceSettings() {
        TypoTolerance tolerance;
        Index index = this.getIndex();
        try {
            tolerance = index.getTypoToleranceSettings();
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
        return tolerance;
    }

    /**
     * Updates typo tolerance settings of {@link Index}.
     *
     * @param tolerance typo tolerance settings to be updated
     * @see Index#updateTypoToleranceSettings(TypoTolerance)
     */
    default void updateTypoToleranceSettings(TypoTolerance tolerance) {
        Index index = this.getIndex();
        try {
            index.updateTypoToleranceSettings(tolerance);
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Resets typo tolerance settings of {@link Index}.
     *
     * @see Index#resetTypoToleranceSettings()
     */
    default void resetTypoToleranceSettings() {
        Index index = this.getIndex();
        try {
            index.resetTypoToleranceSettings();
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
    }

    /**
     * Gets statistics of {@link Index} in an {@link IndexStats} instance.
     *
     * @return statistics of {@link Index} in an {@link IndexStats} instance
     * @see Index#getStats()
     */
    default IndexStats getStats() {
        IndexStats stats;
        Index index = this.getIndex();
        try {
            stats = index.getStats();
        } catch (MeilisearchException e) {
            throw new MSPException(e);
        }
        return stats;
    }


}
