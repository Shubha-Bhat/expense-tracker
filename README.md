# 💰 ExpenseFlow - Personal Expense Tracker

A full-stack web application for tracking daily expenses with interactive charts, secure authentication, and real-time analytics.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1-yellow)

## 📖 Brief Description

ExpenseFlow helps users manage personal finances by tracking expenses, categorizing spending, and visualizing data through interactive charts. Features include user authentication, expense CRUD operations, category-wise analytics, weekly spending trends, and CSV export functionality.

## 🛠️ Tech Stack

**Backend:** Java 21, Spring Boot 3.2, Spring Security, Spring Data JPA, Hibernate, MySQL, Maven

**Frontend:** Thymeleaf, HTML5, CSS3, Bootstrap 5, Chart.js, Font Awesome

## 📁 Folder Structure

ExpenseTracker/
├── src/main/java/com/expensetracker/
│ ├── controller/ # AuthController, ExpenseController
│ ├── model/ # User, Expense, Category
│ ├── repository/ # UserRepository, ExpenseRepository
│ ├── service/ # ExpenseService
│ └── security/ # SecurityConfig, CustomUserDetailsService
├── src/main/resources/
│ ├── application.properties
│ └── templates/ # dashboard.html, login.html, register.html
└── pom.xml


## ⚙️ How It Works

1. **User registers/login** → Spring Security authenticates with BCrypt encrypted passwords
2. **Add expenses** → Form data sent to controller → Saved to MySQL via JPA
3. **Dashboard loads** → Fetches user-specific expenses → Calculates monthly totals
4. **Charts render** → Chart.js displays pie chart (category spending) and line chart (weekly trends)
5. **Delete/Export** → Remove expenses or download CSV

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🔐 Authentication | Secure login/register with BCrypt |
| ➕ CRUD Operations | Add, view, delete expenses |
| 🏷️ 8 Categories | Food, Transport, Shopping, Entertainment, Bills, Healthcare, Education, Other |
| 📊 Monthly Total | Sum of current month expenses |
| 📈 Daily Average | Average spending per day |
| 🥧 Pie Chart | Category-wise spending distribution |
| 📉 Line Chart | 7-day spending trend |
| 📤 CSV Export | Download expense data |
| 📱 Responsive | Works on all devices |

## 🚀 Quick Start

```bash
# 1. Clone
git clone https://github.com/shubha-bhat/expense-tracker.git

# 2. Create database
mysql -u root -p
CREATE DATABASE expensetracker;

# 3. Update application.properties
spring.datasource.password=YOUR_MYSQL_PASSWORD

# 4. Run
mvn spring-boot:run

# 5. Open browser
http://localhost:8080


