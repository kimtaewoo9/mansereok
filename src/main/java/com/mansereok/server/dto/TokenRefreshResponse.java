package com.mansereok.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshResponse {

	private UserDto userDto;
	private String accessToken;

	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class UserDto {

		private String id;
		private String createdAt;
		private String email;
		private String name;
		private String role;
		private boolean locked;
	}
}
