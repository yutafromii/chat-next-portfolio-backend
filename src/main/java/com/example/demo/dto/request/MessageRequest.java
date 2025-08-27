package com.example.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageRequest(
//		@NotNull Long authorId,
		@NotBlank @Size(max = 2000) String content) {
}