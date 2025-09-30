package com.mansereok.server.repository;

import com.mansereok.server.entity.CompatibilityResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompatibilityResultRepository extends JpaRepository<CompatibilityResult, Long> {


}
