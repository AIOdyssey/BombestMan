package fi.helsinki.cs.bombestman.game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyBomber implements IBomber {

    private Tile tile;
    private Tile startingPosition;
    final int number;
    private int cooldown;
    private int bombs;
    private final Match match;
    private int points;
    private Socket serverSide;
    private Socket clientSide;
    PrintWriter serverSideWriter;
    BufferedReader serverSideReader;
    PrintWriter clientSideWriter;
    BufferedReader clientSideReader;

    public MyBomber(int number, int bombs, Match match) throws UnknownHostException, IOException {
        this.number = number;
        this.bombs = bombs;
        this.match = match;
        this.cooldown = 0;
        chooseStartingTile(match, number);
    }

    private void chooseStartingTile(Match match, int number) {
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

    /**
     * Remember to append \n for message - otherwise its hard for app to detect
     * when input has ended
     *
     * @param message
     * @throws IOException
     */
    @Override
    public void sayToBomberman(String message) throws IOException {
        serverSideWriter.append(message).append("\n").flush();
        message = clientSideReader.readLine();
        System.out.println("**************************************************");
        System.out.println("To player " + this.number);
        System.out.print(message);
        System.out.println("**************************************************");
    }

    @Override
    public String readFromBomberman() throws IOException {
        Scanner scan = new Scanner(System.in);
        System.out.print("Please input your command: ");
        String command = scan.nextLine();
        clientSideWriter.append(command).append("\n").flush();
        return serverSideReader.readLine();
    }

    @Override
    public void setMyProcess(Process p) {
        // Nothing here
    }

    @Override
    public void destroy() {
        // Nothing here
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
            return; //null check in case we're dead
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
        this.tile.getBombers().remove(this);
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

    //interface compliance maintained
    public void setSocket(Socket s) {
        this.serverSide = s;
        try {
            serverSideWriter = new PrintWriter(serverSide.getOutputStream(), true);
            serverSideReader = new BufferedReader(new InputStreamReader(serverSide.getInputStream()));
            clientSide = new Socket("127.0.0.1", match.port);
            clientSideWriter = new PrintWriter(clientSide.getOutputStream());
            clientSideReader = new BufferedReader(new InputStreamReader(clientSide.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(Bomber.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public Socket getSocket() {
        return serverSide;
    }
}
