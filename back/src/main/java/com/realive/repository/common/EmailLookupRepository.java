package com.realive.repository.common;

import java.util.Optional;

public interface EmailLookupRepository<T> {

    Optional<T> findByEmail(String email);
    boolean existsByEmail(String email);



}