package fi.helsinki.cs.bombestman.game;

import java.io.IOException;
import java.net.Socket;

public interface IBomber {

    /**
     * Remember to append \n for message - otherwise its hard for app to detect
     * when input has ended
     *
     * @param message
     * @throws IOException
     */
    public void sayToBomberman(String message) throws IOException;

    public String readFromBomberman() throws IOException;

    public void setMyProcess(Process p);

    public void destroy();

    public void returnBomb();

    void setStartingPosition(Tile t);

    public void move(Direction dir);

    public void endTurn();

    public void awardPoints(int amount);

    public Tile getTile();

    public boolean canBomb();

    public void dropBomb();

    public void die();

    public int getBomberNumber();
    
    public Socket getSocket();
    
    public void setSocket(Socket s);
}
