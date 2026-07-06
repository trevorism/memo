package com.trevorism.service

import com.trevorism.data.Repository
import com.trevorism.data.model.filtering.ComplexFilter
import com.trevorism.data.model.filtering.SimpleFilter
import com.trevorism.data.model.paging.PageRequest
import com.trevorism.data.model.sorting.ComplexSort
import com.trevorism.data.model.sorting.Sort

/**
 * Hermetic in-memory Repository<T> for unit tests: no network, no datastore.
 * Assigns incrementing ids on create and supports equality filtering on a single field.
 */
class InMemoryRepository<T> implements Repository<T> {

    private final Map<String, T> store = new LinkedHashMap<>()
    private int counter = 0

    InMemoryRepository(List<T> seed = []) {
        seed.each { create(it) }
    }

    @Override
    List<T> all() { list() }

    @Override
    List<T> list() { new ArrayList<T>(store.values()) }

    @Override
    T get(String id) { id == null ? null : store.get(id) }

    @Override
    T create(T item) {
        if (!item.id) {
            item.id = (++counter).toString()
        }
        store.put(item.id, item)
        return item
    }

    @Override
    T update(String id, T item) {
        if (!store.containsKey(id)) {
            return null
        }
        item.id = id
        store.put(id, item)
        return item
    }

    @Override
    T delete(String id) { store.remove(id) }

    @Override
    void ping() {}

    @Override
    List<T> filter(SimpleFilter simpleFilter) {
        String field = simpleFilter.field
        String value = simpleFilter.value
        return list().findAll { it."$field"?.toString() == value }
    }

    @Override
    List<T> filter(ComplexFilter complexFilter) { list() }

    @Override
    List<T> page(PageRequest pageRequest) { list() }

    @Override
    List<T> sort(ComplexSort complexSort) { list() }

    @Override
    List<T> sort(Sort sort) { list() }
}
