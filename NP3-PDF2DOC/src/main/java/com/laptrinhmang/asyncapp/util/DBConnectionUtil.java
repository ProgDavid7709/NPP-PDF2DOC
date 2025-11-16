package com.laptrinhmang.asyncapp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionUtil {

  
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver"; 
    private static final String DB_URL = "jdbc:mysql://localhost:3306/async_pdf_project"; 
    private static final String DB_USER = "root";
    private static final String DB_PASS = ""; 
    
    public static Connection getConnection() throws SQLException {
    	try {
            // BƯỚC THÊM VÀO: Buộc tải driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new SQLException("MySQL JDBC Driver not found!", e);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("Kết nối Database thành công!");
                System.out.println("Schema: " + conn.getCatalog());
            } else {
                System.out.println("Kết nối Database thất bại!");
            }
        } catch (SQLException e) {
            System.out.println("Lỗi kết nối: " + e.getMessage());
            e.printStackTrace();
        }
    }
}