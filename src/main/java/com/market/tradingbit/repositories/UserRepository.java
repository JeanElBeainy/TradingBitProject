package com.market.tradingbit.repositories;

import com.market.tradingbit.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    @Query("SELECT u.id FROM User u WHERE u.email = :email")
    Long findUserIdByEmail(String email);
}
