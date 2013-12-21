package fi.helsinki.cs.bombestman.game;

public enum TileType {
    TILE_TREASURE,
    TILE_HARDBLOCK,
    TILE_SOFTBLOCK,
    TILE_FLOOR;
    
    public boolean passable() {
        return this == TILE_TREASURE ||
               this == TILE_FLOOR;
    }
    
    @Override
    public String toString() {
        switch(this) {
            case TILE_TREASURE:
                return "$";
            case TILE_HARDBLOCK:
                return "#";
            case TILE_SOFTBLOCK:
                return "?";
            case TILE_FLOOR:
            default:
                return ".";
        }
    }
}
