package fi.helsinki.cs.bombestman.game;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Bomber implements IBomber {

    private Tile tile;
    private Tile startingPosition;
    final int number;
    private int cooldown;
    private int bombs;
    private final Match match;
    private int points;
    PrintWriter writer;
    BufferedReader reader;
    private Socket socket;
    final String commandLocation;
    private Process myProcess;

    public Bomber(int number, int bombs, Match match, String projectFolder) {
        this.number = number;
        this.bombs = bombs;
        this.match = match;
        this.cooldown = 0;
        if (!projectFolder.endsWith("/")) {
            projectFolder += "/";
        }
        this.commandLocation = projectFolder + "run.sh";
        chooseStartingTile(match, number);

    }

    /**
     * Remember to append \n for message - otherwise its hard for bot to detect
     * when input has ended
     *
     * @param message
     * @throws IOException
     */
    @Override
    public void sayToBomberman(String message) throws IOException {
        writer.append(message).append("\n");
        writer.flush(); //possibly not needed as the writer may autoflush after linebreak
    }

    @Override
    public String readFromBomberman() throws IOException {
        return reader.readLine();
    }

    private void chooseStartingTile(Match match, int number) throws RuntimeException {
        // whoops - must set tile
        Tile[][] tiles = match.getTiles();
        int height = tiles.length;
        int width = tiles[0].length;
        if (number == 0) {
            tile = tiles[0][0];
        } else if (number == 1) {
            tile = tiles[height - 1][0];
        } else if (number == 2) {
            tile = tiles[height - 1][width - 1];
        } else if (number == 3) {
            tile = tiles[0][width - 1];
        } else {
            throw new RuntimeException("Cannot have over 4 players");
        }
        tile.addBomber(this);
        setStartingPosition(tile);
    }

    @Override
    public void destroy() {
        myProcess.destroy();
    }

    @Override
    public void setMyProcess(Process p) {
        this.myProcess = p;
    }

    @Override
    public void returnBomb() {
        bombs++;
    }

    public void setStartingPosition(Tile t) {
        this.startingPosition = t;
    }

    @Override
    public void move(Direction dir) {
        if (tile == null) {
            throw new RuntimeException("Tile cannot be null");
        }
        
        Tile target = match.getTile(tile.y + dir.getY(), tile.x + dir.getX());
        if (target != null && target.passable()) {
            tile.moveBomber(this, target);
            this.tile = target;
        }
    }

    @Override
    public void endTurn() {
        if (cooldown > 0) {
            cooldown--;
            if (cooldown == 0) {
                tile = startingPosition;
                tile.addBomber(this);
            }
            //other end-of-turn actions not possible if still dead.
        }

    }

    @Override
    public void awardPoints(int amount) {
        points += amount;

    }

    @Override
    public Tile getTile() {
        return tile;
    }

    @Override
    public boolean canBomb() {
        if (tile == null) {
            return bombs > 0;
        }
        return bombs > 0 && tile.passable();
    }

    @Override
    public void dropBomb() {
        if (canBomb()) {
            bombs--;
            int timer = 0;
            for (int i = 0; i < match.bombTimerDice; i++) {
                timer += Math.random() * match.bombTimerSides + 1;
            }
            tile.putBomb(new Bomb(timer, this, tile));
        }
    }

    @Override
    public void die() {
        tile.getBombers().remove(this);
        this.tile = null;
        this.cooldown = match.dyingCooldown;
        this.points -= match.pointsLostForDying;
    }

    @Override
    public String toString() {
        if (tile == null) {
            return "p" + number + " is dead (" + cooldown
                    + " turns remaining) with " + points + " points";
        }
        return "p" + number + " at " + tile.x + "," + tile.y + " with "
                + points + " points";
    }

    public int getBomberNumber() {
        return this.number;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(Bomber.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
