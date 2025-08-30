package com.reliaquest.api.service;

import com.reliaquest.api.constants.CacheNames;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * Service for evicting cached employee search fragments.
 * <p>
 * This service provides functionality to remove cached search results for employees
 * based on a set of search strings (typically fragments of employee names).
 * <p>
 * Uses the {@link CacheManager} to access and evict entries from the
 * {@link CacheNames#EMPLOYEES_BY_NAME_SEARCH} cache.
 */
@Service
@RequiredArgsConstructor
public class EmployeeSearchCacheEvictionService {

    private final CacheManager cacheManager;

    /**
     * Evicts cached employee search fragments for the provided set of search strings.
     *
     * @param searchStrings Set of search strings (e.g., name fragments) whose cache entries should be evicted.
     */
    public void evictEmployeeFragmentsByUUID(final Set<String> searchStrings) {
        final var searchCache = cacheManager.getCache(CacheNames.EMPLOYEES_BY_NAME_SEARCH);
        if (searchCache == null) {
            return;
        }
        searchStrings.forEach(searchCache::evictIfPresent);
    }
}
