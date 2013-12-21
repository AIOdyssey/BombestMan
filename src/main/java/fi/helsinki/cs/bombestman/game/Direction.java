package fi.helsinki.cs.bombestman.game;

public enum Direction {
    RIGHT,
    DOWN,
    LEFT,
    UP;
    
    public int getX() {
        if(this == RIGHT) return 1;
        if(this == LEFT) return -1;
        return 0;
    }
    public int getY() {
        if(this == DOWN) return 1;
        if(this == UP) return -1;
        return 0;
    }
    
    static Direction getDir(String s) {
        if(s.equalsIgnoreCase("r")) return RIGHT;
        if(s.equalsIgnoreCase("d")) return DOWN;
        if(s.equalsIgnoreCase("l")) return LEFT;
        if(s.equalsIgnoreCase("u")) return UP;
        return null;
    }
}