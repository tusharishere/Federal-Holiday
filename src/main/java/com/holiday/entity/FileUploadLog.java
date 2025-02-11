package com.holiday.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "file_upload_log")
public class FileUploadLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileHash;
    private String fileName;
    private LocalDateTime uploadedAt;
}
