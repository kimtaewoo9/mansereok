package com.mansereok.server.repository;

import com.mansereok.server.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	// 로그인 시 사용자 조회용
	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);

	// 회원가입 시 중복 체크용
	boolean existsByUsername(String username);

	boolean existsByEmail(String email);
}
