# GenAccessApp - Role-Based Access Control System

GenAccessApp is a robust Role-Based Access Control (RBAC) system built with Spring Boot. It provides a flexible and secure way to manage user access to resources based on roles and permissions.

## Features

- **Multi-tenant Architecture**: Support for multiple clients with isolated user and role management
- **Hierarchical Access Control**: Super Admin, Client Admin, and User roles with appropriate permissions
- **JWT Authentication**: Secure token-based authentication system
- **RESTful API**: Comprehensive API for user, role, permission, and client management
- **Database Integration**: MySQL database integration with JPA/Hibernate

## Technology Stack

- Java 17
- Spring Boot 3.x
- Spring Security with JWT
- Spring Data JPA
- MySQL Database
- Maven

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher

### Database Setup

1. Create a MySQL database named `genaccessapp_db`
2. Update the database configuration in `src/main/resources/application.properties` if needed

### Running the Application

```bash
# Clone the repository
git clone https://github.com/yourusername/genaccessapp.git
cd genaccessapp

# Build the application
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on port 8080 by default.

## Default Credentials

The application is initialized with the following default user:

- **Username**: superadmin
- **Password**: admin123
- **Role**: SUPER_ADMIN

## API Endpoints

### Authentication

- `POST /api/auth/signin` - Authenticate a user and get JWT token
- `POST /api/auth/signup` - Register a new user (requires admin privileges)

### User Management

- `GET /api/users` - Get all users (admin only)
- `GET /api/users/{id}` - Get user by ID (admin only)
- `POST /api/users` - Create a new user (admin only)
- `PUT /api/users/{id}` - Update a user (admin only)
- `DELETE /api/users/{id}` - Delete a user (admin only)

### Role Management

- `GET /api/roles` - Get all roles (admin only)
- `GET /api/roles/{id}` - Get role by ID (admin only)
- `POST /api/roles` - Create a new role (admin only)
- `PUT /api/roles/{id}` - Update a role (admin only)
- `DELETE /api/roles/{id}` - Delete a role (admin only)

### Permission Management

- `GET /api/permissions` - Get all permissions
- `GET /api/permissions/{id}` - Get permission by ID
- `POST /api/permissions` - Create a new permission (super admin only)
- `PUT /api/permissions/{id}` - Update a permission (super admin only)
- `DELETE /api/permissions/{id}` - Delete a permission (super admin only)

### Client Management

- `GET /api/clients` - Get all clients (super admin only)
- `GET /api/clients/{id}` - Get client by ID (super admin only)
- `POST /api/clients` - Create a new client (super admin only)
- `PUT /api/clients/{id}` - Update a client (super admin only)
- `DELETE /api/clients/{id}` - Delete a client (super admin only)

## Security Model

### Roles

- **SUPER_ADMIN**: Has complete access to all resources and can manage clients
- **CLIENT_ADMIN**: Can manage users and roles within their assigned client
- **USER**: Has limited access based on assigned permissions

### Permissions

Permissions are granular access controls that can be assigned to roles:

- user_read, user_write, user_delete
- role_read, role_write, role_delete
- client_read, client_write, client_delete

## License

This project is licensed under the MIT License - see the LICENSE file for details.