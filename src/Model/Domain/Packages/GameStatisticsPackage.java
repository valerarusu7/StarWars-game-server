package Model.Domain.Packages;

public class GameStatisticsPackage {
    private int winner;
    private int totalBulletsFired;
    private int totalGivenDamage;
    private int totalDistance;
    private int gameLength;

    public GameStatisticsPackage(int winner, int totalBulletsFired, int totalGivenDamage, int totalDistance, int gameLength) {
        this.winner = winner;
        this.totalBulletsFired = totalBulletsFired;
        this.totalGivenDamage = totalGivenDamage;
        this.totalDistance = totalDistance;
        this.gameLength = gameLength;
    }

    public int getWinner() {
        return winner;
    }

    public int getTotalBulletsFired() {
        return totalBulletsFired;
    }

    public int getTotalGivenDamage() {
        return totalGivenDamage;
    }

    public int getTotalDistance() {
        return totalDistance;
    }

    public int getGameLength() {
        return gameLength;
    }

    @Override
    public String toString() {
        String s = "Game overall Statistics --------------->";
        s += "\nWinner: " + getWinner();
        s += "\nTotal Bullets Fired: " + getTotalBulletsFired();
        s += "\nTotal Given Damage: " + getTotalGivenDamage();
        s += "\nTotal Distance: " + getTotalDistance();
        s += "\nGame Length: " + getGameLength();
        s += "\n";
        return s;
    }
}
