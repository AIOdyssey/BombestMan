package fi.helsinki.cs.bombestman.game;

/**
 * A bomb. It's going to blow.
 */
public class Bomb {
    private int timer;
    final Tile tile;
    private IBomber owner;
    
    public Bomb(int timer, IBomber owner, Tile tile) {
        this.timer = timer;
        this.owner = owner;
        this.tile = tile;
    }
    
    /**
     * 
     * @return true iff the bomb is going to blow on this turn
     */
    public boolean endTurn() {
        timer--;
        return timer == 0;
    }
    
    public IBomber getOwner() {
        return owner;
    }
    
    public void remove() {
        tile.putBomb(null);
        owner.returnBomb();
    }
    
    @Override
    public String toString() {
        return "bomb at " + tile.x + "," + tile.y;
    }
}
