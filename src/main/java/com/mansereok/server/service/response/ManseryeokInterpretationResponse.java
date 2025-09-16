package com.mansereok.server.service.response;

import com.mansereok.server.entity.PersonalInfo;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ManseryeokInterpretationResponse {

	private PersonalInfo personalInfo;
	private String interpretation;
}
