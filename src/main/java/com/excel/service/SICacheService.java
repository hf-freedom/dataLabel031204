package com.excel.service;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SICacheService {

    private final Map<String, Map<Integer, Object>> taskDataCache = new ConcurrentHashMap<>();
    
    private final Map<String, String> taskTemplateCache = new ConcurrentHashMap<>();
    
    private final Map<String, List<String>> taskHeadersCache = new ConcurrentHashMap<>();

    public String generateTaskId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    public void saveTaskData(String taskId, String templateId, List<String> headers, Map<Integer, Object> data) {
        taskDataCache.put(taskId, data);
        taskTemplateCache.put(taskId, templateId);
        taskHeadersCache.put(taskId, headers);
    }

    public Map<Integer, Object> getTaskData(String taskId) {
        return taskDataCache.get(taskId);
    }

    public String getTaskTemplate(String taskId) {
        return taskTemplateCache.get(taskId);
    }

    public List<String> getTaskHeaders(String taskId) {
        return taskHeadersCache.get(taskId);
    }

    public void updateTaskData(String taskId, Map<Integer, Object> data) {
        if (taskDataCache.containsKey(taskId)) {
            taskDataCache.put(taskId, data);
        }
    }

    public void updateRowData(String taskId, Integer rowIndex, Object rowData) {
        Map<Integer, Object> data = taskDataCache.get(taskId);
        if (data != null) {
            data.put(rowIndex, rowData);
        }
    }

    public void deleteRowData(String taskId, Integer rowIndex) {
        Map<Integer, Object> data = taskDataCache.get(taskId);
        if (data != null) {
            data.remove(rowIndex);
        }
    }

    public void clearTask(String taskId) {
        taskDataCache.remove(taskId);
        taskTemplateCache.remove(taskId);
        taskHeadersCache.remove(taskId);
    }

    public boolean existsTask(String taskId) {
        return taskDataCache.containsKey(taskId);
    }

    public List<Object> getTaskDataList(String taskId) {
        Map<Integer, Object> data = taskDataCache.get(taskId);
        if (data == null) {
            return new ArrayList<>();
        }
        List<Object> result = new ArrayList<>();
        List<Integer> sortedKeys = new ArrayList<>(data.keySet());
        Collections.sort(sortedKeys);
        for (Integer key : sortedKeys) {
            result.add(data.get(key));
        }
        return result;
    }
}
