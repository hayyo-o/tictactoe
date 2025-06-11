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

## How to Build and Run

### Requirements

- Java 17+
- Maven 3.6+

### Build Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/hayyo-o/tictactoe.git
   ```

2. Build the project with Maven:
   ```bash
   mvn clean install
   ```

3. Run the server:
   ```bash
   java -jar server/target/tictactoe-server.jar
   ```

4. Run the client:
   ```bash
   java -jar client/target/tictactoe-client.jar
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

The application uses a custom UTF-8 text-based protocol. Each message is a line ending with `CRLF`. Messages have a keyword and space-separated parameters.

Examples:

- `HELLO pavel` → server responds `WELCOME pavel` or `ERROR UsernameTaken`
- `MOVE 1 2` → server broadcasts `MOVE pavel 1 2` and next `YOUR_TURN otherplayer`
- `QUIT` → server responds with `DISCONNECT` and possibly `WINNER` for the opponent

A full protocol description can be found in `Tymova_semestralni_prace_-_piskvorky.docx`.
