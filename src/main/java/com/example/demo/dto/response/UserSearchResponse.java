package com.example.demo.dto.response;

public record UserSearchResponse(
	    Long id,
	    String name,
	    String email,
	    String avatarUrl
	) {}