package Model.mediator.Gameplay;

import Model.Domain.BulletServer;
import Model.Domain.ClientHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BulletsSimulation implements Runnable {

    private MovementReceiver movementReceiver;
    private List<BulletServer> bulletList;
    private List<ClientHandler> playerList;
    private double[][] coordinates;
    private double hit = -1;

    /**
     * Three argument constructor which assigns MovementReceiver responsible for the game, list of bullets from the game and creates a list from set of players. Additionally initializes two dimensional array.
     *
     * @param movementReceiver
     * @param playerList
     * @param bulletList
     */

    public BulletsSimulation(MovementReceiver movementReceiver, Set<ClientHandler> playerList, List<BulletServer> bulletList) {
        this.movementReceiver = movementReceiver;
        this.bulletList = bulletList;
        this.playerList = new ArrayList<>(playerList);
        coordinates = new double[playerList.size()][3];
    }

    /**
     * While game is running assigns all the coordinates and players ids to coordinates array. Afterwards it loops through all the bullets in order to update their position and check for collision.
     * If collision occurred it assigns player ID to hit variable. If bullet leaves the map it is deleted from the list. If hit variable has different value than default -1 MovementReceiver is informed about
     * collision of the player with the bullet. Loop runs every 30 ms in order to simulate speed of the gameplay from the client.
     *
     * @see MovementReceiver#isRunning()
     * @see BulletServer#update(double[][])
     * @see MovementReceiver#playerHit(int, int)
     */
    @Override
    public void run() {
        while (movementReceiver.isRunning()) {
            for (int i = 0; i < coordinates.length; i++) {
                //Data needed in order to implement collision
                coordinates[i][0] = playerList.get(i).getCoordinates()[0];
                coordinates[i][1] = playerList.get(i).getCoordinates()[1];
                coordinates[i][2] = playerList.get(i).getId();
            }
            for (int i = 0; i < bulletList.size(); i++) {
                hit = bulletList.get(i).update(coordinates);
                //This if statement will be reached after bullet will leave the map.
                if (bulletList.get(i).getCoordinates()[0] < 0 - 20 || bulletList.get(i).getCoordinates()[0] > 800 + 20 || bulletList.get(i).getCoordinates()[1] < 0 - 20 || bulletList.get(i).getCoordinates()[1] > 600 + 20) {
                    //Remove bullet from the simulation
                    bulletList.remove(bulletList.get(i));
                }
                //This if statement will bea reached if collision appeared in update method inside BulletServer
                if (hit != -1) {
                    //Inform MovementReceiver about hit
                    movementReceiver.playerHit((int) hit, bulletList.get(i).getBelongsToPlayerID());
                }
            }
            try {
                //It is the same amount of time as in GameView. This way we simulate movement of the bullets by updating them at the same time on both client and server
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        bulletList.clear();
    }
}
