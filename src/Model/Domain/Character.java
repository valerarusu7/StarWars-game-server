package Model.Domain;

import java.io.Serializable;

public class Character implements Serializable {
    private boolean running;
    private String characterName;
    private String characterImageUrl;
    private int damage;
    private int health;
    private Bullet bullet;
    private double speed;
    private int armor;
    private int penetration;

    public Character(String characterName, String characterImageUrl, int damage, int health, int velocity, double speed, int armor, int penetration, int bulletSize, String bulletColor) {
        this.running = false;
        this.characterName = characterName;
        this.characterImageUrl = characterImageUrl;
        this.damage = damage;
        this.health = health;
        this.bullet = new Bullet(velocity, bulletSize, bulletColor);
        this.speed = speed;
        this.armor = armor;
        this.penetration = penetration;
    }

    public String getCharacterImageUrl() {
        return characterImageUrl;
    }

    public int getDamage() {
        return damage;
    }

    public int getHealth() {
        return health;
    }

    public int getArmor() {
        return armor;
    }

    public int getPenetration() {
        return penetration;
    }

    public double getSpeed() {
        return speed;
    }

    public String getCharacterName() {
        return characterName;
    }

    public Bullet getBullet() {
        return bullet;
    }

    public void startRunning() {
		if(!running) {
			speed *= 2;
		}
	}

	public void stopRunning() {
    	speed /= 2;
    	running = false;
	}
}
