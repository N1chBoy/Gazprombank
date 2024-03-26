package com.example.routeweb.repositiries;

import com.example.routeweb.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepo extends JpaRepository<User, Long> {
   @Query("SELECT u FROM User u WHERE u.login_user = :username")
   User findByLogin_user(@Param("username") String username);
}



