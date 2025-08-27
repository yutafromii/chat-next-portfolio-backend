package com.example.demo.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.entity.Task;
import com.example.demo.entity.User;
import com.example.demo.repository.TaskRepository;
import com.example.demo.security.CurrentUserProvider;

@Service
@Transactional(readOnly = true)
public class TaskService {

	private final TaskRepository taskRepository;
	private final CurrentUserProvider currentUser;

	public TaskService(TaskRepository taskRepository, CurrentUserProvider currentUser) {
		this.taskRepository = taskRepository;
		this.currentUser = currentUser;
	}

	public List<Task> getAll() {
		Long uId = currentUser.getRequiredUser().getId();
		return taskRepository.findByUserId(uId, Pageable.unpaged()).getContent();
	}

	public Task findById(Long id) {
		Long uId = currentUser.getRequiredUser().getId();
		return taskRepository.findByIdAndUserId(id, uId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	@Transactional
	public Task create(Task input) {
		User me = currentUser.getRequiredUser();
		Task t = new Task();
		t.setName(input.getName());
		t.setCompleted(input.getCompleted());
		t.setUser(me);
		// 必要なら初期値の設定など
		return taskRepository.save(t);
	}

	@Transactional
	public Task update(Long id, Task input) {
		Long uId = currentUser.getRequiredUser().getId();
		Task current = taskRepository.findByIdAndUserId(id, uId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		// 更新したいフィールドを反映（例）
		current.setName(input.getName());
		current.setCompleted(input.getCompleted());

		return taskRepository.save(current);
	}

	@Transactional
	public void deleteById(Long id) {
		Long uId = currentUser.getRequiredUser().getId();
		Task entity = taskRepository.findByIdAndUserId(id, uId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "task not found"));
		taskRepository.delete(entity);
	}

	// ページング機能
	@Transactional(readOnly = true)
	public Page<Task> search(String q, Pageable pageable) {
	    Long uid = currentUser.getRequiredUser().getId();

	    // 並べ替え未指定時の既定ソートを付けたい場合はここで補完（任意）
	    // if (pageable.getSort().isUnsorted()) {
	    //   pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id").ascending());
	    // }

	    if (q == null || q.isBlank()) {
	      return taskRepository.findByUserId(uid, pageable);
	    }
	    return taskRepository.findByUserIdAndNameContainingIgnoreCase(uid, q.trim(), pageable);
	  }

}
