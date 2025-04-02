package com.sangyunpark.user.infrastructure.repository;

import com.sangyunpark.user.domain.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserJpaRepository extends CrudRepository<User, Long> {
}
