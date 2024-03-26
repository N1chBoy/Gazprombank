package com.example.routeweb.repositiries;

import com.example.routeweb.models.FileModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepo extends JpaRepository<FileModel, Long> {

    Optional<FileModel> findByName(String name);
}
