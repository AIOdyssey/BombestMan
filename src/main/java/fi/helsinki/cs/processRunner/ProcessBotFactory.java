package fi.helsinki.cs.processRunner;

import fi.helsinki.cs.bombestman.game.Bomber;
import fi.helsinki.cs.bombestman.game.Game;
import fi.helsinki.cs.bombestman.game.IBomber;
import fi.helsinki.cs.bombestman.game.Match;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;

public class ProcessBotFactory {

    public static IBomber buildBomber(int number, int bombs, Match match, String projectFolder) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(projectFolder + "/run.sh", Integer.toString(Game.PORT_NO));

        Map<String, String> env = pb.environment();
        env.put("PATH", System.getenv("PATH"));
        
        pb.directory(new File(projectFolder));
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        
        IBomber bomb = new Bomber(number, bombs, match, projectFolder);
        bomb.setMyProcess(process);

        return bomb;
    }
        
}
