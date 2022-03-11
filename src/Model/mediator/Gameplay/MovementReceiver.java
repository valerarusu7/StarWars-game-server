package Model.mediator.Gameplay;

import Model.mediator.GameManagement.GameRoomManager;
import Model.Domain.*;
import Model.Domain.Packages.GameStatisticsPackage;
import Model.Domain.Packages.InGamePackage;
import Model.Domain.Packages.PersonalStatisticsPackage;
import com.google.gson.Gson;
import utility.observer.listener.GeneralListener;
import utility.observer.subject.LocalSubject;
import utility.observer.subject.PropertyChangeProxy;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class responsible for receiving packages with movement and bullets
 */

public class MovementReceiver implements Runnable, LocalSubject<Object, Object> {

    private String msg;
    private Set<ClientHandler> userList;
    private Gson json;
    private boolean running;
    private boolean playAgain;
    private boolean decidingIfWantToPlayAgain;
    private List<BulletServer> bulletsServer;
    private PropertyChangeProxy<Object, Object> subject;
	private double[][] initialPositions;

    /**
     * No argument constructor that initializes set with users, Gson, boolean which is true as long as the game is running, list with all the bullets and listener.
     */

    public MovementReceiver() {
        this.userList = new HashSet<>();
        this.msg = null;
        json = new Gson();
        running = true;
        decidingIfWantToPlayAgain = true;
        playAgain = true;
        bulletsServer = new ArrayList<>();
        this.subject = new PropertyChangeProxy<>(this, true);
    }

    /**
     * Implementation of run method which calls receiveMovement method using instance variable of Set of Client Handlers
     */

    @Override
    public void run() {
        initialPositions = new double[userList.size()][3];
        int z = 0;
        for(ClientHandler clientHandler : userList) {
			initialPositions[z][0] = clientHandler.getCoordinates()[0];
			initialPositions[z][1] = clientHandler.getCoordinates()[1];
			initialPositions[z][2] = clientHandler.getId();
			z++;
		}
    	receiveMovement(userList);
    }


    /**
     * Adds player to the userList set
     *
     * @param player player who needs to be added to the list
     */

    public void addPlayerToGame(ClientHandler player) {
        userList.add(player);
    }

    /**
     * Sets the winner of the game, informs all players about the winner and loser, sets boolean running to false in order to stop receiving information from the client
     *
     * @param playerKilledId  id of the player who lost
     * @param playerWhoShotId id of the player who won
     * @see ClientHandler#getId()
     * @see ClientHandler#setWinner(boolean winner)
     * @see ClientHandler#getClient()
     */
    public void playerHit(int playerKilledId, int playerWhoShotId) {
        for (ClientHandler clientHandler : userList) {
//			This statement will disable player ability to move
//			if(playerKilledId == clientHandler.getId()) {
//				clientHandler.setAlive(false);
//			}
            if (playerWhoShotId == clientHandler.getId()) {
                clientHandler.setWinner(true);
            }
            try {
                clientHandler.getClient().playerDied(playerKilledId, playerWhoShotId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        running = false;
    }

    /**
     * Sets all players alive and starts BulletSimulation. As long as the game is running loops through all the alive players in order to check if there are any messages in the buffer. Reads the message and
     * checks if it's information about the bullet or the movement based on velocity variable. If it's null it means it is movement package. If it's a bullet, instance of BulletServer is created and added
     * to bulletsServer list. Player statistics about amount of bullets shot in the game are updated and Thread responsible for sending this message to all the players is executed. If it is a movement package
     * boundaries method is called in order to check if players is within game borders. If his next move is considered legal position of the player and statistics about the distance made are updated.
     * Thread responsible for sending this message to all the players is executed When the game is finished statistics of all players are gathered and send to both database and players. In the end socket
     * connection with all players is closed
     *
     * @param userList
	 * 		Set of all players in the game
     * @see ClientHandler#setAlive(boolean)
     * @see ClientHandler#isBufferedReaderReady()
     * @see ClientHandler#getIn()
     * @see InGamePackage#getId()
     * @see ClientHandler#bulletShot()
     * @see ClientHandler#updateCoordinates(double, double)
     * @see ClientHandler#isWinner()
     * @see ClientHandler#getAndResetBullets()
     * @see ClientHandler#getAndResetDistance()
     * @see ClientHandler#closeSocket()
     * @see ClientHandler#getClient()
     * @see InGamePackage#getCoordinates()
     * @see InGamePackage#getVelocity()
     * @see #boundaries(ClientHandler, InGamePackage)
     */

    private void receiveMovement(Set<ClientHandler> userList) {
    	while(playAgain) {
			running = true;
			try {
				//Mark all players alive
				for (ClientHandler clientHandler : userList) {
					clientHandler.setAlive(true);
				}
				GameRoomManager.executorService.execute(new BulletsSimulation(this, userList, bulletsServer));
				do {
					synchronized (userList) {
						for (ClientHandler clientHandler : userList) {
							//Checking if message is in hte buffer and if player is alive
							if (clientHandler.isBufferedReaderReady()) {
								msg = clientHandler.getIn().readLine();
								InGamePackage data = json.fromJson(msg, InGamePackage.class);
								//If code will reach this if statement it means that bullet was shot
								if (data.getVelocity() != null) {
									bulletsServer.add(new BulletServer(data.getId(), data.getCoordinates(), data.getVelocity()));
									clientHandler.bulletShot();
									GameRoomManager.executorService.execute(new MovementWriter(msg, userList));
								}
								//If code will reach this else statement it means that player is moving
								else {
									if (boundaries(clientHandler, data)) {
										clientHandler.updateCoordinates(data.getCoordinates()[0], data.getCoordinates()[1]);
										GameRoomManager.executorService.execute(new MovementWriter(msg, userList));
									}
								}
							}
						}
					}
				} while (isRunning());
			} catch (IOException e) {
				e.printStackTrace();
			}
			sendGameStatistics();
			sendPersonalStatistics();
			while(decidingIfWantToPlayAgain) {
				int[] decisions = new int[userList.size()];
				int i = 0;
				for (ClientHandler clientHandler : userList) {
					decisions[i] = clientHandler.getPlayAgain();
					i++;
				}
				decidingIfWantToPlayAgain = false;
				for(int y = 0; y < decisions.length; y++) {
					if(decisions[y] == 0) {
						decidingIfWantToPlayAgain = true;
					}
					else if(decisions[y] == -1) {
						playAgain = false;
					}
				}
			}
			decidingIfWantToPlayAgain = true;
			if(playAgain) {
				playAgain();
			}
		}
		for(ClientHandler clientHandler : userList) {
			try {
				clientHandler.getClient().gameAborted();
				clientHandler.closeSocket();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
    }

    /**
     * Check if player is moving within boundaires of the game and doesn't allow him to leave the map.
     *
     * @param clientHandler Player who is moving
     * @param data          Desired new position
     * @return true if movement is legal
     * @see ClientHandler#getCoordinates()
     */
    private boolean boundaries(ClientHandler clientHandler, InGamePackage data) {
        return (clientHandler.getCoordinates()[0] >= 0 && clientHandler.getCoordinates()[0] <= 800 - 40 && clientHandler.getCoordinates()[1] >= 0 && clientHandler.getCoordinates()[1] <= 600 - 60)
                || ((clientHandler.getCoordinates()[0] <= 0 && data.getCoordinates()[0] >= 0 || clientHandler.getCoordinates()[0] >= 800 - 40 && data.getCoordinates()[0] <= 0)
                && ((clientHandler.getCoordinates()[1] <= 0 && data.getCoordinates()[1] >= 0) || (clientHandler.getCoordinates()[1] >= 600 - 60 && data.getCoordinates()[1] <= 0)
                || (clientHandler.getCoordinates()[1] > 0 && clientHandler.getCoordinates()[1] < 600 - 60)))
				|| ((clientHandler.getCoordinates()[1] <= 0 && data.getCoordinates()[1] >= 0 || clientHandler.getCoordinates()[1] >= 600 - 60 && data.getCoordinates()[1] <= 0) &&
				((clientHandler.getCoordinates()[0] <= 0 && data.getCoordinates()[0] >= 0) || (clientHandler.getCoordinates()[0] >= 800 - 40 && data.getCoordinates()[0] <= 0) ||
				(clientHandler.getCoordinates()[0] > 0 && clientHandler.getCoordinates()[0] < 800 - 40)));
    }

    /**
     * Checks if game is still running
     *
     * @return true if game is running
     */
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean addListener(GeneralListener<Object, Object> listener, String... propertyNames) {
        return subject.addListener(listener);
    }

    @Override
    public boolean removeListener(GeneralListener<Object, Object> listener, String... propertyNames) {
        return subject.removeListener(listener);
    }

	/**
	 * Gathers personal statistics of all players and send them to each of them.
	 */

	private void sendPersonalStatistics() {
        for (ClientHandler clientHandler : userList) {
            int id = clientHandler.getId();
            String characterPlayed = clientHandler.getCharacterChoice().getCharacterName();
            int amountOfBulletsShot = (int) clientHandler.getAndResetBullets();
            int givenDamage = 0;
            int receivedDamage = 0;
            int distance = (int) clientHandler.getAndResetDistance();

            boolean isWinner = clientHandler.isWinner();

            PersonalStatisticsPackage personalStatisticsPackage = new PersonalStatisticsPackage(id, characterPlayed, amountOfBulletsShot, givenDamage, receivedDamage, distance);
            subject.firePropertyChange("UpdatePersonalStatistics", null, personalStatisticsPackage);
			try {
				clientHandler.getClient().sendGameStatistics(id, isWinner, amountOfBulletsShot, distance);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
        }
    }

	/**
	 * Using observer pattern sends information to GameRoomManager about all overall statistics from the game(amount of bullets fired during the game, distance made by the players, winner of the game).
	 */

	private void sendGameStatistics() {
		int winner = -1;
        int totalBulletsFired = 0;
        int totalGivenDamage = 0;
        int totalDistance = 0;
        int gameLength = 0;

        for (ClientHandler clientHandler : userList) {
            totalBulletsFired += (int) clientHandler.getBulletsShotInTheGame();
            totalDistance += (int) clientHandler.getDistance();
            if (clientHandler.isWinner()) {
                winner = clientHandler.getId();
            }
            clientHandler.setWinner(false);
        }
        GameStatisticsPackage gameStatisticsPackage = new GameStatisticsPackage(winner, totalBulletsFired, totalGivenDamage, totalDistance, gameLength);
        subject.firePropertyChange("UpdateGameStatistics", null, gameStatisticsPackage);
    }

	/**
	 * Called when all players want to play again. Sends all necessary information about players again (player ID, username, characterChoice and starting location).
	 */
	private void playAgain() {
    	for(int i = 0; i < initialPositions.length; i++) {
    		for(ClientHandler clientHandler : userList) {
    			if(initialPositions[i][2] == clientHandler.getId()) {
    				clientHandler.setCoordinates(new double[]{initialPositions[i][0], initialPositions[i][1]});
				}
			}
		}
		for(ClientHandler clientHandler : userList) {
			clientHandler.defaultPlayAgain();
			for(ClientHandler clientHandler1 : userList) {
				try {
					clientHandler.getClient().prepareGame(clientHandler1.getId(), clientHandler1.getUsername(), clientHandler1.getCharacterChoice(), new int[]{(int)clientHandler1.getCoordinates()[0], (int)clientHandler1.getCoordinates()[1]});
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
