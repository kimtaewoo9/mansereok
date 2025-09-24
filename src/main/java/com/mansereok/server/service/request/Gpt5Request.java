package com.mansereok.server.service.request;

import com.mansereok.server.dto.Message;
import java.util.List;
import lombok.Getter;

@Getter
public class Gpt5Request {

	private String model;
	private List<Message> messages;
	private int max_completion_tokens;
	private String reasoning_effort = "medium"; // minimal, low, medium, high (추론 능력 결정)

	public Gpt5Request(String model, List<Message> messages, int max_completion_tokens) {
		this.model = model;
		this.messages = messages;
		this.max_completion_tokens = max_completion_tokens;
	}
}
