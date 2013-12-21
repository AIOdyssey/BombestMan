package fi.helsinki.cs.bombestman.game;

import static fi.helsinki.cs.bombestman.game.Game.BOMB_FORCE;
import static fi.helsinki.cs.bombestman.game.Game.BOMB_TIMER_DICE;
import static fi.helsinki.cs.bombestman.game.Game.BOMB_TIMER_SIDES;
import static fi.helsinki.cs.bombestman.game.Game.DYING_COOL_DOWN;
import static fi.helsinki.cs.bombestman.game.Game.POINTS_LOST_FOR_DYING;
import static fi.helsinki.cs.bombestman.game.Game.POINTS_PER_TREASURE;
import static fi.helsinki.cs.bombestman.game.Game.TREASURE_CHANGE;
import static fi.helsinki.cs.bombestman.game.Game.TURNS;
import fi.helsinki.cs.processRunner.ProcessBotFactory;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Game {

    public static int bombersCount = 2;
    public static final int POINTS_PER_TREASURE = 3;
    public static final int POINTS_LOST_FOR_DYING = 3;
    public static final int DYING_COOL_DOWN = 5;
    public static final int BOMB_TIMER_DICE = 1;
    public static final int BOMB_TIMER_SIDES = 3;
    public static final int BOMB_FORCE = 3;
    public static final int TURNS = 30;
    public static final double TREASURE_CHANGE = 1;
    public static final int INITIAL_COUNT_OF_BOMBS = 3;

    public static int PORT_NO;// = 51291;

    /*
     * We just are evil and throw exceptions if necessary...
     *
     * @throws IOException
     */
    public static void main(final String[] args) {

        bombersCount = args.length - 1;
        PORT_NO = Integer.parseInt(args[args.length - 1]);
        IBomber[] bombers = new IBomber[bombersCount];

        // File located to the root folder of this project.
        File mapFile = new File("map1.txt");

        try {
            Match m = createMatch(bombers, mapFile);
            m.parseMap();

            createBombers(bombers, m, args);
            m.run();
            // LET'S PLAY

            System.out.println("Shall the game begin");

            do {
                String status = m.getStatus();
                System.out.print(status);
                for (IBomber iBomber : bombers) {
                    iBomber.sayToBomberman(status);
                }
                System.out.println("Status updated");

                Thread.sleep(1000);

            } while (!m.passRound());
            System.out.println(m.getStatus());

        } catch (InterruptedException ex) {
            System.out.println("interrupted" + ex);
        } catch (IOException ex) {
            System.out.println("exception" + ex);
        } finally {
            killBombers(bombers);
            System.out.println("DONE");
        }
    }

    private static void killBombers(IBomber[] bombers) {
        for (IBomber iBomber : bombers) {
            iBomber.destroy();
        }
    }

    private static void createBombers(IBomber[] bombers, Match m, String[] args) throws IOException {
        for (int i = 0; i < bombersCount; i++) {
            String aiLocation = args[i];
            bombers[i] = ProcessBotFactory.buildBomber(i, INITIAL_COUNT_OF_BOMBS, m, aiLocation);
            // In case one wants to play this interactively....
            // bombers[i] = new MyBomber(i, INITIAL_COUNT_OF_BOMBS, m);
        }
    }

    private static Match createMatch(IBomber[] bombers, File mapFile) {
        return new Match(bombers, mapFile, POINTS_PER_TREASURE, POINTS_LOST_FOR_DYING,
                DYING_COOL_DOWN, BOMB_TIMER_DICE, BOMB_TIMER_SIDES, BOMB_FORCE, TURNS,
                TREASURE_CHANGE, PORT_NO);
    }
}