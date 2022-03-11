package Model.Domain.Packages;

public class PersonalStatisticsPackage implements Package{
    private int playerId;
    private String characterPlayed;
    private int bulletsFired;
    private int givenDamage;
    private int receivedDamage;
    private int distance;

    public PersonalStatisticsPackage(int playerId, String characterPlayed, int bulletsFired, int givenDamage, int receivedDamage, int distance) {
        this.playerId = playerId;
        this.characterPlayed = characterPlayed;
        this.bulletsFired = bulletsFired;
        this.givenDamage = givenDamage;
        this.receivedDamage = receivedDamage;
        this.distance = distance;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getCharacterPlayed() {
        return characterPlayed;
    }

    public int getBulletsFired() {
        return bulletsFired;
    }

    public int getGivenDamage() {
        return givenDamage;
    }

    public int getReceivedDamage() {
        return receivedDamage;
    }

    public int getDistance() {
        return distance;
    }

    @Override
    public String toString() {
        String s = "Personal Statistics --------------->";
        s += "\nPlayer ID: " + getPlayerId();
        s += "\nCharacter Played: " + getCharacterPlayed();
        s += "\nBullets Fired: " + getBulletsFired();
        s += "\nGiven Damage: " + getGivenDamage();
        s += "\nReceived Damage: " + getReceivedDamage();
        s += "\nDistance: " + getDistance();
        s += "\n";
        return s;
    }
}
