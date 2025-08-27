package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
	Optional<Task> findById(Long id);

	  Page<Task> findByUserIdAndNameContainingIgnoreCase(Long userId, String name, Pageable pageable);

	Page<Task> findByUserId(Long userId, Pageable pageable);

	Optional<Task> findByIdAndUserId(Long id, Long userId);
}
