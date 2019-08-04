package com.example.easynotes.repository;

import com.example.easynotes.model.Stores;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface StoreRepository extends JpaRepository<Stores, Long> {

}
