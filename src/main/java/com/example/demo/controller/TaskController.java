package com.example.demo.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Task;
import com.example.demo.service.TaskService;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // 一覧 + 検索 + ソート + ページング（これ1本に統一）
    @GetMapping
    public Page<Task> list(
            @RequestParam(required = false) String q,
            Pageable pageable // ?page=0&size=10&sort=completed,desc を自動バインド
    ) {
        // 並べ替え未指定 (= unsorted) のときに「追加順」を保証したいなら以下を有効化
        // if (pageable.getSort().isUnsorted()) {
        //     pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
        //                               Sort.by("id").ascending()); // or createdAt.asc()
        // }
        return taskService.search(q, pageable);
    }

    // 詳細
    @GetMapping("/{id}")
    public Task findById(@PathVariable Long id) {
        return taskService.findById(id);
    }

    // 作成（201 Created）
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task createTask(@Valid @RequestBody Task task) {
        return taskService.create(task);
    }

    // 更新（PUT）
    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @Valid @RequestBody Task task) {
        return taskService.update(id, task);
    }

    // 削除（204 No Content）
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) {
        taskService.deleteById(id);
    }
}

