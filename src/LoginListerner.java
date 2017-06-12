import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginListerner extends PluginListener {


    private static void setPerms(Player player) {

        if (etc.getDataSource().doesPlayerExist(player.getName())) {
            Player data = etc.getDataSource().getPlayer(player.getName());
            player.setAdmin(data.isAdmin());
            player.setCanModifyWorld(data.canBuild());
            player.setCommands(data.getCommands());
            player.setGroups(data.getGroups());
            player.setIgnoreRestrictions(data.canIgnoreRestrictions());
        }
    }

    private static String getPassword(String password) {

        try {
            MessageDigest code = MessageDigest.getInstance("MD5");
            byte[] bytes = password.getBytes();
            code.update(bytes, 0, bytes.length);
            BigInteger bigInteger = new BigInteger(1, code.digest());
            return String.format("%1$032X", bigInteger).toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static LPlayer getPlayer(String name) {

        if (!Login.getPlayers().isEmpty() && Login.getPlayers().containsKey(name)) {
            return Login.getPlayers().get(name);
        }
        return null;
    }

    private static boolean isLogin(String name) {

        return getPlayer(name) != null && getPlayer(name).status > 1;

    }

    private void getInventory(Player player) {

        LPlayer lPlayer = getPlayer(player.getName());
        if (lPlayer != null) {
            String list = lPlayer.items;
            if (!list.contains(":I:")) {
                return;
            }

            String[] items = list.split(":I:");
            String[] arrays;
            for (int i = 0; i < (arrays = items).length; i++) {
                String item = arrays[i];
                if (item.contains("::")) {
                    String[] data = item.split("::");
                    if (item.length() > 6) {
                        player.getInventory().setSlot(Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[3]), Integer.parseInt(data[2]));
                    }
                }
            }
            player.getInventory().update();
        }
    }

    private void setInventory(Player player) {

        LPlayer lPlayer = getPlayer(player.getName());
        if (lPlayer != null) {
            Item[] items = player.getInventory().getContents().clone();
            StringBuilder list = new StringBuilder();
            if (items != null) {
                Item[] arrays;
                for (int i = 0; i < (arrays = items).length; i++) {
                    Item gItem = arrays[i];
                    if (gItem != null) {
                        list.append(gItem.getItemId()).append("::").append(gItem.getAmount()).append("::").append(gItem.getSlot()).append("::").append(gItem.getDamage()).append(":I:");
                    }
                }
            }
            lPlayer.items = list.toString();
        }
    }

    @Override
    public boolean onCommand(Player player, String[] split) {

        String playerName = player.getName();
        if (split[0].equalsIgnoreCase("/login")) {
            LPlayer lPlayer = getPlayer(playerName);
            if (lPlayer != null && split.length > 1) {
                if (lPlayer.status > 1) {
                    return true;
                }

                String password = getPassword(split[1]);
                if (password.contains(lPlayer.password.trim())) {
                    getInventory(player);
                    lPlayer.status = 2;
                    lPlayer.items = "";
                    setPerms(player);
                    player.sendMessage("§2Welcome! " + playerName);
                    return true;
                }
                player.kick("§cBad Password!");
                return true;
            }
            return true;
        }
        if (split[0].equalsIgnoreCase("/register")) {
            LPlayer lPlayer = getPlayer(playerName);
            if (lPlayer != null) {
                player.sendMessage("§cAccount \"" + playerName + "\" already!");
            } else if (split.length > 1) {
                Matcher matcher = Pattern.compile("[0-9a-zA-Z]+").matcher(playerName);
                if (playerName.length() > 16 || playerName.length() < 4) {
                    player.sendMessage("§cUsername should be 4-16 chars.");
                } else if (matcher.find() && matcher.group() != playerName) {
                    player.sendMessage("§cUsername should be [0-9 a-z A-Z]");
                } else {
                    LPlayer nPlayer = new LPlayer(getPassword(split[1].trim()));
                    nPlayer.status = 2;
                    Login.getPlayers().put(playerName.trim(), nPlayer);
                    setPerms(player);

                    Login.save();
                    player.sendMessage("§2Success!");
                }
            }
            return true;
        }
        if (split[0].equalsIgnoreCase("/reset-password") || split[0].equalsIgnoreCase("/resetpassword")) {
            //TODO:
        }
        return false;
    }

    @Override
    public String onLoginChecks(String user) {

        LPlayer player = getPlayer(user);
        if (player != null && player.status > 1) {
            player.status = 1;
        }

        return null;
    }

    @Override
    public void onLogin(Player player) {

        String playerName = player.getName();

        player.setCanModifyWorld(false);
        player.setIgnoreRestrictions(false);
        player.setAdmin(false);
        player.setCommands(new String[]{"register", "login"});
        player.setGroups(new String[0]);

        LPlayer lPlayer = getPlayer(playerName);
        if (lPlayer != null) {
            if (lPlayer.status == 1) {
                lPlayer.status = 0;
                player.kick("§cPlayer is Online!");
            }
            if (lPlayer.items.length() < 4) {
                setInventory(player);
            }

            player.getInventory().clearContents();
            player.sendMessage("§2§c /login <Password>");
            return;
        }
        player.sendMessage("§2§c /register <Password>");

    }

    @Override
    public void onDisconnect(Player player) {

        LPlayer lPlayer = getPlayer(player.getName());
        if (lPlayer != null) {
            lPlayer.status = 0;
        }
    }

    //TODO: Add Chat Message
    @Override
    public boolean onBlockDestroy(Player player, Block block) {

        return !isLogin(player.getName());
    }

    @Override
    public boolean onBlockCreate(Player player, Block blockPlaced, Block blockClicked, int itemInHand) {

        return !isLogin(player.getName());
    }

    @Override
    public boolean onBlockPlace(Player player, Block blockPlaced, Block blockClicked, Item itemInHand) {

        return !isLogin(player.getName());
    }

    @Override
    public boolean onOpenInventory(Player player, Inventory inventory) {

        return !isLogin(player.getName());
    }

    @Override
    public boolean onChat(Player player, String message) {

        return !isLogin(player.getName());
    }

    @Override
    public void onPlayerMove(Player player, Location from, Location to) {

        if (!isLogin(player.getName())) {
            player.teleportTo(from);
        }
    }


}
