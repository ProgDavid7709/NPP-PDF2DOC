package com.laptrinhmang.asyncapp.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;
import com.laptrinhmang.asyncapp.model.bean.User;
import com.laptrinhmang.asyncapp.util.DBConnectionUtil;
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
        String sql = "SELECT * FROM users WHERE username = ? ";
        User user = null;
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
        	
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                	String hashedPassword = rs.getString("password");
                	if (BCrypt.checkpw(password, hashedPassword))
                	{
                    user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                	}
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Lỗi khi xác thực người dùng: " + e.getMessage());
        }
        return user;
    }

    /**

     * @param user Đối tượng User chứa thông tin (username, password, email)
     * @return true nếu tạo thành công, false nếu thất bại (ví dụ: trùng username)
     */
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        
        try (Connection conn = DBConnectionUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
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
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();
        System.out.println("--- BẮT ĐẦU KIỂM TRA USERDAO (BCRYPT) ---");

        // --- 1. Tạo một username/password duy nhất để test ---
        // Chúng ta dùng System.currentTimeMillis() để đảm bảo username không bao giờ trùng
        String testUsername = "admin" + System.currentTimeMillis();
        String testPassword = "123";
        String testEmail = testUsername + "@test.com";

        System.out.println("Sẽ test với User: " + testUsername);
        System.out.println("Mật khẩu gốc: " + testPassword);

        try {
            // --- 2. Thử Test hàm createUser (Đăng ký) ---
            System.out.println("\nĐang test createUser()...");
            User newUser = new User();
            newUser.setUsername(testUsername);
            newUser.setPassword(testPassword); // <-- Đưa mật khẩu gốc
            newUser.setEmail(testEmail);

            boolean createSuccess = userDAO.createUser(newUser);
            
            if (createSuccess) {
                System.out.println("Thành công: Đã tạo user mới.");
            } else {
                System.out.println("Thất bại: Không tạo được user.");
                return; // Dừng test nếu không tạo được
            }

            // --- 3. Thử Test hàm validateLogin (Đăng nhập ĐÚNG) ---
            System.out.println("\nĐang test validateLogin() với mật khẩu ĐÚNG...");
            // Dùng lại mật khẩu gốc để test
            User loggedInUser = userDAO.validateLogin(testUsername, testPassword);

            if (loggedInUser != null) {
                System.out.println("Thành công: Đăng nhập thành công! Welcome, " + loggedInUser.getUsername());
            } else {
                System.out.println("Thất bại: Đăng nhập (đúng) thất bại. (LỖI LOGIC NGHIÊM TRỌNG!)");
            }

            // --- 4. Thử Test hàm validateLogin (Đăng nhập SAI) ---
            System.out.println("\nĐang test validateLogin() với mật khẩu SAI...");
            User failedUser = userDAO.validateLogin(testUsername, "matkhausai123");

            if (failedUser == null) {
                System.out.println("Thành công: Đăng nhập (sai) thất bại, đúng như mong đợi.");
            } else {
                System.out.println("Thất bại: Đăng nhập (sai) lại thành công. (LỖI LOGIC NGHIÊM TRỌNG!)");
            }

        } catch (Exception e) {
            System.out.println("\n--- LỖI NGOẠI LỆ ---");
            e.printStackTrace();
        }
        
        System.out.println("\n--- KIỂM TRA HOÀN TẤT ---");
    }
}