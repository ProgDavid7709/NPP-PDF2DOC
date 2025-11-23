# NPP-PDF2DOC

README hướng dẫn cài đặt và chạy project trên Eclipse.

## Tổng quan
Project này là một ứng dụng Maven web (chạy trên Tomcat). Dữ liệu cơ sở và file kết quả khi upload được lưu ra ổ đĩa theo cấu hình local (xem mục Note bên dưới).

## Yêu cầu trước
- Java (phiên bản tương thích với project — thường Java 8+)
- Eclipse IDE for Enterprise Java and Web Developers
- Apache Tomcat 9.0 (đã tải và cấu hình trong Eclipse)
- MySQL (hoặc MySQL Server)
- Maven (Eclipse có tích hợp Maven bằng m2e)

## 0) Import cơ sở dữ liệu
1. Tìm file `database.sql` trong thư mục dự án.
2. Import file `database.sql` vào MySQL bằng MySQL Workbench, phpMyAdmin

## 1) Import project vào Eclipse
1. Mở Eclipse.  
2. Chọn `File > Import...`  
3. Chọn `Existing Maven Projects` (thường nằm trong `Maven`), nhấn `Next`.  
4. Ở phần `Root Directory`, bấm `Browse...` và chọn folder:
   ```
   NPP-PDF2DOC/NPP-PDF2DOC/
   ```
   (thư mục chứa `pom.xml` của project).  
5. Eclipse sẽ tìm `pom.xml` và liệt kê project. Chọn project rồi nhấn `Finish`.

> Đảm bảo Tomcat 9.0 đã được cài và thêm vào Eclipse trước khi chạy project (Server Runtime).

## 2) Cập nhật Maven
1. Sau khi import, chuột phải vào project trong `Project Explorer`.  
2. Chọn `Maven > Update Project...`  
3. Nếu cần, hãy bật tùy chọn `Force Update of Snapshots/Releases`, không thì để mặc định.
4. Đợi Eclipse tải phụ thuộc và xây dựng project.

## 3) Chạy trên Tomcat
1. Chuột phải vào project -> `Run As > Run on Server`.  
2. Chọn server `Apache Tomcat v9.0` (hoặc server Tomcat 9 bạn đã cấu hình).  
3. Nhấn `Finish`. Eclipse sẽ deploy project lên Tomcat và mở trình duyệt tới URL ứng dụng (thường `http://localhost:8080/<context>`).

## Cấu hình đường dẫn lưu trữ kết quả và dữ liệu upload
- File kết quả và dữ liệu upload được lưu trong:
  ```
  E:/nppp2d_saves
  ```

## Gợi ý xử lý lỗi thường gặp
- Nếu không thấy server Tomcat trong danh sách khi `Run on Server`: vào `Window > Preferences > Server > Runtime Environments` và thêm `Apache Tomcat v9.0`.
- Lỗi kết nối DB: kiểm tra `database.sql` đã import, tên database, user, mật khẩu và URL kết nối trong cấu hình project.
- Lỗi Maven: chạy `Maven > Update Project` hoặc kiểm tra `Problems` view để biết chi tiết.
