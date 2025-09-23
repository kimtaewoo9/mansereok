package com.mansereok.server.dto.oauth;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class XRedirectDto {

	private String code;
	private String codeVerifier;
}
