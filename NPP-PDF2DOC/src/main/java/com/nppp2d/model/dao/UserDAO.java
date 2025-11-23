package com.nppp2d.model.dao;

import com.nppp2d.model.bean.User;
import com.nppp2d.util.DBConnectionUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

public class UserDAO {

    /**
     * Hàm kiểm tra Đăng nhập
     * Tìm một user dựa trên username và password.
     *
     * @param username Tên đăng nhập
     * @param password Mật khẩu
     * @return Đối tượng User nếu tìm thấy, ngược lại trả về null.
     */
    public User validateLogin(String username, String password) {
        String sql =
            "SELECT id, username, password, email FROM users WHERE username = ?";
        User user = null;
        try (
            Connection conn = DBConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");
                    if (BCrypt.checkpw(password, hashedPassword)) {
                        user = new User();
                        user.setId(rs.getInt("id"));
                        user.setUsername(rs.getString("username"));
                        user.setEmail(rs.getString("email"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(
                "Lỗi khi xác thực người dùng: " + e.getMessage()
            );
        }
        return user;
    }

    /**

     * @param user Đối tượng User chứa thông tin (username, password, email)
     * @return true nếu tạo thành công, false nếu thất bại (ví dụ: trùng username)
     */
    public boolean createUser(User user) {
        String sql =
            "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (
            Connection conn = DBConnectionUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            // --- BƯỚC HASHING ---
            // 1. Lấy mật khẩu gốc (plain text)
            String plainTextPassword = user.getPassword();

            // 2. Tạo một "salt" ngẫu nhiên với "cost factor" là 12
            // Cost factor (10-12) càng cao, hash càng chậm, càng an toàn
            String salt = BCrypt.gensalt(12);

            // 3. Băm mật khẩu (BCrypt tự động kết hợp salt vào hash)
            String hashedPassword = BCrypt.hashpw(plainTextPassword, salt);
            // ---------------------

            ps.setString(1, user.getUsername());
            ps.setString(2, hashedPassword); // <-- LƯU HASH VÀO DB
            ps.setString(3, user.getEmail());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // main() test removed — move tests to proper unit test classes under src/test/java.
    // Leaving production classes free of ad-hoc main() test methods.
}
