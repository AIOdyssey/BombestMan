package fi.helsinki.cs.bombestman.game;

import java.util.LinkedList;
import java.util.List;

public class Tile {
    final int x, y;
    private TileType type;
    private Bomb bomb;
    private final List<IBomber> bombers;
    private final Match match;
    
    public Tile(int x, int y, TileType type, Match match) {
        this.x = x;
        this.y = y;
        this.type = type;
        bombers = new LinkedList<IBomber>();
        this.match = match;
    } 
    public boolean passable() {
        return type.passable() && bomb == null;
    }
    
    public TileType getType() {
        return type;
    }
    public void setType(TileType t) {
        type = t;
    }

    public void addBomber(IBomber b){
        bombers.add(b);
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.x;
        hash = 97 * hash + this.y;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Tile other = (Tile) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        return true;
    }
    
    public void moveBomber(IBomber b, Tile t) {
        bombers.remove(b);
        t.bombers.add(b);
    }
    
    public void collectTreasure() {
        if(type == TileType.TILE_TREASURE) {
            type = TileType.TILE_FLOOR;
            for(IBomber b : bombers) {
                b.awardPoints(match.pointsPerTreasure / bombers.size());
            }
        }
    }
    
    @Override
    public String toString() {
        if (!bombers.isEmpty()) {
            return bombers.get(0).getBomberNumber() + "";
        }
        else if (bomb != null) {
            return "b"; //bomb.toString();
        }
        else return type.toString();
    }
    
    public void putBomb(Bomb b) {
        this.bomb = b;
    }
    
    public Bomb getBomb() {
        return bomb;
    }
    
    public List<IBomber> getBombers() {
        return bombers;
    }    
}
