package com.nhnacademy.memberapi.domain.auth.repository;

import com.nhnacademy.memberapi.domain.auth.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
