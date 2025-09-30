package com.mansereok.server.service.response.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GptCompatibilityResponse {

	private Integer score;
	private String interpretation;
}
