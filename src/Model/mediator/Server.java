package Model.mediator;

import RMIinterfaces.RmiClient;
import RMIinterfaces.RmiServer;
import Model.mediator.GameManagement.GameRoomManager;
import Model.Domain.ClientHandler;
import Model.Domain.Packages.GameStatisticsPackage;
import Model.Domain.Packages.PersonalStatisticsPackage;
import Model.Domain.User;
import utility.observer.event.ObserverEvent;
import utility.observer.listener.LocalListener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Server implements RmiServer, LocalListener<Object, Object> {

    public static int userIdCounter;
    public Set<ClientHandler> userList;
    private GameRoomManager gameRoomManager;
    private DataPersistence database;
    private ServerSocket serverSocket;
    private int currentGameId = 0;

    public Server(int port) {
        try {
            Registry reg = LocateRegistry.createRegistry(1099);
            UnicastRemoteObject.exportObject(this, 0);
            Naming.rebind("StarWarsGame", this);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        this.userList = new HashSet<>();
        gameRoomManager = new GameRoomManager();
        gameRoomManager.addListener(this);
        userIdCounter = 1;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        database = new GameDatabase();
    }


    /**
     * Creates account in the database with given arguments.
     *
     * @param username String passed from RemoteClient.
     * @param password String passed from RemoteClient.
     * @param email    String passed from RemoteClient.
     * @throws IllegalArgumentException When username or email already exists.
     * @see Model.mediator.GameDatabase#register(String, String, String, String)
     */
    @Override
    public void register(String username, String password, String email, String avatarUrl) {
        database.register(username, password, email, avatarUrl);
    }


    /**
     * Validates login with given arguments in database. Returns boolean based on result of validateLogin(username, password) method
     * in GameDatabase class. If login is validated, new ClientHandler is created for Client, ID is assigned from database and he is added to userList in the server.
     * loginValidated method is called on the clients side to set CurrentPlayer with verified username and ID.
     *
     * @param username String passed from RemoteClient.
     * @param password String passed from RemoteClient.
     * @param client   String passed from RemoteClient.
     * @return true if given username and password is valid. False if given username and password is invalid.
     * @see Model.mediator.GameDatabase#validateLogin(String, String)
     */
    @Override
    public boolean validateLogin(String username, String password, RmiClient client) {
        boolean userWithThisUsernameAlreadyConnected = false;
        for (ClientHandler clientHandler : userList) {
            if (clientHandler.getUsername().equals(username)) {
                userWithThisUsernameAlreadyConnected = true;
            }
        }
        if (!userWithThisUsernameAlreadyConnected) {
            boolean loggedIn = database.validateLogin(username, password);
            if (loggedIn) {
                User user = database.getUser(username);
                ClientHandler userClientHandler = new ClientHandler(client, user.getID(), user.getUsername());
                userList.add(userClientHandler);

                try {
                    client.loginValidated(user);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                for (ClientHandler clientHandler : userList) {
                    try {
                        clientHandler.getClient().userConnected(username);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public ArrayList<Object[]> getPersonalStatistics(int id) {
        return database.getPersonalStatistics(id);
    }

    @Override
    public ArrayList<Object[]> getGameStatistics(int id) {
        return database.getGameStatistics(id);
    }

    @Override
    public ArrayList<String> getOverallStatistics(int id) {
        return database.getOverallStatistics(id);
    }

    @Override
    public boolean updateUsername(int id, String updatedUsername) {
        return database.updateUsername(id, updatedUsername);
    }

    @Override
    public boolean updatePassword(int id, String updatedPassword) {
        return database.updatePassword(id, updatedPassword);
    }

    @Override
    public boolean updateEmail(int id, String updatedEmail) {
        return database.updateEmail(id, updatedEmail);
    }

    @Override
    public boolean updateAvatar(int id, String updatedAvatarUrl) {
        return database.updateAvatar(id, updatedAvatarUrl);
    }

    /**
     * Executes setCharacterChoice in ClientHandler for everyone in userList with given ID and picked character name.
     *
     * @param id              ID of player.
     * @param characterPicked Character name for given ID of player.
     */

    @Override
    public void pickCharacter(int id, String characterPicked) {
        for (ClientHandler client : userList) {
            if (client.getId() == id)
                client.setCharacterChoice(database.getCharacter(characterPicked));
        }
    }

    /**
     * Redirects player to GameRoomManager based on desire game room size.
     *
     * @param id   ID of the player who wants to play
     * @param size Desired game room size
     * @see Model.mediator.GameManagement.GameRoomManager#startGameForOnePlayer(ClientHandler)
     * @see Model.mediator.GameManagement.GameRoomManager#startGameForTwoPlayers(ClientHandler)
     */

	@Override
    public void pickGameRoomSize(int id, int size) {
        for (ClientHandler client : userList) {
            if (client.getId() == id) {
                switch (size) {
                    case 1:
                        gameRoomManager.startGameForOnePlayer(client);
                        break;
                    case 2:
                        gameRoomManager.startGameForTwoPlayers(client);
                        break;
					case 3:
						gameRoomManager.startGameForThreePlayer(client);
						break;
                }
            }
        }
    }

    /**
     * Establishes TCP socket connection with the player
     *
     * @param id ID of the player who wants to establish socket connection
     * @see ClientHandler#getId()
     * @see ClientHandler#setSocket(Socket)
     */

	@Override
    public void startServerSocket(int id) {
        try {
            for (ClientHandler client : userList) {
                if (client.getId() == id) {
                    Socket clientSocket = serverSocket.accept();
                    client.setSocket(clientSocket);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Player disconnects from the game and is removed from active users list.
     *
     * @param id ID of the player who wants to be disconnected
     * @see ClientHandler#getId()
     * @see ClientHandler#getClient()
     */
    @Override
    public void disconnect(int id) {
        String disconnectedClientUsername = "";
        for (ClientHandler clientHandler : userList) {
                if (clientHandler.getId() == id) {
                	clientHandler.playAgain(false);
                    userList.remove(clientHandler);
                    disconnectedClientUsername = clientHandler.getUsername();
                    break;
                }
        }

        playerDisconnected(disconnectedClientUsername);
        gameRoomManager.removePlayerByID(id);
    }

    @Override
    public ArrayList<String> getOnlineUsers() {
        ArrayList<String> onlineUsers = new ArrayList<>();
        for (ClientHandler client : userList){
            onlineUsers.add(client.getUsername());
        }
        return onlineUsers;
    }

	@Override
	public void playAgain(int id, boolean wantsToPlayAgain) {
		for (ClientHandler clientHandler : userList) {
			if(clientHandler.getId() == id) {
				clientHandler.playAgain(wantsToPlayAgain);
			}
		}
	}

	/**
	 * Called when one of the users send a message to the chat. Informs all users about the message.
	 *
	 * @param message
	 * 		New message on the chat
	 */

	@Override
	public void newMessageOnChat(String message) {
		for(ClientHandler clientHandler : userList) {
			try {
				clientHandler.getClient().newMessageOnChat(message);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Informs all users about disconnected player.
	 *
	 * @param username
	 * 		Username of the player who disconnected.
	 */

	private void playerDisconnected(String username){
        for (ClientHandler client: userList){
            try {
                client.getClient().userDisconnected(username);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void propertyChange(ObserverEvent<Object, Object> event) {
        // First update game statistics. Method returns ID of added game. I set an instance variable currentGameId for that number. Therefore if there are multiple games
        // at the same time, there might occur a problem, but don't know how to handle it now.
        switch (event.getPropertyName()){
            case "UpdateGameStatistics":
                GameStatisticsPackage gameStatisticsPackage = (GameStatisticsPackage) event.getValue2();
                currentGameId = database.updateGameStatistics(gameStatisticsPackage.getWinner(), gameStatisticsPackage.getTotalBulletsFired(), gameStatisticsPackage.getTotalGivenDamage(), gameStatisticsPackage.getTotalDistance(), gameStatisticsPackage.getGameLength());
                break;
            case "UpdatePersonalStatistics":
                PersonalStatisticsPackage personalStatisticsPackage = (PersonalStatisticsPackage) event.getValue2();
                database.updatePersonalStatistics(currentGameId, personalStatisticsPackage.getPlayerId(), personalStatisticsPackage.getCharacterPlayed(), personalStatisticsPackage.getBulletsFired(), personalStatisticsPackage.getGivenDamage(), personalStatisticsPackage.getReceivedDamage(), personalStatisticsPackage.getDistance());
                break;
        }
    }
}