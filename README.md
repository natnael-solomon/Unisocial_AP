# UniSocial - Advanced Programming Project

[![Java](https://img.shields.io/badge/Java-17%2B-007396?logo=java&logoColor=white)](https://www.java.com/)
[![JavaFX](https://img.shields.io/badge/JavaFX-22.0.1-1E90FF?logo=javafx&logoColor=white)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Maven-3.8%2B-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Table of Contents
- [Project Overview](#project-overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [System Architecture](#system-architecture)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [Development](#development)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## Project Overview

UniSocial is a feature-rich desktop social networking application built with JavaFX for the client interface and a custom Java socket server for the backend. The application provides a modern, responsive user interface with real-time updates and secure communication.

## Features

### User Authentication
- Secure user registration and login
- Password hashing with BCrypt
- Session management with JWT tokens
- Remember me functionality

### Social Features
- Create, edit, and delete posts
- Like and comment on posts
- Follow/Unfollow users
- User profiles with activity feed
- Real-time notifications

### User Interface
- Modern, responsive JavaFX interface
- Theming support (Light/Dark modes)
- Rich text formatting in posts
- Image upload and display
- Smooth animations and transitions

### Technical Features
- Multi-threaded server architecture
- SQLite database for data persistence
- Custom protocol over TCP sockets
- JSON-based communication
- Comprehensive logging

## Technology Stack

### Client
- **Java 17+** - Core language
- **JavaFX 22** - UI framework
- **Maven** - Build and dependency management
- **Gson** - JSON processing
- **Ikonli** - Icon library
- **FontAwesome 6** - Icon set

### Server
- **Java 17+** - Core language
- **SQLite** - Embedded database
- **BCrypt** - Password hashing
- **SLF4J** - Logging facade
- **Gson** - JSON processing

## System Architecture

### Client Architecture
```
unisocial-client/
├── controllers/      # Application controllers
├── core/             # Core application components
├── events/           # Custom event classes
├── exceptions/       # Custom exceptions
├── models/           # Data models
├── services/         # Service layer
├── utils/            # Utility classes
└── views/            # FXML views and UI components
```

### Server Architecture
```
unisocial-server/
├── models/           # Data models
├── services/         # Business logic
├── utils/            # Utility classes
├── ClientHandler.java # Handles client connections
├── DatabaseManager.java # Database operations
├── ServerConfig.java  # Server configuration
└── UnisocialServer.java # Main server class
```

## Installation

### Prerequisites
- Java Development Kit (JDK) 17 or higher
- Maven 3.8 or higher
- Git (for version control)

### Build Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/unisocial.git
   cd unisocial
   ```

2. **Build the server**
   ```bash
   cd unisocial-server
   mvn clean package
   ```

3. **Build the client**
   ```bash
   cd ../unisocial-client
   mvn clean package
   ```

## Configuration

### Server Configuration
Edit `src/main/resources/config.properties` to configure:
- Server port
- Database path
- Logging level
- Session timeout

### Client Configuration
Edit `src/main/resources/application.properties` to configure:
- Server host and port
- Theme preferences
- Application settings

## Usage

### Starting the Server
```bash
cd unisocial-server/target
java -jar unisocial-server-1.0-SNAPSHOT.jar
```

### Starting the Client
```bash
cd unisocial-client/target
java --module-path "path/to/javafx-sdk/lib" \
     --add-modules javafx.controls,javafx.fxml \
     -jar unisocial-client-1.0-SNAPSHOT.jar
```

## Development

### Code Style
- Follow Google Java Style Guide
- Use meaningful variable and method names
- Document public APIs with Javadoc
- Keep methods focused and under 50 lines

### Branching Strategy
- `main` - Production-ready code
- `develop` - Integration branch
- `feature/*` - New features
- `bugfix/*` - Bug fixes
- `hotfix/*` - Critical production fixes

### Commit Message Convention
```
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

Types:
- feat: New feature
- fix: Bug fix
- docs: Documentation changes
- style: Code style changes
- refactor: Code refactoring
- test: Adding tests
- chore: Maintenance tasks

## API Documentation

The API uses JSON over TCP sockets with the following message format:

### Authentication
- `AUTH_LOGIN` - User login
- `AUTH_REGISTER` - User registration
- `AUTH_LOGOUT` - User logout

### Posts
- `POST_CREATE` - Create new post
- `POST_GET` - Get post by ID
- `POST_UPDATE` - Update post
- `POST_DELETE` - Delete post
- `POST_LIKE` - Like/unlike post
- `POST_COMMENT` - Add comment to post

### Users
- `USER_GET` - Get user profile
- `USER_UPDATE` - Update profile
- `USER_SEARCH` - Search users
- `USER_FOLLOW` - Follow user
- `USER_UNFOLLOW` - Unfollow user

## Testing

### Running Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TestClassName

# Run with coverage (requires JaCoCo)
mvn jacoco:prepare-agent test jacoco:report
```

### Test Coverage
- Unit tests for all service classes
- Integration tests for API endpoints
- UI tests for critical user flows
- Aim for >80% code coverage

## Deployment

### Server Deployment
1. Build the server with `mvn clean package`
2. Copy the JAR file to the server
3. Create a systemd service or use a process manager
4. Configure environment variables
5. Start the service

### Client Distribution
1. Build the client with `mvn clean package`
2. Use `jpackage` to create native installers
3. Sign the application for distribution
4. Create installation packages (.dmg, .msi, .deb, etc.)

## Troubleshooting

### Common Issues

#### Connection Issues
- Verify server is running
- Check firewall settings
- Ensure correct host/port configuration

#### Database Issues
- Verify database file permissions
- Check database schema version
- Look for locked database files

#### UI Issues
- Check JavaFX installation
- Verify resource paths
- Look for missing dependencies

### Logs
Server logs are stored in `logs/` by default. Check these files for errors:
- `server.log` - General server logs
- `error.log` - Error messages
- `access.log` - Client connections

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

Distributed under the MIT License. See `LICENSE` for more information.

## Acknowledgments
- [JavaFX](https://openjfx.io/) - For the amazing UI framework
- [Ikonli](https://kordamp.org/ikonli/) - For the icon library
- [BCrypt](https://github.com/patrickfav/bcrypt) - For secure password hashing
- [SQLite](https://sqlite.org/) - For the embedded database

---

*This project was developed as part of the Advanced Programming course.*
