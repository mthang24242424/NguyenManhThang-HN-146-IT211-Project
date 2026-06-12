    # Hackathon Java Web Service

Dự án base dành cho bài thi/hackathon môn **Java Web Service** sử dụng **Spring Boot** và **MySQL**.

Project được tổ chức theo kiến trúc phân lớp **Layered Architecture**.

```text
Controller → Service → Repository → Database
```

---

## 1. Công nghệ sử dụng

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- MySQL
- Gradle
- Spring Validation
- Spring AOP

---

## 2. Cấu trúc thư mục

```text
src/main/java/re.edu.hackathon
├── aop
├── common
├── controller
├── dto
│   ├── request
│   └── response
├── entity
├── exception
├── repository
├── service
└── HackathonApplication.java
```

---

## 3. Ý nghĩa các package

| Package        | Chức năng                                                         |
|----------------|-------------------------------------------------------------------|
| `aop`          | Chứa các class xử lý AOP, ví dụ logging hoặc đo thời gian xử lý   |
| `common`       | Chứa các class dùng chung trong toàn project, ví dụ `ApiResponse` |
| `controller`   | Nhận request từ client và định nghĩa REST API                     |
| `dto.request`  | Chứa DTO dùng để nhận dữ liệu từ client                           |
| `dto.response` | Chứa DTO dùng để trả dữ liệu về client                            |
| `entity`       | Chứa entity ánh xạ với bảng trong database                        |
| `exception`    | Chứa custom exception và class xử lý lỗi tập trung                |
| `repository`   | Chứa interface thao tác với database thông qua Spring Data JPA    |
| `service`      | Chứa logic nghiệp vụ của hệ thống                                 |

---

## 4. Luồng xử lý request

```text
Client / Postman
        ↓
Controller
        ↓
Service
        ↓
Repository
        ↓
MySQL Database
```

Ví dụ:

```text
POST /api/products
        ↓
ProductController
        ↓
ProductService
        ↓
ProductRepository
        ↓
products table
```

---

## 5. Cấu hình database

Tạo database trong MySQL:

```sql
CREATE DATABASE hackathon_db;
```

Cấu hình trong file:

```text
src/main/resources/application.properties
```

```properties
spring.application.name=hackathon

spring.datasource.url=jdbc:mysql://localhost:3306/hackathon_db
spring.datasource.username=root
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Nếu MySQL có mật khẩu, sửa lại dòng sau:

```properties
spring.datasource.password=your_password
```

---

## 6. Hướng dẫn clone dự án và phát triển theo yêu cầu đề bài

Dự án này là **base project** dùng chung cho nhiều đề bài hackathon môn Java Web Service.

Sinh viên cần clone dự án về máy, cấu hình môi trường, sau đó phát triển chức năng theo yêu cầu cụ thể của từng đề bài.

## 7. Clone dự án từ GitHub

Mở Terminal hoặc Git Bash và chạy lệnh:

```bash
git clone <repository-url>
```

Ví dụ:

```bash
git clone https://github.com/hunghx/IT211-JavaWebService-hackathon-base-project
```

## 8. Mở dự án bằng IntelliJ IDEA

Thực hiện các bước sau:

1. Mở IntelliJ IDEA.
2. Chọn **Open**.
3. Chọn thư mục dự án vừa clone.
4. Chờ IntelliJ load Gradle và tải dependencies.
5. Kiểm tra các file cấu hình và sửa lại theo đúng cấu hình của bạn nếu cần (ví dụ `application.properties`).

## 9. Chạy ứng dụng

Sau khi mở dự án, bạn có thể chạy ứng dụng bằng cách:

1. Mở file `HackathonApplication.java`.
2. Nhấn chuột phải vào file và chọn **Run 'HackathonApplication'**
3. Ứng dụng sẽ khởi động và lắng nghe trên cổng mặc định `8080`.