package serverController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import serverEnumUtils.ServerMessageBuilder;
import stateEnum.State;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages the lifecycle of a single Tic Tac Toe game between two clients.
 * <p>
 * Handles move logic, win/draw detection, turn notifications, and clean-up on termination.
 * Uses simple synchronization on an internal lock to coordinate move readiness and turns.
 * </p>
 *
 * @version 1.0
 * @created April 2025
 */
public class GameManager implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(GameManager.class);

    private Connection playerCross;
    private Connection playerCircle;
    private Server server;

    private boolean crossReady = false;
    private boolean circleReady = false;

    private int n = 3;
    private State[][] board = new State[n][n];

    // ----------------------------------------- THREADING DANGER TODO Sync and stuff
    private int moveCounter = 0;
    private boolean crossMove = true;
    private final Object lock = new Object();
    private boolean wait = true;
    boolean gameRunning = true;
    // -----------------------------------------


    /**
     * Creates a new GameManager for two players and immediately starts the game.
     * Randomly assigns who plays X (cross) and O (circle), initializes the board,
     * notifies both players of the START and first turn.
     *
     * @param player1 one client connection
     * @param player2 the other client connection
     * @param server  reference to the central Server for cleanup
     */
    public GameManager(Connection player1, Connection player2, Server server) {
        this.server = server;

        log.info("Designating states to players");

        // Randomly decide who is cross
        if (ThreadLocalRandom.current().nextBoolean()) {
            this.playerCross = player1;
            this.playerCircle = player2;
        } else {
            this.playerCross = player2;
            this.playerCircle = player1;
        }

        // Initialize board
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                board[i][j] = State.BLANK;
            }
        }

        // Link back
        playerCross.setGameManager(this);
        playerCircle.setGameManager(this);

        // Notify both players
        log.debug("Sending start messages to players");
        String startMessage = ServerMessageBuilder.start(playerCross.getName(), playerCircle.getName());
        playerCross.sendMessage(startMessage);
        playerCircle.sendMessage(startMessage);

        // Notify first turn
        if (crossMove) {
            playerCross.sendMessage(ServerMessageBuilder.turn(playerCross.getName()));
        } else {
            playerCircle.sendMessage(ServerMessageBuilder.turn(playerCircle.getName()));
        }
    }

    //https://stackoverflow.com/questions/1056316/algorithm-for-determining-tic-tac-toe-game-over

    /**
     * Checks the given move for a win condition after placing the symbol.
     *
     * @param x row index (0-based)
     * @param y column index (0-based)
     * @param s the State symbol (X or O)
     * @return the winning State if this move wins the game; otherwise null
     */
    private State move(int x, int y, State s) {
        if(board[x][y] == State.BLANK) {
            board[x][y] = s;
        }
        moveCounter++;

        for(int i = 0; i < n; i++) {
            if(board[x][i] != s)
                break;
            if(i == n-1) {
                return s;
            }
        }
        for(int i = 0; i < n; i++) {
            if(board[i][y] != s)
                break;
            if(i == n-1) {
                return s;
            }
        }

        if(x == y) {
            for(int i = 0; i < n; i++) {
                if(board[i][i] != s)
                    break;
                if(i == n-1) {
                    return s;
                }
            }
        }

        if(x+y == n - 1) {
            for(int i = 0; i < n; i++) {
                if(board[i][(n-1)-i] != s)
                    break;
                if(i == n-1) {
                    return s;
                }
            }
        }

        return null;
    }

    /**
     * Processes a player's move request:
     * validates turn order and coordinates, updates the board,
     * broadcasts the move, and handles win/draw logic.
     *
     * @param player the Connection issuing the move
     * @param x      row index of the move
     * @param y      column index of the move
     */
    public void playerMove(Connection player, int x, int y) {
        synchronized (lock) {
            if (x >= n || y >= n || x < 0 || y < 0) {
                player.sendMessage(ServerMessageBuilder.error("Invalid position"));
                return;
            }

            if (board[x][y] != State.BLANK) {
                player.sendMessage(ServerMessageBuilder.error("Cell already occupied"));
                return;
            }

            if ((player == playerCross && crossMove) || (player == playerCircle && !crossMove)) {

                State symbol = (player == playerCross) ? State.X : State.O;
                State winner = move(x, y, symbol);

                // Send move first
                sendMoveToBothPlayers(player.getName(), x, y);

                // Winner check then
                if (winner != null) {
                    String winMessage = ServerMessageBuilder.winner(player.getName());
                    playerCross.sendMessage(winMessage);
                    playerCircle.sendMessage(winMessage);
                    gameRunning = false;
                } else if (moveCounter == n * n) {
                    String drawMessage = ServerMessageBuilder.draw();
                    playerCross.sendMessage(drawMessage);
                    playerCircle.sendMessage(drawMessage);
                    gameRunning = false;
                } else {
                    crossMove = !crossMove;
                    wait = false;
                    lock.notifyAll();
                }

            } else {
                player.sendMessage(ServerMessageBuilder.error("Not your move"));
            }
        }
    }

    /**
     * Sends a MOVE notification to both players.
     *
     * @param playerName the name of the player who moved
     * @param x          row index of the move
     * @param y          column index of the move
     */
    private void sendMoveToBothPlayers(String playerName, int x, int y) {
        String moveMessage = ServerMessageBuilder.move(playerName, x, y);
        playerCross.sendMessage(moveMessage);
        playerCircle.sendMessage(moveMessage);
    }

    /**
     * Marks a player as ready; once both players are ready,
     * notifies the main game loop to proceed.
     *
     * @param player the Connection that sent OK
     */
    public void playerReady(Connection player) {
        synchronized (lock) {
            if(player == playerCross) {
                crossReady = true;
            }
            else {
                circleReady = true;
            }
            if(crossReady && circleReady) {
                log.info("Both players ready");
                wait = false;
                lock.notifyAll();
            }
        }
    }

    /**
     * Main game loop: waits for readiness or move completion,
     * then notifies the appropriate player that it’s their turn.
     */
    @Override
    public void run() {
        while (gameRunning) {
            synchronized (lock) {
                log.info("Waiting for player move");
                while (wait && gameRunning) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("GameManager thread interrupted");
                        return;
                    }
                }

                if (!gameRunning) break;

                if (crossMove) {
                    playerCross.sendMessage(ServerMessageBuilder.turn(playerCross.getName()));
                } else {
                    playerCircle.sendMessage(ServerMessageBuilder.turn(playerCircle.getName()));
                }

                wait = true;
            }
        }

        log.info("Game ended, clearing GameManager from players.");
        if (playerCross != null) playerCross.setGameManager(null);
        if (playerCircle != null) playerCircle.setGameManager(null);
    }

    /**
     * Returns both participating connections.
     *
     * @return a Set containing the two Connection objects
     */
    public Set<Connection> getConnections() {
        Set<Connection> connections = new HashSet<>();
        connections.add(playerCross);
        connections.add(playerCircle);
        return connections;
    }

    /**
     * Indicates whether the game is still running.
     *
     * @return true if the game has not yet ended
     */
    public synchronized boolean isGameRunning() {
        return gameRunning;
    }

    /**
     * Forces termination of the game loop and unblocks any waiting threads.
     */
    public synchronized void terminate() {
        gameRunning = false;
        synchronized (lock) {
            wait = false;
            lock.notifyAll();
        }
    }

    /**
     * Handles a player quitting mid-game:
     * ends the game, awards the other player a win, notifies of disconnect,
     * cleans up references, and removes the quitting player.
     *
     * @param player the Connection that quit
     */
    public void quit(Connection player) {
        log.info("User {} sent quit to GameManager", player.getName());

        if (!gameRunning) {
            log.info("Game already ended. No need to process quit logic.");
            return;
        }

        terminate();

        Connection otherPlayer = (player == playerCross) ? playerCircle : playerCross;

        playerCross.setGameManager(null);
        playerCircle.setGameManager(null);

        if (otherPlayer != null && otherPlayer.getReady()) {
            otherPlayer.sendMessage(ServerMessageBuilder.winner(otherPlayer.getName()));
            otherPlayer.sendMessage(ServerMessageBuilder.disconnect());
        }

        player.terminate();
        server.removeConnection(player);
    }

}