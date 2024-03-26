package com.example.routeweb.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Service
public class DatabaseLogService {

    private DataSource dataSource;

    @Autowired
    public DatabaseLogService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void saveLog(String level, String message) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "INSERT INTO logs (level, message) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, level);
                pstmt.setString(2, message);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}