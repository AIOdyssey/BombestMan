package fi.helsinki.cs.bombestman.game;

import static fi.helsinki.cs.bombestman.game.Game.BOMB_FORCE;
import static fi.helsinki.cs.bombestman.game.Game.BOMB_TIMER_DICE;
import static fi.helsinki.cs.bombestman.game.Game.BOMB_TIMER_SIDES;
import static fi.helsinki.cs.bombestman.game.Game.DYING_COOL_DOWN;
import static fi.helsinki.cs.bombestman.game.Game.POINTS_LOST_FOR_DYING;
import static fi.helsinki.cs.bombestman.game.Game.POINTS_PER_TREASURE;
import static fi.helsinki.cs.bombestman.game.Game.TREASURE_CHANCE;
import static fi.helsinki.cs.bombestman.game.Game.TURNS;
import fi.helsinki.cs.processRunner.ProcessBotFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

public class Game {

    public static int bombersCount;
    public static final int POINTS_PER_TREASURE = 1;
    public static final int POINTS_LOST_FOR_DYING = 3;
    public static final int DYING_COOL_DOWN = 10;
    public static final int BOMB_TIMER_DICE = 4;
    public static final int BOMB_TIMER_SIDES = 3;
    public static final int BOMB_FORCE = 4;
    public static final int TURNS = 200;
    public static final double TREASURE_CHANCE = 0.2;
    public static final int INITIAL_COUNT_OF_BOMBS = 2;

    public static int PORT_NO;// = 51291;

    /*
     * We just are evil and throw exceptions if necessary...
     *
     * @throws IOException
     */
    public static void main(final String[] args) throws IOException {

        bombersCount = args.length - 1;
        PORT_NO = Integer.parseInt(args[args.length - 1]);
        IBomber[] bombers = new IBomber[bombersCount];

        String map
                = "...?????...\n"
                + ".#.#?#?#.#.\n"
                + "..???????..\n"
                + "?#?#?#?#?#?\n"
                + "????.$.????\n"
                + "?#?#$$$#?#?\n"
                + "????.$.????\n"
                + "?#?#?#?#?#?\n"
                + "..???????..\n"
                + ".#.#?#?#.#.\n"
                + "...?????...";

        // File located to the root folder of this project.
//         File mapFile = new File("map1.txt");
        File mapFile = File.createTempFile("map", "txt");
        FileWriter fw = new FileWriter(mapFile);
        fw.write("11 11\n");
        fw.append(map);
        fw.append("\n");
        fw.flush();
        fw.close();
        try {

            Match m = createMatch(bombers, mapFile);
            m.parseMap();
            createBombers(bombers, m, args);
            m.run();

            // Lets give initial details to bombers:
            String initialMessage = "BEGIN MSG\n"
                    + "map width: " + m.getMapWidth() + "\n"
                    + "map height: " + m.getMapHeight() + "\n"
                    + "bombersCount " + bombersCount + "\n"
                    + "POINTS_PER_TREASURE " + POINTS_PER_TREASURE + "\n"
                    + "POINTS_LOST_FOR_DYING " + POINTS_LOST_FOR_DYING + "\n"
                    + "DYING_COOL_DOWN " + DYING_COOL_DOWN + "\n"
                    + "BOMB_TIMER_DICE " + BOMB_TIMER_DICE + "\n"
                    + "BOMB_TIMER_SIDES " + BOMB_TIMER_SIDES + "\n"
                    + "BOMB_FORCE " + BOMB_FORCE + "\n"
                    + "TURNS " + TURNS + "\n"
                    + "TREASURE_CHANGE " + TREASURE_CHANCE + "\n"
                    + "INITIAL_COUNT_OF_BOMBS " + INITIAL_COUNT_OF_BOMBS + "\n"
                    + "MAP: \n"
                    + m.getMapString()
                    + "END MSG\n\n";
            sendToAllBots(bombers, initialMessage);

            // LET'S PLAY
//            System.out.println("Shall the game begin");
            do {
                String status = m.getStatus();
                System.out.print(status);
                sendToAllBots(bombers, status);

                Thread.sleep(1000);

            } while (!m.passRound());
            System.out.println(m.getStatus());

            sendToAllBots(bombers, "THE END\n");

        } catch (InterruptedException ex) {
            System.out.println("interrupted" + ex);
        } catch (IOException ex) {
            System.out.println("exception" + ex);
        } finally {
            System.out.println("Starting to kill bombers");
            killBombers(bombers);
            System.out.println("DONE");
        }
    }

    private static void sendToAllBots(IBomber[] bombers, String status) throws IOException {
        for (IBomber iBomber : bombers) {
            iBomber.sayToBomberman(status);
        }
        System.out.print(status);
    }

    private static void killBombers(IBomber[] bombers) {
        for (IBomber iBomber : bombers) {
            iBomber.destroy();
        }
    }

    private static void createBombers(IBomber[] bombers, Match m, String[] args) throws IOException {
        ServerSocket socket = new ServerSocket(PORT_NO);
        for (int i = 0; i < bombersCount; i++) {
            String aiLocation = args[i];
            bombers[i] = ProcessBotFactory.buildBomber(i, INITIAL_COUNT_OF_BOMBS, m, aiLocation);
            bombers[i].setSocket(socket.accept());
            // In case one wants to play this interactively....
            // bombers[i] = new MyBomber(i, INITIAL_COUNT_OF_BOMBS, m);
        }
    }

    private static Match createMatch(IBomber[] bombers, File mapFile) {
        return new Match(bombers, mapFile, POINTS_PER_TREASURE, POINTS_LOST_FOR_DYING,
                DYING_COOL_DOWN, BOMB_TIMER_DICE, BOMB_TIMER_SIDES, BOMB_FORCE, TURNS,
                TREASURE_CHANCE, PORT_NO);
    }
}
