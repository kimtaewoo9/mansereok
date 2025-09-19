package com.mansereok.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

	@NotBlank(message = "사용자명은 필수입니다")
	private String username; // 이메일 ..

	@NotBlank(message = "비밀번호는 필수입니다")
	private String password;
}
