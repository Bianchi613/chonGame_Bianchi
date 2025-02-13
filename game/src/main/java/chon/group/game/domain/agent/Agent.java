package chon.group.game.domain.agent;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

/**
 * Represents an agent in the game, with properties such as position, size,
 * speed, and image.
 * The agent can move in specific directions and chase a target.
 */
public class Agent {

    /** X position (horizontal) of the agent. */
    private int posX;

    /** Y (vertical) position of the agent. */
    private int posY;

    /** Height of the agent. */
    private int height;

    /** Width of the agent. */
    private int width;

    /** Agent speed. */
    private int speed;

    /** Image representing the agent. */
    private Image image;

    /** Indicates if the agent is facing left. */
    private boolean flipped = false;

    /** The initial agent's health. */
    private int health;

    /** The maximum agent's health. */
    private int fullHealth;

    /* The time of the last hit taken. */
    private long lastHitTime = 0;

    /* Flag to control the invulnerability status of the agent. */
    private boolean invulnerable;

    /* Invulnerability (in milliseconds) */
    private final long INVULNERABILITY_COOLDOWN = 500;

    // Jumping parameters
    private boolean isJumping = false;
    private int jumpHeight = 140;
    private int gravity = 7;

    // Movement boundaries
    private int minX = 0;
    private int maxX = 1300; // Assuming the right side boundary
    private int minY = 375;
    private int maxY = 1000; // Assuming the bottom side boundary

    /**
     * Constructor to initialize the agent properties including its direction.
     *
     * @param posX      the agent's initial X (horizontal) position
     * @param posY      the agent's initial Y (vertical) position
     * @param height    the agent's height
     * @param width     the agent's width
     * @param speed     the agent's speed
     * @param health    the agent's health
     * @param pathImage the path to the agent's image
     * @param flipped   the agent's direction (RIGHT=0 or LEFT=1)
     */
    public Agent(int posX, int posY, int height, int width, int speed, int health, String pathImage, boolean flipped) {
        this.posX = Math.max(minX, Math.min(posX, maxX - width));  // Limite horizontal
        this.posY = Math.max(minY, Math.min(posY, maxY - height));  // Limite vertical
        this.height = height;
        this.width = width;
        this.speed = speed;
        this.health = health;
        this.fullHealth = health;
        this.image = new Image(getClass().getResource(pathImage).toExternalForm());
        this.flipped = flipped;
    }

    /**
     * Constructor to initialize the agent properties without direction.
     *
     * @param posX      the agent's initial X (horizontal) position
     * @param posY      the agent's initial Y (vertical) position
     * @param height    the agent's height
     * @param width     the agent's width
     * @param speed     the agent's speed
     * @param health    the agent's health
     * @param pathImage the path to the agent's image
     */
    public Agent(int posX, int posY, int height, int width, int speed, int health, String pathImage) {
        this(posX, posY, height, width, speed, health, pathImage, false);
    }

    // Getter and setter methods

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = Math.max(minX, Math.min(posX, maxX - width));  // Limite horizontal
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        if (isJumping) {
            this.posY = posY;  // Permite pulo acima de minY
        } else {
            this.posY = Math.max(minY, Math.min(posY, maxY - height));  // Limite vertical
        }
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getFullHealth() {
        return fullHealth;
    }

    public void setFullHealth(int fullHealth) {
        this.fullHealth = fullHealth;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public boolean isFlipped() {
        return flipped;
    }

    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
    }

    public long getLastHitTime() {
        return lastHitTime;
    }

    public void setLastHitTime(long lastHitTime) {
        this.lastHitTime = lastHitTime;
    }

    public long getInvulnerabilityCooldown() {
        return INVULNERABILITY_COOLDOWN;
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    // Flip image horizontally for direction
    private void flipImage() {
        ImageView flippedImage = new ImageView(image);
        flippedImage.setScaleX(-1);
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        this.flipped = !this.flipped;
        this.image = flippedImage.snapshot(params, null);
    }

    // Moves the agent based on input directions (RIGHT, LEFT, UP, DOWN)
    public void move(List<String> movements) {
        if (movements.contains("RIGHT")) {
            if (flipped)
                this.flipImage();
            setPosX(posX += speed);
        } else if (movements.contains("LEFT")) {
            if (!flipped)
                this.flipImage();
            setPosX(posX -= speed);
        } else if (movements.contains("UP")) {
            setPosY(posY -= speed);
        } else if (movements.contains("DOWN")) {
            setPosY(posY += speed);
        }
    }

    // Makes the agent chase a target (targetX, targetY)
    public void chase(int targetX, int targetY) {
        if (targetX > this.posX) {
            this.move(new ArrayList<>(List.of("RIGHT")));
        } else if (targetX < this.posX) {
            this.move(new ArrayList<>(List.of("LEFT")));
        }
        if (targetY > this.posY) {
            this.move(new ArrayList<>(List.of("DOWN")));
        } else if (targetY < this.posY) {
            this.move(new ArrayList<>(List.of("UP")));
        }
    }

    // Take damage and handle invulnerability
    public void takeDamage(int damage) {
        this.invulnerable = this.updateInvulnerability();
        if (!this.invulnerable && this.health > 0) {
            this.health = health - damage;
            if (this.health < 0) this.health = 0;
            else this.lastHitTime = System.currentTimeMillis();
        }
    }

    // Update invulnerability status
    private boolean updateInvulnerability() {
        if (System.currentTimeMillis() - lastHitTime >= INVULNERABILITY_COOLDOWN) {
            return false;
        }
        return true;
    }public void jump() {
        if (!isJumping) {  // Check if the agent is already jumping
            isJumping = true;
            new Thread(() -> {
                // Jump up (ascending)
                for (int i = 0; i < jumpHeight / gravity; i++) {
                    posY -= gravity;  // Move upwards
                    try { Thread.sleep(20); } catch (InterruptedException e) {}
                }
                
                // Fall down (gravity effect)
                for (int i = 0; i < jumpHeight / gravity; i++) {
                    posY += gravity;  // Move downwards
                    try { Thread.sleep(20); } catch (InterruptedException e) {}
                }
                isJumping = false;  // Reset jumping state
            }).start();
        }
    }
}    