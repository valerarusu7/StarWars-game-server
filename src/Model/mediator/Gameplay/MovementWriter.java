package Model.mediator.Gameplay;

import Model.Domain.ClientHandler;

import java.util.Set;

public class MovementWriter implements Runnable {
	private String messageToBeSend;
	private Set<ClientHandler> userList;

	/**
	 * Two arguments constructor responsible for assigning messageToBeSend and userList.
	 * @param messageToBeSend
	 * @param userList
	 */

	public MovementWriter(String messageToBeSend, Set<ClientHandler> userList) {
		this.messageToBeSend = messageToBeSend;
		this.userList = userList;
	}

	/**
	 * Implementaion of run method calls sendMovementToAll method.
	 */
	@Override
	public void run() {
		sendMovementToAll(messageToBeSend, userList);
	}

	/**
	 * Loops through all the players and sends them a message.
	 * @param messageToBeSend
	 * 		Desired message.
	 * @param userList
	 * 		List of players.
	 * @see ClientHandler#write(String)
	 * @see ClientHandler#flush()
	 */

	public void sendMovementToAll(String messageToBeSend, Set<ClientHandler> userList) {
		synchronized (userList) {
			for (ClientHandler clientHandler : userList) {
				clientHandler.write(messageToBeSend);
				clientHandler.flush();

			}
		}
	}
}
