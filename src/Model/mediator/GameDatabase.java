package Model.mediator;

import Model.Domain.Character;
import Model.Domain.User;

import java.sql.SQLException;
import java.util.ArrayList;

public class GameDatabase implements DataPersistence {
    private Database database;

    public GameDatabase() {
        try {
            database = new Database("org.postgresql.Driver", "jdbc:postgresql://localhost:5432/semesterproject", "postgres", "123456");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method first checks if username and email are valid calling private methods.
     * If they are valid, calls another private method to create account with given arguments.
     *
     * @param username String passed from Model.mediator.Server.
     * @param password String passed from Model.mediator.Server.
     * @param email    String passed from Model.mediator.Server.
     * @param avatarUrl
     * @throws IllegalArgumentException if username or email is not valid.
     * @see #usernameAlreadyExists(String)
     * @see #emailAlreadyExists(String)
     * @see #createAccount(String, String, String, String)
     */
    @Override
    public void register(String username, String password, String email, String avatarUrl) {
        if(username == null){
            throw new IllegalArgumentException("Empty username");
        }

        if (email == null){
            throw new IllegalArgumentException("Empty email");
        }

        if (username.length() < 5){
            throw new IllegalArgumentException("Username has to be at least 5 characters long");
        }

        if (usernameAlreadyExists(username)) {
            throw new IllegalArgumentException("Username already exists");
        }


        if (!(email.contains("@") && email.contains("."))){
            throw new IllegalArgumentException("Invalid email");
        }

        if (emailAlreadyExists(email)) {
            throw new IllegalArgumentException(("Email already exists"));
        }

        createAccount(username, password, email, avatarUrl);
    }

    /**
     * Validates login with given parameters using private method
     *
     * @param username String passed from Model.mediator.Server.
     * @param password String passed from Model.mediator.Server.
     * @return true if given pass
     * word for user matches returned password from database.
     * @see #getPasswordForUser(String)
     */
    @Override
    public boolean validateLogin(String username, String password) {
        if (username == null || password == null){
            throw new IllegalArgumentException("Username or password empty");
        }
        String returnedPassword = getPasswordForUser(username);
        return returnedPassword.equals(password);
    }

    /**
     * Returns ID of given username from database.
     *
     * @param username String passed from Model.mediator.Server.
     * @return ID of player with giver username
     */
    @Override
    public User getUser(String username) {
        String sql = "SELECT user_id, username, avatar_image_url FROM sep2_game.game_user WHERE username = ?;";

        ArrayList<Object[]> returnedObjects = new ArrayList<>();

        try {
            returnedObjects = database.query(sql, username);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (returnedObjects == null){
            throw new IllegalArgumentException("No user found.");
        }

        Object[] row = returnedObjects.get(0);

        return new User((int) row[0], row[1].toString(), row[2].toString());
    }

    /**
     * @param id
     * @return
     */
    @Override
    public ArrayList<Object[]> getPersonalStatistics(int id) {
        String sql = "SELECT game_id, character, bullets_fired, given_damage, received_damage, distance_in_px FROM sep2_game.personal_statistics WHERE user_id = ?;";

        ArrayList<Object[]> returnedObjects = new ArrayList<>();

        try {
            returnedObjects = database.query(sql, id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (returnedObjects == null){
            throw new IllegalArgumentException("No personal data.");
        }

        return returnedObjects;
    }

    @Override
    public ArrayList<Object[]> getGameStatistics(int id) {
        String sql = "SELECT sep2_game.game_statistics.game_id, winner, total_bullets_fired, total_given_damage, total_distance, game_length FROM sep2_game.personal_statistics LEFT JOIN sep2_game.game_statistics ON sep2_game.personal_statistics.game_id = sep2_game.game_statistics.game_id WHERE user_id = ?;";

        ArrayList<Object[]> returnedObjects = new ArrayList<>();

        try {
            returnedObjects = database.query(sql, id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (returnedObjects == null){
            throw new IllegalArgumentException("No game data.");
        }

        return returnedObjects;
    }

    @Override
    public ArrayList<String> getOverallStatistics(int id) {
        String sql = "SELECT SUM(bullets_fired), SUM(given_damage), SUM(received_damage), SUM(distance_in_px), COUNT(game_id) FROM sep2_game.personal_statistics WHERE user_id = ?;";

        ArrayList<Object[]> returnedObjects = new ArrayList<>();

        try {
            returnedObjects = database.query(sql, id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (returnedObjects.get(0)[0] == null){
            throw new IllegalArgumentException("No overall data.");
        }

        ArrayList<String> overallStatistics = new ArrayList<>();

        Object[] row = returnedObjects.get(0);

        overallStatistics.add(row[0].toString()); // Total bullets fired for player with given ID
        overallStatistics.add(row[1].toString()); // Total given damage for player with given ID
        overallStatistics.add(row[2].toString()); // Total received damage for player with given ID
        overallStatistics.add(row[3].toString()); // Total distance for player with given ID
        overallStatistics.add(row[4].toString()); // Total games played for player with given ID

        sql = "SELECT COUNT(winner) FROM sep2_game.game_statistics WHERE winner = ?;";

        try {
            returnedObjects = database.query(sql, id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        row = returnedObjects.get(0);
        overallStatistics.add(row[0].toString()); // Total won games for player with given ID

        return overallStatistics;
    }

    @Override
    public Character getCharacter(String characterName) {
        String sql = "SELECT * FROM sep2_game.characters WHERE character_name = ?;";

        ArrayList<Object[]> returnedObjects = new ArrayList<>();

        try {
            returnedObjects = database.query(sql, characterName);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Object[] row = returnedObjects.get(0);
        String returnedCharacterName = row[0].toString();
        String characterImageUrl = row[1].toString();
        int damage = (int) row[2];
        int health = (int) row[3];
        int velocity = (int) row[4];
        double speed = (double) ((int) row[5]);
        int armor = (int) row[6];
        int penetration = (int) row[7];
        int bulletSize = (int) row[8];
        String bulletColor = row[9].toString();

        Character character = new Character(returnedCharacterName, characterImageUrl, damage, health, velocity,
                speed, armor, penetration, bulletSize, bulletColor);

        return character;
    }

    @Override
    public boolean updateUsername(int id, String updatedUsername) {
        if (updatedUsername.length() < 5){
            throw new IllegalArgumentException("Username has to be at least 5 characters long");
        }

        if (usernameAlreadyExists(updatedUsername)) {
            throw new IllegalArgumentException("Username already exists");
        }

        String sql = "UPDATE sep2_game.game_user SET username = ? WHERE user_id = ?;";

        try {
            database.update(sql, updatedUsername, id);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean updatePassword(int id, String updatedPassword) {
        String sql = "UPDATE sep2_game.game_user SET password = ? WHERE user_id = ?;";

        try {
            database.update(sql, updatedPassword, id);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean updateEmail(int id, String updatedEmail) {
        if (!(updatedEmail.contains("@") && updatedEmail.contains("."))){
            throw new IllegalArgumentException("Invalid email");
        }

        if (emailAlreadyExists(updatedEmail)) {
            throw new IllegalArgumentException(("Email already exists"));
        }

        String sql = "UPDATE sep2_game.game_user SET email = ? WHERE user_id = ?;";

        try {
            database.update(sql, updatedEmail, id);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean updateAvatar(int id, String updatedAvatarUrl) {
        String sql = "UPDATE sep2_game.game_user SET avatar_image_url = ? WHERE user_id = ?;";

        try {
            database.update(sql, updatedAvatarUrl, id);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public int updateGameStatistics(int winner, int totalBulletsFired, int totalGivenDamage, int totalDistance, int gameLength) {
        String sql = "INSERT INTO sep2_game.game_statistics (winner, total_bullets_fired, total_given_damage, total_distance, game_length) VALUES (?, ?, ?, ?, ?);";

        try {
            int gameId = database.updateWithID(sql, winner, totalBulletsFired, totalGivenDamage, totalDistance, gameLength);
            return gameId;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public void updatePersonalStatistics(int gameId, int userId, String characterPlayed, int bulletsFired, int givenDamage, int receivedDamage, int distance) {
        String sql = "INSERT INTO sep2_game.personal_statistics VALUES (?, ?, ?, ?, ?, ?, ?);";

        try {
            database.update(sql, gameId, userId, characterPlayed, bulletsFired, givenDamage, receivedDamage, distance);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Returns password from the database as a string for giver user with username.
     *
     * @param username String passed from Model.mediator.Server.
     * @return password as a string.
     */
    private String getPasswordForUser(String username) {
        String sql = "SELECT password FROM sep2_game.game_user WHERE username = ?;";

        ArrayList<Object[]> returnedObjects = new ArrayList<>();

        try {
            returnedObjects = database.query(sql, username);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String checkedPassword;
        try {
            checkedPassword = returnedObjects.get(0)[0].toString();
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
        return checkedPassword;
    }

    /**
     * Check if username is unique in database.
     *
     * @param username String passed from Model.mediator.Server.
     * @return true if username already exists, false if username is unique.
     */
    private boolean usernameAlreadyExists(String username) {
        String sql = "SELECT username FROM sep2_game.game_user WHERE username = ?;";

        ArrayList<Object[]> returnedObjects = new ArrayList<>();

        try {
            returnedObjects = database.query(sql, username);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (returnedObjects.size() == 0) {
            return false;
        } else {
            boolean exists = !returnedObjects.get(0)[0].toString().equals("");
            return exists;
        }
    }

    /**
     * Check if email is already assigned to the existing account in the database.
     *
     * @param email String passed from Model.mediator.Server.
     * @return true if email is already assigned to the existing account. True if email is not registered.
     */
    private boolean emailAlreadyExists(String email) {
        String sql = "SELECT email FROM sep2_game.game_user WHERE email = ?;";

        ArrayList<Object[]> returnedObjects = new ArrayList<>();

        try {
            returnedObjects = database.query(sql, email);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (returnedObjects.size() == 0) {
            return false;
        } else {
            boolean exists = !returnedObjects.get(0)[0].toString().equals("");
            return exists;
        }
    }

    /**
     * Creates account in the database with given arguments using SQL statement.
     *
     * @param username String passed from register method.
     * @param password String passed from register method.
     * @param email    String passed from register method.
     */
    private void createAccount(String username, String password, String email, String avatarUrl) {
        String sql = "INSERT INTO sep2_game.game_user (username, password, email, avatar_image_url) VALUES (?, ?, ?, ?);";

        try {
            database.update(sql, username, password, email, avatarUrl);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
