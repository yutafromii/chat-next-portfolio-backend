package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);
  boolean existsByNameIgnoreCase(String name);
  @Query("""
		    select u from User u
		    left join fetch u.profile p
		    where u.id <> :me
		      and (
		        lower(u.name)  like lower(concat('%', :q, '%'))
		        or lower(u.email) like lower(concat('%', :q, '%'))
		      )
		    order by u.name asc
		  """)
	List<User> searchUsers(@Param("q") String q, @Param("me") Long meId);
}