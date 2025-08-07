package com.example.demo.repository;

import com.example.demo.entity.AlertLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertLogRepository extends JpaRepository<AlertLogEntity, Integer> {
}