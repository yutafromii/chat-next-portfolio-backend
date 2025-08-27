package com.example.demo.dto.response;

import java.time.Instant;

public record MessageResponse(
		Long id,
		String content,
		Instant createdAt,
		Author author) {
	public record Author(Long id, String name) {
	}
}
