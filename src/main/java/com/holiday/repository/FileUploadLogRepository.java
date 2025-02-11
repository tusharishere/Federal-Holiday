package com.holiday.repository;

import com.holiday.entity.FileUploadLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileUploadLogRepository extends JpaRepository<FileUploadLog, Long> {
    boolean existsByFileHash(String fileHash);
}
