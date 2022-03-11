package Model.mediator.GameManagement;

import Model.mediator.Gameplay.MovementReceiver;
import Model.Domain.ClientHandler;
import utility.observer.event.ObserverEvent;
import utility.observer.listener.GeneralListener;
import utility.observer.listener.LocalListener;
import utility.observer.subject.LocalSubject;
import utility.observer.subject.PropertyChangeProxy;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameRoomManager implements LocalListener<Object, Object>, LocalSubject<Object, Object> {

	public static final ExecutorService executorService  = Executors.newFixedThreadPool(30);
	private ArrayList<ClientHandler> roomForTwoPlayers;
	private ArrayList<ClientHandler> roomForThreePlayers;
	private static  final int[][] START_LOCATIONS = {{100, 100}, {700, 100}, {100, 500}, {700, 500}};
	private PropertyChangeProxy<Object, Object> subject;

	/**
	 * No argument constructor initializes executorService with 15 available threads. It initializes roomForTwoPlayers and subject.
	 */

	public GameRoomManager() {
		roomForTwoPlayers = new ArrayList<>();
		roomForThreePlayers = new ArrayList<>();
		this.subject = new PropertyChangeProxy<>(this, true);
	}

	/**
	 * Informs player about his id, username, character choice and starting location. Starts the game.
	 * @param player
	 * 		Player who wants to play.
	 */

	public void startGameForOnePlayer(ClientHandler player) {
		try {
			player.getClient().prepareGame(player.getId(), player.getUsername(), player.getCharacterChoice(), START_LOCATIONS[0]);
			player.setCoordinates(new double[]{START_LOCATIONS[0][0], START_LOCATIONS[0][1]});
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		MovementReceiver receiver = new MovementReceiver();
		receiver.addPlayerToGame(player);
		receiver.addListener(this);
		executorService.execute(receiver);
	}

	/**
	 * Adds player to the waiting room. If there is another player who wants to play informs players about their id, username,
	 * character choices and starting locations. Starts the game.
	 * @param player
	 * 		Player who wants to play
	 */

	public void startGameForTwoPlayers(ClientHandler player) {
		startGameForXplayers(2, roomForTwoPlayers, player);
	}

	public void startGameForThreePlayer(ClientHandler player) {
		startGameForXplayers(3, roomForThreePlayers, player);
	}

	private void startGameForXplayers(int x, ArrayList<ClientHandler> waitingList, ClientHandler player) {
		waitingList.add(player);
		if(waitingList.size() > x - 1) {
			ClientHandler[] playersList = new ClientHandler[x];
			for (int i = 0; i < playersList.length; i++) {
				playersList[i] = waitingList.get(i);
				waitingList.get(i).setCoordinates(new double[]{START_LOCATIONS[i][0], START_LOCATIONS[i][1]});
			}
			for (int i = 0; i < playersList.length; i++) {
				for (int y = 0; y < playersList.length; y++) {
					try {
						playersList[i].getClient().prepareGame(playersList[y].getId(), playersList[y].getUsername(), playersList[y].getCharacterChoice(), START_LOCATIONS[y]);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
			for (int i = 0; i < playersList.length; i++) {
				waitingList.remove(playersList[playersList.length - 1 - i]);
			}
			MovementReceiver receiver = new MovementReceiver();
			for (int i = 0; i < playersList.length; i++) {
				receiver.addPlayerToGame(playersList[i]);
			}
			receiver.addListener(this);
			executorService.execute(receiver);
		}
	}

	/**
	 * Removes player from the waiting list.
	 *
	 * @param id
	 * 		Player's ID
	 */

	public void removePlayerByID(int id) {
		for(ClientHandler clientHandler : roomForTwoPlayers) {
			if(clientHandler.getId() == id) {
				roomForTwoPlayers.remove(clientHandler);
			}
		}
	}

	@Override
	public void propertyChange(ObserverEvent<Object, Object> event) {
		switch (event.getPropertyName()){
			case "UpdateGameStatistics":
				subject.firePropertyChange("UpdateGameStatistics", null, event.getValue2());
				break;
			case "UpdatePersonalStatistics":
				subject.firePropertyChange("UpdatePersonalStatistics", null, event.getValue2());
				break;
		}
	}

	@Override
	public boolean addListener(GeneralListener<Object, Object> listener, String... propertyNames) {
		return subject.addListener(listener);
	}

	@Override
	public boolean removeListener(GeneralListener<Object, Object> listener, String... propertyNames) {
		return subject.removeListener(listener);
	}

}
