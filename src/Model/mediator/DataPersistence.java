package Model.mediator;

import Model.Domain.Character;
import Model.Domain.User;

import java.util.ArrayList;

public interface DataPersistence {
    boolean validateLogin(String username, String password);

    void register(String username, String password, String email, String avatarUrl);

    User getUser(String username);

    ArrayList<Object[]> getPersonalStatistics(int id);

    ArrayList<Object[]> getGameStatistics(int id);

    ArrayList<String> getOverallStatistics(int id);

    Character getCharacter(String characterName);

    boolean updateUsername(int id, String updatedUsername);

    boolean updatePassword(int id, String updatedPassword);

    boolean updateEmail(int id, String updatedEmail);

    boolean updateAvatar(int id, String updatedAvatarUrl);

    int updateGameStatistics(int winner, int totalBulletsFired, int totalGivenDamage, int totalDistance, int gameLength);

    void updatePersonalStatistics(int gameId, int userId, String characterPlayed, int bulletsFired, int givenDamage, int receivedDamage, int distance);
}
