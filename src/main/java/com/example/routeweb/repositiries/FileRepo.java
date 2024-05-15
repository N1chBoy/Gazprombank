package com.example.routeweb.repositiries;

import com.example.routeweb.models.FileModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepo extends JpaRepository<FileModel, Long> {

    Optional<FileModel> findByName(String name);

    int countByQueue(String queue);

    @Query(value = "SELECT MIN(dateTime) FROM FileModel")
    String findEarliestDate();

    @Query(value = "SELECT MAX(dateTime) FROM FileModel")
    String findLatestDate();

    @Query("SELECT f FROM FileModel f WHERE f.dateTime >= :start AND f.dateTime < :end")
    List<FileModel> findByDateRange(@Param("start") Date start, @Param("end") Date end);
}
