package Model.Domain;

public class BulletServer {
	private double[] coordinates;
	private int[] velocity;
	private int belongsToPlayerID;

	public BulletServer(int belongsToPlayerID, double[] coordinates, int[] velocity) {
		this.coordinates = coordinates;
		this.velocity = velocity;
		this.belongsToPlayerID = belongsToPlayerID;
	}

	public double[] getCoordinates() {
		return coordinates;
	}

	public double update(double[][] playersCoordinates) {
		//Update coordinates according to the bullet velocity
		coordinates[0] += velocity[0];
		coordinates[1] += velocity[1];
		//Check for collision with any player (40x60 are our standard dimensions for every character), id is neccessery in order to recognize the player
		for(int i = 0; i < playersCoordinates.length; i++) {
			if (coordinates[0] >= playersCoordinates[i][0] && coordinates[0] <= playersCoordinates[i][0] + 40 &&
					coordinates[1] >= playersCoordinates[i][1] && coordinates[1] <= playersCoordinates[i][1] + 60) {
				// IF SOMEBODY WAS HIT RETURN HIS ID
				return playersCoordinates[i][2];
			}
		}
		// IF NO ONE WAS HIT RETURN -1
		return -1;
	}

	public int getBelongsToPlayerID() {
		return belongsToPlayerID;
	}
}
