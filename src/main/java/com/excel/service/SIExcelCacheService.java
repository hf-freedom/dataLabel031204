package com.excel.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SIExcelCacheService {

    private final Map<String, List<Map<String, Object>>> cache = new ConcurrentHashMap<>();
    private final Map<String, String> templateIdCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> headerCache = new ConcurrentHashMap<>();

    @CachePut(value = "excelData", key = "#taskId")
    public List<Map<String, Object>> saveExcelData(String taskId, String templateId, 
                                                  List<String> headers, 
                                                  List<Map<String, Object>> dataList) {
        cache.put(taskId, new ArrayList<>(dataList));
        templateIdCache.put(taskId, templateId);
        headerCache.put(taskId, new ArrayList<>(headers));
        return dataList;
    }

    @Cacheable(value = "excelData", key = "#taskId")
    public List<Map<String, Object>> getExcelData(String taskId) {
        return cache.get(taskId);
    }

    public String getTemplateId(String taskId) {
        return templateIdCache.get(taskId);
    }

    public List<String> getHeaders(String taskId) {
        return headerCache.get(taskId);
    }

    @CacheEvict(value = "excelData", key = "#taskId")
    public void removeData(String taskId, int index) {
        List<Map<String, Object>> list = cache.get(taskId);
        if (list != null && index >= 0 && index < list.size()) {
            list.remove(index);
        }
    }

    public void updateData(String taskId, int index, Map<String, Object> newData) {
        List<Map<String, Object>> list = cache.get(taskId);
        if (list != null && index >= 0 && index < list.size()) {
            list.set(index, newData);
        }
    }

    @CacheEvict(value = "excelData", key = "#taskId")
    public List<Map<String, Object>> saveAndClear(String taskId) {
        List<Map<String, Object>> result = new ArrayList<>(cache.get(taskId));
        cache.remove(taskId);
        templateIdCache.remove(taskId);
        headerCache.remove(taskId);
        return result;
    }
}
