# PlayTrack - Media Tracker Desktop Application

PlayTrack is a polished Java Swing desktop application for tracking your films, games, and books. It features a modern UI, animated authentication screens, and local SQLite database storage.

## Features

- **Animated Auth Screen**: Smooth sliding animation between Login and Register.
- **MVC Architecture**: Clean separation of concerns.
- **Dual Database System**: Separate databases for authentication (`auth.db`) and system data (`playtrack.db`).
- **Modern UI**: Built with FlatLaf for a premium desktop look.
- **Media Tracking**: Track Films, Games, and Books with categories, genres, and ratings.
- **Summary Dashboard**: View your activity stats and top genres.
- **Profile Management**: Customize your profile and bio.

## Tech Stack

- **Language**: Java 11+
- **UI Framework**: Java Swing
- **Look & Feel**: FlatLaf
- **Database**: SQLite with JDBC
- **Architecture**: MVC (Model-View-Controller)

## Project Structure

```text
PlayTrack/
├── bin/                # Compiled class files
├── db/                 # SQLite database files
├── lib/                # External JAR dependencies
├── src/                # Source code
│   └── com/playtrack/
│       ├── app/        # Entry point
│       ├── config/     # Database configurations
│       ├── dao/        # Data Access Objects
│       ├── model/      # Data models
│       ├── service/    # Business logic
│       ├── ui/         # UI components and panels
│       └── util/       # Utility classes
├── README.md           # Project documentation
└── run.sh              # Execution script
```

## How to Run

### Prerequisites
- Java JDK 11 or higher installed.

### Running the Application
1. Open a terminal in the project root directory.
2. Make the run script executable:
   ```bash
   chmod +x run.sh
   ```
3. Run the application:
   ```bash
   ./run.sh
   ```

## Development

The project is ready for VS Code or any Java IDE. Simply add the JARs in the `lib` folder to your project's classpath.
