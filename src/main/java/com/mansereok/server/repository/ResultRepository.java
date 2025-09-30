package com.mansereok.server.repository;

import com.mansereok.server.entity.Result;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {

	Optional<Result> findBySolarDateAndSolarTimeAndGenderAndIsLunar(
		LocalDate solarDate, LocalTime solarTime, String gender, Boolean isLunar
	);
}
