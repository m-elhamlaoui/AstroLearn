# AstroLearn: Collaborative Space Exploration Hub

## Project Overview
AstroLearn is a comprehensive platform designed for space enthusiasts to explore, learn, and contribute to the space exploration community. The application provides a rich set of features including user-generated content, interactive learning resources, and AI-powered assistance to create an engaging and educational experience.

## Core Features

### User Management
- **Authentication System**: Secure JWT-based authentication with token refresh capabilities
- **User Profiles**: Customizable profiles with interests, achievements, and contribution history
- **Role-Based Access Control**: Different permission levels for regular users, content creators, and administrators
- **Verification System**: Three-tier verification status (Unverified, Pending, Verified) with admin approval process
- **Experience & Leveling System**: Users earn experience points for platform activities and progress through ranks:
  - Novice (0 XP) → Explorer (1,000 XP) → Astronaut (5,000 XP) → Galactic (10,000 XP)

### Content Management

#### Articles System
- **Creation & Publishing**: Rich text editor with support for images, videos, and formatting
- **Content Moderation**: AI-powered verification system to ensure quality and relevance
- **Interaction**: Voting, commenting, and sharing functionality
- **Search & Filter**: Advanced search with filters for topics, authors, and popularity
- **Reading History**: Tracks user engagement with articles for personalized recommendations

#### Courses Platform
- **Structured Learning Paths**: Multi-module courses with progressive difficulty levels
- **Interactive Content**: Video lessons, quizzes, and practical exercises
- **Progress Tracking**: Course completion metrics and achievement badges
- **Quiz System**: Interactive assessments with immediate feedback and scoring

### Interactive Features

#### Space Events Calendar
- **Mission Tracking**: Comprehensive database of past, current, and upcoming space missions
- **Interactive Timeline**: Visual representation of space exploration history
- **Mission Management**: Only verified users can add or edit mission information
- **Status Tracking**: Missions categorized as Upcoming, In-Progress, Completed, or Failed

#### AI Chatbot Assistant
- **Knowledge Base**: LLM trained on space exploration data to answer user questions


## Technical Architecture

### Frontend
- **Framework**: Next.js with React for a responsive single-page application
- **State Management**: React Context API and custom hooks
- **UI Components**: Custom component library with responsive design
- **API Integration**: Axios for RESTful API communication with the backend

### Backend
- **Framework**: Spring Boot for robust API development
- **API Design**: RESTful architecture with comprehensive endpoint documentation
- **Security**: JWT authentication, CORS configuration, and input validation
- **Business Logic**: Service-oriented architecture with clear separation of concerns

### Database
- **RDBMS**: PostgreSQL for structured data storage
- **Schema Design**: Normalized database schema with efficient relationships
- **Data Access**: JPA/Hibernate for object-relational mapping
- **Performance**: Optimized queries and indexing for fast data retrieval

### Media Storage
- **Cloud Storage**: AWS S3 integration for storing images
## Development Setup

### Prerequisites
- Java 17 or higher
- Node.js 16 or higher
- PostgreSQL 13 or higher
- Maven 3.8 or higher

### Backend Setup
```bash
# Navigate to backend directory
cd Backend/demo

# Build the application
./mvnw clean package

# Run the application
./mvnw spring-boot:run
```

### Frontend Setup
```bash
# Navigate to frontend directory
cd Frontend

# Install dependencies
npm install

# Run development server
npm run dev
```

