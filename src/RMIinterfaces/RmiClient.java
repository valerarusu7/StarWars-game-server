package RMIinterfaces;


import Model.Domain.Character;
import Model.Domain.User;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RmiClient extends Remote {
    void loginValidated(User user) throws RemoteException;

    void prepareGame(int id, String username, Character characterChoice, int[] location) throws RemoteException;

    void playerDied(int playerWhoIsHitID, int playerWhoShotID) throws RemoteException;

    void sendGameStatistics(int id, boolean winner, int bulletsShotInTheGame, int distance) throws RemoteException;

    void disconnectFromTheGame() throws RemoteException;

    void userConnected(String username) throws RemoteException;

    void userDisconnected(String username) throws RemoteException;

    void gameAborted() throws RemoteException;

    void newMessageOnChat(String message) throws RemoteException;
}
