package com.example.routeweb.models;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "file_data")
public class FileModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_file;

    @Column(name = "name_file", nullable = false, length = 255)
    private String name;

    @Column(name = "datetime_file", nullable = false)
    private Date dateTime;

    @Column(name = "size_file", nullable = false)
    private int size;

    @Column(name = "queue_file", nullable = false, length = 50)
    private String queue;

    @Column(name = "archive_file", length = 255)
    private String archive;

    @Column(name = "hash_file", length = 64)
    private String hash;

    public FileModel() {
    }

    public FileModel(String name, Date dateTime, int size, String queue, String archive) {
        this.name = name;
        this.dateTime = dateTime;
        this.size = size;
        this.queue = queue;
        this.archive = archive;
    }

    public Long getId() {
        return id_file;
    }

    public void setId(Long id) {
        this.id_file = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getArchive() {
        return archive;
    }

    public void setArchive(String archive) {
        this.archive = archive;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getHashFile() {
        return this.hash;
    }
}
