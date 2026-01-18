# Community Management System

## 📋 Description

This is a community management system that allows users to create and manage communities, groups, events, and facilitate real-time messaging. The system supports attendance tracking, role-based access control, and multimedia messaging capabilities.

---

## 🚀 Features

- **User Authentication** - Secure registration and login with JWT tokens
- **Community Management** - Create and manage multiple communities
- **Group Organization** - Create groups within communities with role-based permissions
- **Event Management** - Schedule and manage events with attendance tracking
- **Real-time Messaging** - WebSocket-based chat with text and media support
- **Role-Based Access** - Owner, Admin, and Member roles with different permissions
- **Attendance Tracking** - Mark and track attendance for events
- **Media Support** - Share images, videos, PDFs, and documents

---

## 🛠️ Technology Stack

### Backend
- **Java:** Oracle JDK 21
- **Framework:** Spring Boot 3.x
- **Database:** PostgreSQL 16.x
- **ORM:** Hibernate/JPA
- **Security:** Spring Security with JWT
- **WebSocket:** STOMP over SockJS
- **Build Tool:** Maven

### Additional Libraries
- **Validation:** Jakarta Bean Validation
- **Jackson:** JSON processing

### IDE
- IntelliJ IDEA Community Edition 2024.2.4
---

## 📦 Prerequisites

Before running this application, ensure you have the following installed:

- **Java Development Kit (JDK):** Oracle JDK 21 or higher
    - Download from: https://www.oracle.com/java/technologies/downloads/#java21
    - Verify installation: `java -version`

- **PostgreSQL:** Version 16.x or higher
    - Download from: https://www.postgresql.org/download/
    - Verify installation: `psql --version`

- **Maven:** Version 3.8+ (usually bundled with IDE)
    - Verify installation: `mvn -version`

- **Python:** Version 3.8+ (for running tests)
    - Download from: https://www.python.org/downloads/
    - Verify installation: `python --version`

---

## ⚙️ Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd <project-directory>
```

### 2. Database Setup

#### Create PostgreSQL Database

```sql
-- Connect to PostgreSQL
psql -U postgres

-- Create database
CREATE DATABASE community_db;
\q
```

#### Configure Database Connection

Edit `src/main/resources/application.yaml`:

```properties
spring:

datasource:
url: jdbc:postgresql://localhost:5432/Community
username: postgres
password: nani
driver-class-name: org.postgresql.Driver

jpa:
hibernate:
ddl-auto: create
show-sql: true
properties:
hibernate:
format_sql: true
database: postgresql
database-platform: org.hibernate.dialect.PostgreSQLDialect
```

### 3. Build the Project

```bash
# Clean and install dependencies
mvn clean install

# Skip tests during build (optional)
mvn clean install -DskipTests
```

### 4. Run the Application

```bash
# Using Maven
mvn spring-boot:run

# Or using Java
java -jar target/community-management-system-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

---

## 🧪 Testing the Application

### Automated API Testing

We provide a comprehensive Python test script that tests all API endpoints.

#### Setup Python Environment

```bash
# Install required Python package
pip install requests

# On Windows
py -m pip install requests
```

#### Run the Test Script

```bash
# Navigate to test directory
python test/ApiTest/test.py > test_results.txt 2>&1
```

#### Expected Output

```
================================================================================
                          API AUTOMATED TEST SUITE
================================================================================

Testing User Registration...
✓ PASS - User Registration

Testing User Authentication...
✓ PASS - User Authentication

...

================================================================================
                             TEST SUMMARY
================================================================================

Test Results:
  Total Tests: 36
  Passed: 36
  Failed: 0

🎉 ALL TESTS PASSED! 🎉
```

---

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/register` | Register new user | No |
| POST | `/auth/authenticate` | Login user | No |

**Example Request (Register):**
```json
POST /api/auth/register
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "phone": "+1234567890"
}
```

**Example Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "userEmail": "john@example.com"
}
```

---

### User Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/user/{userId}` | Get user by ID | Yes |
| GET | `/user/email/{email}` | Get user by email | Yes |
| GET | `/user/all` | Get all users | Yes |
| PUT | `/user/{userId}` | Update user | Yes |
| DELETE | `/user/{userId}` | Delete user | Yes |
| GET | `/user/exists/{email}` | Check if email exists | Yes |

---

### Community Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/communities` | Create community | Yes |
| GET | `/communities/{id}` | Get community | Yes |
| GET | `/communities` | Get all communities | Yes |
| GET | `/communities/search?q={term}` | Search communities | Yes |
| GET | `/communities/created-by/{userId}` | Get user's communities | Yes |
| GET | `/communities/user/{userId}` | Get communities where user is member | Yes |
| PUT | `/communities/{id}` | Update community | Yes |
| DELETE | `/communities/{id}` | Delete community | Yes |

#### Community Membership Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/communities/{id}/members` | Add member | Yes |
| GET | `/communities/{id}/members` | Get all members | Yes |
| GET | `/communities/{id}/members/{userId}` | Get user membership | Yes |
| PUT | `/communities/{id}/members/{memberId}/role` | Update member role | Yes |
| DELETE | `/communities/{id}/members/{memberId}` | Remove member | Yes |

**Example Request (Create Community):**
```json
POST /api/communities
Headers: 
  Authorization: Bearer {token}
  User-Id: {userId}

{
  "name": "Tech Enthusiasts",
  "description": "A community for technology lovers"
}
```

---

### Group Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/groups` | Create group | Yes |
| GET | `/groups/{id}` | Get group | Yes |
| GET | `/groups/community/{communityId}` | Get groups by community | Yes |
| GET | `/groups/community/{communityId}/search?q={term}` | Search groups | Yes |
| GET | `/groups/created-by/{userId}` | Get user's groups | Yes |
| GET | `/groups/user/{userId}` | Get groups where user is member | Yes |
| PUT | `/groups/{id}` | Update group | Yes |
| DELETE | `/groups/{id}` | Delete group | Yes |

#### Group Membership Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/groups/{id}/members` | Add member | Yes |
| GET | `/groups/{id}/members` | Get all members | Yes |
| GET | `/groups/{id}/members/{userId}` | Get user membership | Yes |
| PUT | `/groups/{id}/members/{memberId}/role` | Update member role | Yes |
| DELETE | `/groups/{id}/members/{memberId}` | Remove member | Yes |

---

### Event Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/events` | Create event | Yes |
| GET | `/events/{id}` | Get event | Yes |
| GET | `/events/community/{communityId}` | Get events by community | Yes |
| GET | `/events/community/{communityId}/upcoming` | Get upcoming events | Yes |
| GET | `/events/group/{groupId}` | Get events by group | Yes |
| GET | `/events/group/{groupId}/upcoming` | Get upcoming group events | Yes |
| PUT | `/events/{id}` | Update event | Yes |
| DELETE | `/events/{id}` | Delete event | Yes |
| PATCH | `/events/{id}/attendance?enabled={bool}` | Toggle attendance | Yes |

**Example Request (Create Event):**
```json
POST /api/events
Headers: 
  Authorization: Bearer {token}
  User-Id: {userId}

{
  "title": "Monthly Meetup",
  "description": "Monthly tech meetup",
  "communityId": "uuid",
  "groupId": "uuid",
  "eventDate": "2025-02-15",
  "eventTime": "18:00:00",
  "location": "Tech Hub, Downtown",
  "attendanceEnabled": true
}
```

#### Event Attendance Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/events/{id}/attendance` | Mark attendance | Yes |
| GET | `/events/{id}/attendance` | Get all attendance | Yes |
| GET | `/events/{id}/attendance/group/{groupId}` | Get group attendance | Yes |
| GET | `/events/{id}/attendance/stats` | Get attendance stats | Yes |

---

### Message Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/messages` | Create message | Yes |
| GET | `/messages/{id}` | Get message by ID | Yes |
| GET | `/messages/event/{eventId}?page={n}&size={m}` | Get paginated messages | Yes |
| GET | `/messages/event/{eventId}/all` | Get all messages | Yes |
| GET | `/messages/event/{eventId}/user/{userId}` | Get user's messages | Yes |
| GET | `/messages/event/{eventId}/count` | Get message count | Yes |
| DELETE | `/messages/{id}` | Delete message | Yes |

**Example Request (Text Message):**
```json
POST /api/messages
Headers: 
  Authorization: Bearer {token}
  X-User-Id: {userId}

{
  "eventId": "uuid",
  "type": "TEXT",
  "content": "Hello everyone!",
  "mediaList": []
}
```

**Example Request (Media Message):**
```json
POST /api/messages
Headers: 
  Authorization: Bearer {token}
  X-User-Id: {userId}

{
  "eventId": "uuid",
  "type": "MEDIA",
  "content": "Check out these photos!",
  "mediaList": [
    {
      "mediaType": "IMAGE",
      "url": "https://example.com/photo.jpg",
      "sizeInBytes": 1024000,
      "width": 1920,
      "height": 1080
    },
    {
      "mediaType": "VIDEO",
      "url": "https://example.com/video.mp4",
      "sizeInBytes": 5120000,
      "width": 1280,
      "height": 720,
      "durationInSeconds": 120
    }
  ]
}
```

---

### WebSocket Endpoints

#### Connection
```
ws://localhost:8080/ws
```

#### Subscribe to Event Messages
```
Topic: /topic/event/{eventId}/messages
```

#### Subscribe to Typing Indicators
```
Topic: /topic/event/{eventId}/typing
```

#### Subscribe to Message Deletions
```
Topic: /topic/event/{eventId}/message-deleted
```

#### Send Message
```
Destination: /app/event/{eventId}/send
Payload: CreateMessageRequest
```

#### Send Typing Indicator
```
Destination: /app/event/{eventId}/typing
Payload: { userId, userName, isTyping }
```

---

## 🔐 Security

### JWT Authentication

The application uses JWT (JSON Web Tokens) for authentication. Include the token in the Authorization header:

```
Authorization: Bearer {your-jwt-token}
```

### User Identification

Some endpoints require the `User-Id` header:

```
User-Id: {user-uuid}
```

### Role-Based Access

- **OWNER:** Full control over communities/groups
- **ADMIN:** Manage members and content
- **MEMBER:** View and participate

---

## 📝 Development Guidelines

### Code Style

- Use Java naming conventions
- Follow REST API best practices
- Document all public methods
- Write unit tests for services
- Use meaningful variable names

---

## 🤝 Contributing

[Add contribution guidelines here]

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

##  Future Features

- [ ] Add email notifications
- [ ] Implement push notifications
- [ ] Add event reminders
- [ ] Create admin dashboard
- [ ] Add analytics and reporting
- [ ] Mobile app integration

---

## 🙏 Acknowledgments

- Spring Boot team for excellent framework
- PostgreSQL community
- All contributors

---

**Version:** 1.0.0  
