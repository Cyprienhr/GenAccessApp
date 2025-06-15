package com.example.genaccessapp.repository;

import com.example.genaccessapp.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    
    List<Role> findByClientId(Long clientId);
    
    Optional<Role> findByNameAndClientId(String name, Long clientId);
    
    Boolean existsByNameAndClientId(String name, Long clientId);
    
    Boolean existsByName(String name);
}