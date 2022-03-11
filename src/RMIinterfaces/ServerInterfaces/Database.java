package RMIinterfaces.ServerInterfaces;

import RMIinterfaces.RmiClient;

import java.rmi.RemoteException;
import java.util.ArrayList;

public interface Database {
    boolean validateLogin(String username, String password, RmiClient client) throws RemoteException;

    void register(String username, String password, String email, String avatarUrl) throws RemoteException;

    ArrayList<Object[]> getPersonalStatistics(int id) throws RemoteException;

    ArrayList<Object[]> getGameStatistics(int id) throws RemoteException;

    ArrayList<String> getOverallStatistics(int id) throws RemoteException;

    boolean updateUsername(int id, String updatedUsername) throws RemoteException;

    boolean updatePassword(int id, String updatedPassword) throws RemoteException;

    boolean updateEmail(int id, String updatedEmail) throws RemoteException;

    boolean updateAvatar(int id, String updatedAvatarUrl) throws RemoteException;
}
