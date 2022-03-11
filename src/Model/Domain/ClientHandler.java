package Model.Domain;

import RMIinterfaces.RmiClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler {
    private RmiClient client;
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    private String username;
    private int id;
    private int playAgain;
    private Character characterChoice;
    private double[] coordinates;
    private double distance;
    private double bulletsShotInTheGame;
    private boolean alive;
    private boolean isWinner;

    /**
     * Three arguments constructor assigns RmiClient, player ID and username of the player. It sets distance made to 0, bulletsShotInTheGame to 0, alive boolean to false and isWinner boolean to false.
     *
     * @param client   RmiClient of the player
     * @param id       ID of the player
     * @param username Username of the player
     */

    public ClientHandler(RmiClient client, int id, String username) {
        this.client = client;
        this.id = id;
        this.username = username;
        distance = 0;
        bulletsShotInTheGame = 0;
        playAgain = 0;
        alive = false;
        isWinner = false;
    }

    /**
     * Gets BufferedReader assigned to the socket.
     *
     * @return BufferedReader
     */
    public BufferedReader getIn() {
        return in;
    }

    /**
     * If player is alive informs either there is a message in the buffer.
     *
     * @return
     */
    public boolean isBufferedReaderReady() {
        if (alive) {
            try {
                return in.ready();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Gets username of the player.
     *
     * @return username
     */

    public String getUsername() {
        return username;
    }

    /**
     * Gets ID of the player.
     *
     * @return id
     */

    public int getId() {
        return id;
    }

    /**
     * Gets player's character choice.
     *
     * @return characterChoice
     */

    public Character getCharacterChoice() {
        return characterChoice;
    }

    /**
     * Set player's character choice.
     *
     * @param characterChoice Desired character
     */
    public void setCharacterChoice(Character characterChoice) {
        this.characterChoice = characterChoice;
    }

    /**
     * Gets current coordinates of the player.
     *
     * @return coordinates
     */

    public double[] getCoordinates() {
        return coordinates;
    }

    /**
     * Sets coordinates for the player.
     *
     * @param coordinates New coordinates
     */

    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }

    /**
     * Assigns new socket. Creates PrintWriter and BufferedReader for this socket.
     *
     * @param socket New socket
     */

    public void setSocket(Socket socket) {
        this.socket = socket;
        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets RmiClient.
     *
     * @return client
     */

    public RmiClient getClient() {
        return client;
    }

    /**
     * Write a message through TCP connection.
     *
     * @param text Message
     */
    public void write(String text) {
        out.println(text);
    }

    /**
     * Flush the PrintWriter manually.
     */
    public void flush() {
        out.flush();
    }

    /**
     * Gets distance made during the game and resets it to 0.
     *
     * @return distance
     */
    public double getAndResetDistance() {
        double distanceAll = distance;
        distance = 0;
        return distanceAll;
    }

    /**
     * Get distance during the game.
     *
     * @return distance
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Update amount of bullets shot in the game.
     */

    public void bulletShot() {
        bulletsShotInTheGame++;
    }

    /**
     * Get amount of bullets shot in the game and reset the variable.
     *
     * @return bulletsShotInTheGame
     */
    public double getAndResetBullets() {
        double bullets = bulletsShotInTheGame;
        bulletsShotInTheGame = 0;
        return bullets;
    }

    /**
     * Get amount of bullets shot in the game
     *
     * @return bulletsShotInTheGame
     */
    public double getBulletsShotInTheGame() {
        return bulletsShotInTheGame;
    }

    /**
     * Set the player alive or dead.
     *
     * @param alive boolean informing about state of the player
     */

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    /**
     * Updates player's current coordinates and updates distance variable
     *
     * @param x Amount of distance made on X axis since last update
     * @param y Amount of distance made on Y axis since last update
     */

    public void updateCoordinates(double x, double y) {
        coordinates[0] += x;
        coordinates[1] += y;
        addDistance(x, y);
    }

    /**
     * Informs either the player is a winner or not.
     *
     * @return isWinner
     */

    public boolean isWinner() {
        return isWinner;
    }

    /**
     * Marks player as a winner.
     */

    public void setWinner(boolean heh) {
        isWinner = heh;
    }

    /**
     * Closes socket responsible for TCP connection on server and client side.
     */

    public void closeSocket() {
        try {
            client.disconnectFromTheGame();
            socket.close();
            playAgain = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	/**
	 * If player is in decision making state play again is equal 0. If he wants to play again with the same opponent it is equal 1. If player is not interested in playing it is equal -1.
	 *
	 * @param playAgainWithTheSamePlayer
	 * 			boolean informing whether player wants to play again with the same player
	 */

	public void playAgain(boolean playAgainWithTheSamePlayer) {
    	if(playAgainWithTheSamePlayer) {
    		playAgain = 1;
		}
		else {
			playAgain = -1;
		}
	}

	/**
	 * Return information whether player wants to play again with the same opponent or not
	 *
	 * @return playAgain
	 */

	public int getPlayAgain() {
		return playAgain;
	}

	/**
	 * Restores play again to default value
	 *
	 */

	public void defaultPlayAgain() {
		playAgain = 0;
	}

	/**
     * Updates distance variable.
     *
     * @param x Amount of distance made on X axis since last update
     * @param y Amount of distance made on Y axis since last update
     */

    private void addDistance(double x, double y) {
        if (coordinates != null) {
            double distanceX = Math.abs(coordinates[0] - x);
            double distanceY = Math.abs(coordinates[1] - y);
            distance += distanceX + distanceY;
        }
    }

}
