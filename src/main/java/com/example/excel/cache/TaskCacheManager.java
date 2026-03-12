package com.example.excel.cache;

import com.example.excel.dto.ExcelTaskDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class TaskCacheManager {

    private final Map<String, ExcelTaskDTO> taskCache = new ConcurrentHashMap<>();

    @Value("${excel.cache.expire-minutes:30}")
    private int expireMinutes;

    private ScheduledExecutorService scheduler;

    @PostConstruct
    public void init() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "task-cache-cleaner");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(this::cleanExpiredTasks, 5, 5, TimeUnit.MINUTES);
        log.info("Task cache manager initialized, expire time: {} minutes", expireMinutes);
    }

    public String createTaskId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public void putTask(String taskId, ExcelTaskDTO task) {
        task.setTaskId(taskId);
        task.setCreateTime(LocalDateTime.now());
        task.setExpireTime(LocalDateTime.now().plusMinutes(expireMinutes));
        taskCache.put(taskId, task);
        log.debug("Task cached: {}, total tasks: {}", taskId, taskCache.size());
    }

    public ExcelTaskDTO getTask(String taskId) {
        ExcelTaskDTO task = taskCache.get(taskId);
        if (task != null) {
            task.setExpireTime(LocalDateTime.now().plusMinutes(expireMinutes));
        }
        return task;
    }

    public void removeTask(String taskId) {
        taskCache.remove(taskId);
        log.debug("Task removed: {}, total tasks: {}", taskId, taskCache.size());
    }

    public boolean exists(String taskId) {
        return taskCache.containsKey(taskId);
    }

    public void updateTask(String taskId, ExcelTaskDTO task) {
        if (taskCache.containsKey(taskId)) {
            task.setExpireTime(LocalDateTime.now().plusMinutes(expireMinutes));
            taskCache.put(taskId, task);
        }
    }

    private void cleanExpiredTasks() {
        LocalDateTime now = LocalDateTime.now();
        int beforeSize = taskCache.size();
        taskCache.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().getExpireTime().isBefore(now);
            if (expired) {
                log.debug("Task expired and removed: {}", entry.getKey());
            }
            return expired;
        });
        int afterSize = taskCache.size();
        if (beforeSize != afterSize) {
            log.info("Cleaned expired tasks, before: {}, after: {}", beforeSize, afterSize);
        }
    }

    public Map<String, ExcelTaskDTO> getAllTasks() {
        return new ConcurrentHashMap<>(taskCache);
    }
}
