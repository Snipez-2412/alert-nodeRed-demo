package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertEntity {

    @Id
    @Column(name = "event_id")
    private String eventId;

    private String message;
    private String timestamp;
    private String cameraName;
    private String area;
    private String temperature;
    private String filename;
    private String filetype;

    @Lob
    private String filedata;
}