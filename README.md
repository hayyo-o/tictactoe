# Networked Tic-Tac-Toe Game (Piškvorky)

## Overview

This project is a simple multiplayer **Tic-Tac-Toe (piškvorky)** game implemented using a **client-server architecture**. It enables multiple users to connect to a central server and play 1v1 games in real time. The game follows the traditional rules of Tic-Tac-Toe on a 3x3 grid—players take turns placing their symbol (X or O), and the first to align three of their symbols horizontally, vertically, or diagonally wins.

- **Client Developer**: Pavel Liapin
- **Server Developer**: Vojtěch Anton

## Features

- Multiplayer support: many players can be connected at once.
- Matchmaking: server automatically pairs clients into 2-player games.
- Unique usernames are required for login.
- GUI-based JavaFX client application.
- Text-based UTF-8 communication protocol.
- Server logging to console and to file.
- Server handles edge cases such as duplicate usernames or player disconnects.

## Requirements

- **Java 17** or higher
- **Maven 3.6+**
- JavaFX (included in the fat JAR)

## How to Build

### From Source

1. Clone or extract the project
2. Build with Maven:
   ```bash
   mvn clean package
   ```

### Pre-built JARs

Pre-built executable JARs are available in the release package:
- `server/tictactoe-server.jar`
- `client/tictactoe-client.jar`

## How to Run

### Server

```bash
java -jar server/target/tictactoe-server.jar
```

The server will start on port 8080. Press Enter to stop.

### Client

```bash
java -jar client/target/tictactoe-client.jar
```

**Note**: The client may show module-related warnings on startup, but will function normally.

### Alternative: Run from Maven

```bash
# Server
cd server
mvn exec:java

# Client
cd client
mvn javafx:run
```
## Project Structure

- `server/` – Java server-side logic and matchmaking.
- `client/` – JavaFX GUI client with game logic and network communication.
- `api/` – Enums and utility classes used by both client and server.

## Known Issues / Limitations

- The application does not persist user data between sessions (no database).
- Game state is only maintained in-memory—if the server shuts down, all games are lost.
- GUI layout adapts to screen size but has basic style; advanced responsiveness could be improved.

## Deviations from Assignment

- **Login without password**: The original assignment allows for username and optional password; currently only usernames are supported.
- **No game timeout handling**: The game waits indefinitely for a move unless a player disconnects manually.
- **Minimal error UI on the client**: Errors are displayed via alert dialogs but not shown inline or color-coded.
- **Limited screen transitions**: The login and game screens exist within the same JavaFX window and are toggled; future updates may split views or use transitions for better UX.

## Communication Protocol

The application uses a custom UTF-8 text-based protocol:

### Client → Server Messages
- `HELLO <username>` - Login request
- `OK` - Ready confirmation (not used in current version)
- `MOVE <x> <y>` - Make a move at position (x,y)
- `QUIT` - Disconnect from game

### Server → Client Messages
- `WELCOME <username>` - Login accepted
- `START <player1> <player2>` - Game started
- `YOUR_TURN <username>` - Your turn to move
- `MOVE <username> <x> <y>` - Move notification
- `WINNER <username>` - Game won
- `DRAW` - Game ended in draw
- `ERROR <message>` - Error occurred
- `DISCONNECT` - Opponent disconnected
