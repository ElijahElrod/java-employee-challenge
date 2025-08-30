package com.reliaquest.api.service;

import com.reliaquest.api.constants.CacheNames;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeSearchCacheEvictionService {

    private final CacheManager cacheManager;

    public void evictEmployeeFragmentsByUUID(final Set<String> searchStrings) {
        final var searchCache = cacheManager.getCache(CacheNames.EMPLOYEES_BY_NAME_SEARCH);
        if (searchCache == null) {
            return;
        }
        searchStrings.forEach(searchCache::evictIfPresent);
    }
}
