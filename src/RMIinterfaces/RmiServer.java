package RMIinterfaces;

import RMIinterfaces.ServerInterfaces.Database;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface RmiServer extends Remote, Database {
	void pickCharacter(int id, String characterPicked) throws RemoteException;
	void pickGameRoomSize(int id, int size) throws RemoteException;
	void startServerSocket(int id) throws RemoteException;
	void playAgain(int id, boolean wantsToPlayAgain) throws RemoteException;
	void disconnect(int id) throws RemoteException;
	ArrayList<String> getOnlineUsers() throws RemoteException;
	void newMessageOnChat(String message) throws RemoteException;
}
