package fi.helsinki.cs.bombestman.game;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * A single game of Bombestman
 *
 * @author kviiri
 */
public class Match implements Runnable {

    private IBomber[] bombers;
    private Tile[][] map;

    //match parameters
    final int pointsPerTreasure;
    final int pointsLostForDying;
    final int dyingCooldown;
    final int bombTimerDice;
    final int bombTimerSides;
    final int bombForce;
    final double treasureChance;
    private final File mapFile;
    int turns;

    //public ServerSocket socket;
    final int port;

    //keeping this as internal state for less garbage
    private final Set<Tile> explodedTiles = new HashSet<Tile>();

    public Match(IBomber[] bombers, File mapFile, int pointsPerTreasure,
            int pointsLostForDying, int dyingCooldown, int bombTimerDice,
            int bombTimerSides, int bombForce, int turns, double treasureChance,
            int port) {
        this.bombers = bombers;
        this.mapFile = mapFile;
        this.pointsPerTreasure = pointsPerTreasure;
        this.pointsLostForDying = pointsLostForDying;
        this.dyingCooldown = dyingCooldown;
        this.bombTimerDice = bombTimerDice;
        this.bombTimerSides = bombTimerSides;
        this.bombForce = bombForce;
        this.turns = turns;
        this.treasureChance = treasureChance;
        this.port = port;
    }

    public int getMapWidth() {
        return this.map[0].length;
    }

    public int getMapHeight() {
        return this.map.length;
    }

    public boolean passRound() throws IOException {
//        System.out.println("Getting commands");
        String[] commands = getCommands();
        //execute move commands
        for (int i = 0; i < commands.length; i++) {
            if (commands[i] == null) {
                System.out.println("command is null");
                continue;
            }

            String[] parsed = commands[i].split(" ");

            if (parsed.length >= 2 && parsed[0].equalsIgnoreCase("move")) {
                Direction dir = Direction.getDir(parsed[1]);
                if (dir != null) {
                    bombers[i].move(dir);
                }
            }
        }

        giveTreasurePoints();
        //execute bomb commands
        for (int i = 0; i < commands.length; i++) {
            if (commands[i].equalsIgnoreCase("bomb")
                    && bombers[i].canBomb()) {
                bombers[i].dropBomb();
            }
        }

        //explosion checks
        explodedTiles.clear();
        for (Bomb b : collectBombs()) {
            if (b.endTurn()) {
                collectBombs().remove(b);
                b.remove();
                calculateExplosionArea(b.tile, explodedTiles, bombForce);
            }
        }

        for (Tile t : explodedTiles) {

            switch (t.getType()) {
                case TILE_TREASURE:
                    t.setType(TileType.TILE_FLOOR);
                    break;
                case TILE_SOFTBLOCK:
                    t.setType(Math.random() < treasureChance
                            ? TileType.TILE_TREASURE
                            : TileType.TILE_FLOOR);
                    break;
            }
        }

        for (IBomber b : bombers) {
            b.endTurn();
            if (explodedTiles.contains(b.getTile())) {
                b.die();
            }
        }
        return (--turns == 0);
    }

    private void calculateExplosionArea(Tile t, Set<Tile> out, int force) {
        if (out.contains(t)) {
            return; //fast exit if the bomb was already detonated recursively
        }
        out.add(t);
        calculateExplosionArea(getNeighbor(t, Direction.UP), Direction.UP, out, force);
        calculateExplosionArea(getNeighbor(t, Direction.LEFT), Direction.LEFT, out, force);
        calculateExplosionArea(getNeighbor(t, Direction.RIGHT), Direction.RIGHT, out, force);
        calculateExplosionArea(getNeighbor(t, Direction.DOWN), Direction.DOWN, out, force);
    }

    private void calculateExplosionArea(Tile t, Direction dir, Set<Tile> out, int force) {
        if (t == null) {
            return;
        }
        if (t.getBomb() != null) {
            collectBombs().remove(t.getBomb());
            t.getBomb().remove();
            calculateExplosionArea(t, out, bombForce);
        }
        out.add(t);
        if (force > 0 && t.passable()) {
            calculateExplosionArea(getNeighbor(t, dir), dir, out, force - 1);
        }
    }

    /**
     *
     * @return the game state as a String
     */
    protected String getStatus() {
        StringBuilder buf = new StringBuilder();
        buf.append("Turns left: ").append(turns).append("\n");
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                buf.append(map[i][j].toString());
            }
            buf.append('\n');
        }
        for (IBomber bomber : bombers) {
            buf.append(bomber.toString()).append('\n');
        }
        for (Bomb b : collectBombs()) {
            buf.append(b.toString()).append('\n');
        }
        buf.append('\n');
        buf.append('\n');
        return buf.toString();

    }

    public Tile getTile(int y, int x) {
        if (x < 0 || x >= map[0].length
                || y < 0 || y >= map.length) {
            return null;
        } else {
            return map[y][x];
        }
    }

    public Tile[][] getTiles() {
        return this.map;
    }

    public Tile getNeighbor(Tile t, Direction d) {
        return getTile(t.y + d.getY(), t.x + d.getX());
    }

    protected String getMapString() {
        StringBuilder buf = new StringBuilder();
        for (Tile[] map1 : map) {
            for (int j = 0; j < map.length; j++) {
                buf.append(map1[j].toString());
            }
            buf.append('\n');
        }
        return buf.toString();
    }

    public void parseMap() {
        Tile[][] ret = null;
        Scanner read = null;
        try {
            read = new Scanner(this.mapFile);
            int mapwidth = read.nextInt();
            int mapheight = read.nextInt();
            read.nextLine();
            ret = new Tile[mapwidth][mapheight];
            for (int i = 0; i < mapheight; i++) {
                String line = read.nextLine();
                for (int j = 0; j < mapwidth; j++) {
                    TileType t;
                    switch (line.charAt(j)) {
                        case '#':
                            t = TileType.TILE_HARDBLOCK;
                            break;
                        case '?':
                            t = TileType.TILE_SOFTBLOCK;
                            break;
                        case '$':
                            t = TileType.TILE_TREASURE;
                            break;
                        case '.':
                            t = TileType.TILE_FLOOR;
                            break;
                        default:
                            t = TileType.TILE_FLOOR;
                    }
                    ret[i][j] = new Tile(j, i, t, this);
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("DBG: File not found");
            throw new RuntimeException("cannot continue without map");
        } finally {
            if (read != null) {
                read.close();
            }
        }
        this.map = ret;
    }

    private void giveTreasurePoints() {
        for (int i = 0; i < bombers.length; i++) {
            if (bombers[i].getTile() != null) {
                bombers[i].getTile().collectTreasure();
            }
        }
    }

    private String[] getCommands() throws IOException {
        String result[] = new String[bombers.length];
        for (int i = 0; i < bombers.length; i++) {
            IBomber b = bombers[i];

            try {
                String red = b.readFromBomberman();
                result[i] = red.replaceAll("\n", "  ");
            } catch (NullPointerException e) {
                System.out.println("DBG: read from bomberissa NPE" + e);
                result[i] = "WAIT";
            }
        }
        return result;

    }

    public List<Bomb> collectBombs() {
        List<Bomb> bombs = new ArrayList<Bomb>();
        for (Tile[] tiles : this.map) {
            for (int i = 0; i < tiles.length; i++) {
                Tile tile = tiles[i];
                if (tile.getBomb() != null) {
                    bombs.add(tile.getBomb());
                }
            }
        }
        return bombs;
    }

    @Override
    public void run() {
    }
}
