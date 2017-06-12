import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Logger;

public class Login extends Plugin {

    private static final Logger logger = Logger.getLogger("Minecraft");

    private static HashMap<String, LPlayer> players = new HashMap<>();


    public static HashMap<String, LPlayer> getPlayers() {

        return players;
    }

    public static void save() {

        File folder = new File("Accounts");
        if ((!folder.exists()) && (!folder.mkdir())) {
            logger.info("[Login] Failed to create the 'Account' Folder");
        }
        File file = new File("Accounts/Accounts.hex");
        if (file.exists()) {
            file.delete();
        }
        FileWriter gWriter;
        try {
            if (!file.createNewFile()) {
                logger.info("[Login] Save Error - Create File");
            }
            gWriter = new FileWriter(file);
            if (!players.isEmpty()) {
                for (String name : players.keySet()) {
                    LPlayer lPlayer = players.get(name);
                    String account = name + ":A:" + lPlayer.password + ":A:" + lPlayer.items;

                    gWriter.flush();
                    gWriter.write(account + "\r\n");
                }
            }
            gWriter.close();
        } catch (IOException e) {
            logger.info("[Login] Error Saving Account");
        }
    }

    @Override
    public void enable() {

        logger.info("[Login] Enabled");

        logger.info("[Login] Loading Accounts");
        save();
        File file = new File("Accounts/Accounts.hex");
        if (!file.exists()) {
            logger.info("[Login] Failed to load 'Accounts.hex'");
        }

        Scanner scanner;
        try {
            scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains(":A:")) {
                    String[] data = line.split(":A:");
                    if (data.length > 2) {
                        LPlayer lPlayer = new LPlayer(data[1].trim());
                        if (data.length == 3 && data[2].length() > 4) {
                            lPlayer.items = data[2];
                        }
                        players.put(data[0].trim(), lPlayer);
                    }
                }
            }
            logger.info("[Login] Loaded Accounts Successfully");
            scanner.close();
        } catch (FileNotFoundException e) {
            logger.info("[Login] Failed to load Accounts");
            e.printStackTrace();

        }

    }

    @Override
    public void disable() {

        logger.info("[Login] Disabled");

        save();

        for (Player player : etc.getServer().getPlayerList()) {
            player.kick("§c[Login] Reloading");
        }
    }

    @Override
    public void initialize() {

        LoginListerner listerner = new LoginListerner();
        PluginLoader loader = etc.getLoader();

        loader.addListener(PluginLoader.Hook.COMMAND, listerner, this, PluginListener.Priority.HIGH);
        loader.addListener(PluginLoader.Hook.LOGIN, listerner, this, PluginListener.Priority.HIGH);
        loader.addListener(PluginLoader.Hook.DISCONNECT, listerner, this, PluginListener.Priority.HIGH);
        loader.addListener(PluginLoader.Hook.LOGINCHECK, listerner, this, PluginListener.Priority.HIGH);
        loader.addListener(PluginLoader.Hook.BLOCK_DESTROYED, listerner, this, PluginListener.Priority.HIGH);
        loader.addListener(PluginLoader.Hook.BLOCK_CREATED, listerner, this, PluginListener.Priority.HIGH);
        loader.addListener(PluginLoader.Hook.OPEN_INVENTORY, listerner, this, PluginListener.Priority.HIGH);
        loader.addListener(PluginLoader.Hook.PLAYER_MOVE, listerner, this, PluginListener.Priority.HIGH);
        loader.addListener(PluginLoader.Hook.CHAT, listerner, this, PluginListener.Priority.HIGH);

    }
}
