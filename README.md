#  Banking Management System - Backend

This is a secure, scalable Banking Management System backend built with **Spring Boot**. It supports features like **user authentication**, **account management**, **fund transactions**,  and **audit logging** for administrative traceability.

---

##  Features

###  User Management
- User Registration and Login (with JWT)
- Role-based access control (Customer, Bank Staff, Admin)
- Password encryption using BCrypt

###  Account Management
---
- Create Savings/Current Accounts
- Account approval by Admin/Bank Staff
- Account statuses: Active, Frozen, Closed

###  Transaction Management
---
- Deposit & Withdraw
- Internal Fund Transfers
- Double-entry transaction logs
- Audit logging for all actions



###  Audit Log
---
- Tracks login, transactions, account updates
- Useful for admin reviews & system monitoring

---

## 🛠️ Tech Stack

| Layer | Technology |
|------|------------|
| Backend | Spring Boot |
| Security | Spring Security, JWT |
| ORM | Spring Data JPA (Hibernate) |
| Database | H2 / Postgresql |
| Build Tool | Maven |



---


---

##  Getting Started

### 1. Clone the Repository
<pre>
git clone https://github.com/Lotiyad/Banking-System.git
cd banking
  </pre>
### 2.Configure Application Properties
<pre>
spring.datasource.url=jdbc:mysql://localhost:3306/BMS
spring.datasource.username=root
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true


</pre>
### 3. Build & Run
<pre>
  ./mvnw spring-boot:run

</pre>




