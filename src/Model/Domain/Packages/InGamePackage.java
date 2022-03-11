package Model.Domain.Packages;

public class InGamePackage implements Package{
	private int id;
	private double[] coordinates;
	private int[] velocity;

	public InGamePackage(int id, double[] coordinates, int[] velocity) {
		this.id = id;
		this.coordinates = coordinates;
		this.velocity = velocity;
	}

	public int getId() {
		return id;
	}

	public double[] getCoordinates() {
		return coordinates;
	}

	public int[] getVelocity() {
		return velocity;
	}

}
