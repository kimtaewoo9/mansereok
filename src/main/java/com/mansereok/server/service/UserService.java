package com.mansereok.server.service;

import com.mansereok.server.entity.Role;
import com.mansereok.server.entity.SocialType;
import com.mansereok.server.entity.User;
import com.mansereok.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	/**
	 * 새로운 사용자를 등록한다.
	 *
	 * @param name     사용자명
	 * @param password 평문 비밀번호 (암호화되어 저장됨)
	 * @param email    이메일
	 * @param role     사용자 역할
	 * @return 생성된 사용자 엔티티
	 */
	public User createUser(String name, String email, String password, Role role) {
		if (userRepository.existsByUsername(name)) {
			throw new RuntimeException("이미 존재하는 사용자명입니다: " + name);
		}
		if (userRepository.existsByEmail(email)) {
			throw new RuntimeException("이미 존재하는 이메일 입니다: " + email);
		}

		User user = new User(
			name,
			passwordEncoder.encode(password),
			email,
			role != null ? role : Role.USER,
			true
		);

		return userRepository.save(user);
	}

	public User findByUsername(String username) {
		return userRepository.findByUsername(username)
			.orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + username));
	}

	public User getUserBySocialId(String socialId) {
		return userRepository.findBySocialId(socialId)
			.orElse(null);
	}

	// Oauth를 통한 회원가입 !.
	public User registerWithOauth(String username, String email, String name,
		String sub, SocialType socialType) {
		return userRepository.save(
			User.create(
				sub,
				name, // 사용자 이름
				email,
				sub, // socialId
				socialType
			)
		);
	}
}
