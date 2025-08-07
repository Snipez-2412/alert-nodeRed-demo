package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alert_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer stage;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private AlertEntity alert;
}