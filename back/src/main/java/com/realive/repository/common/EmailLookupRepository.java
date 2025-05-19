package com.realive.repository.common;

import java.util.Optional;

public interface EmailLookupRepository<T> {

    Optional<T> findByEmail(String email);
    boolean existsByEmail(String email);

// E-mail을 통해서 유저를 찾는 메소드 각 repo에서 구현할때 jpa 하고나서 뒤에 붙이면됨
    
}
