package com.velocitypowered.tp_duel1;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class Tp_duel_1 extends JavaPlugin implements Listener{

    private final ChallengeManager challengeManager = new ChallengeManager();
    private final MCUPlayer first_spawn = new MCUPlayer();
    private final MCUPlayer second_spawn = new MCUPlayer();

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);

    }

    @Override
    public void onDisable() {
        Player[] onlinePlayers = Bukkit.getOnlinePlayers().toArray(new Player[0]);

        /*
            This ends the duel for all players on server restart
         */
        for (Player player : onlinePlayers) {
            if (challengeManager.getFighting(player)) {
                challengeManager.endDuel(player);
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Temp commands are below -------------------------------------------------------------------------------

        if (command.getName().equalsIgnoreCase("die")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                p.setHealth(0);
                p.sendMessage("goodbye cruel world");
            }
        }
        if (command.getName().equalsIgnoreCase("amidueling")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (challengeManager.getFighting(p) != null) {
                    boolean isDueling = challengeManager.getFighting(p);
                    p.sendMessage(" " + isDueling + " ");
                } else {
                    p.sendMessage("The server doesn't think youre dueling. You might have lost your stuff because the server restarted while you were offline and dueling. I'm sorry");
                }
            }
        }
        if (command.getName().equalsIgnoreCase("gamemodeC")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                p.setGameMode(GameMode.CREATIVE);
                p.sendMessage("Welcome to creative!");
            }
        }
        if (command.getName().equalsIgnoreCase("gamemodeS")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                p.setGameMode(GameMode.SURVIVAL);
                p.sendMessage("Welcome to survival");
            }
        }
        if (command.getName().equalsIgnoreCase("day")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                p.getWorld().setTime(6000);
                p.sendMessage("daay now");
            }
        }

        //Temp commands are above -------------------------------------------------------------------------------

        /*
            This handles the command processing

            Input: The command the user typed
            Output: It executes the corresponding code
        */
        if (command.getName().equalsIgnoreCase("duel") && sender instanceof Player p) {
            String second_word = args[0];
            String action = args.length > 0 ? args[0] : "";

            switch (action.toLowerCase()) {
                case "accept":
                    handleAccept(p, args);
                    break;
                case "reject":
                    handleReject(p, args);
                    break;
                case "restore":
                    handleRestore(p);
                    break;
                case "help":
                    displayHelp(p);
                    break;
                default:
                    handleDefault(p, args);
                    break;
            }

            return false;
        }

        return false;
    }

    private void handleAccept(Player player, String[] args) {
        if (args.length == 1) {
            challengeManager.acceptChallengeRequest(player);
        } else if (args.length == 2) {
            acceptSpecificChallenge(player, args[1]);
        }
    }

    private void acceptSpecificChallenge(Player player, String targetName) {
        List<Player> challengers = challengeManager.challengeRequests.get(player);
        for (Player challenger : challengers) {
            if (challenger.getName().equals(targetName)) {
                challengeManager.acceptChallengeRequestTargeted(challenger, player);
            }
        }
    }

    private void handleReject(Player player, String[] args) {
        if (args.length == 1) {
            List<Player> challenger_list = challengeManager.challengeRequests.get(player);
            challenger_list.remove(0); //depending on start or end of list
        } else if (args.length == 2) {
            rejectSpecificChallenge(player, args[1]);
        }
    }

    private void rejectSpecificChallenge(Player player, String rejectedChallenger){
        List<Player> challengers = challengeManager.challengeRequests.get(player);
        for (Player challenger : challengers) {
            if (challenger.getName().equals(rejectedChallenger)) {
                challengeManager.challengeRequests.remove(challenger);
            }
        }
    }

    private void handleRestore(Player player) {
        try {
            if (challengeManager.getFighting(player)) {
                challengeManager.endDuel(player);
            } else {
                player.sendMessage("You should have already received your stuff. Please talk to a mod if this isn't the case.");
            }
        } catch (NullPointerException e) {
            player.sendMessage("Something went wrong. Please try to remember exactly what you did leading up to this, and describe the events to a mod");
        }
    }

    private void displayHelp(Player player) {
        player.sendMessage("\u00A79Here is a summary of all duel commands:" + "\n" +
                "\u00A7f" + "/duel <playername> Lets you challenge the specified player to a duel" + "\n" +
                "/duel accept  Lets you accept the most recent duel challenge you've received" + "\n" +
                "/duel restore Lets you leave a duel and/or get your stuff back if there is a glitch");
    }

    private void handleDefault(Player player, String[] args) {
        if (args.length == 1) {
            sendChallenge(player, args[0]);
        } else {
            player.sendMessage("/duel <playername> or /duel accept");
        }
    }

    private void sendChallenge(Player player, String playerName) {
        try {
            Player targetPlayer = Bukkit.getPlayer(playerName);
            if (targetPlayer != null) {
                challengeManager.sendChallengeRequest(player, targetPlayer);
            } else {
                player.sendMessage("The target player is not a player.");
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage("Please provide a valid player name.");
        }
    }

     /*
        Respawn event handler details:
        Allows the duel to end for the respawning player as a safety measure

        Edge cases I checked for
        - One guy restores then other guy respawns
        - One guy respawns then other guy restore
     */
    @EventHandler
    public void onPlayerRespawn (PlayerRespawnEvent event){
        if (challengeManager.getFighting(event.getPlayer()) != null && challengeManager.getFighting(event.getPlayer()).equals(true)) { //if the player who died was dueling && duelManager.getDueling(event.getPlayer())!=null
            Player ourguy = event.getPlayer();
            ourguy.sendMessage("You lost! Loser!");

            Bukkit.getScheduler().runTaskLater(this, () -> {
                Player opponent = Bukkit.getPlayer(challengeManager.getOpponent(ourguy));

                challengeManager.endDuel(ourguy); //if this is called he was dueling
                //ourguy.setLevel(((int) (ourguy.getLevel() * .8))); //sets player level lower if they die, if desired

                assert opponent != null;
                if (opponent.isOnline() && challengeManager.getFighting(opponent) && (!opponent.isDead())) {
                    challengeManager.endDuel(opponent);
                }
                //guy1
            }, 20L);
        }
        //event.getPlayer().sendMessage("got to the end");
        event.getPlayer().sendMessage(challengeManager.getFighting(event.getPlayer()).toString());
    }



    @EventHandler
    /* Death event details:
        This makes sure the dueling players dont drop their stuff on the ground
        also makes sure the player doesnt lose items or levels when they die
    */
    public void onPlayerDeath (PlayerDeathEvent event){// Prevent items from dropping on death
        Player deadPlayer = event.getPlayer();
        if (challengeManager.getFighting(deadPlayer) != null) {
            if (challengeManager.getFighting(deadPlayer)) {
                event.getDrops().clear();
                event.setKeepInventory(true);
                event.setKeepLevel(true);
            }
        }
    }

    /*Disconnect Event Details:
    Basically, if a player DC's while in a duel, the other player wins

    Edge cases I checked for:
    - what if one guy restores other guy DC's *
    - one guy DC's other guy restores *
    - what if one guy resapwns other guy DC's *On respawn theyd both get it
    - what if one guy DC's other guy respawns *
     */
    @EventHandler
    public void playerDisconnect (PlayerQuitEvent event){
        Player player = event.getPlayer();
        if (challengeManager.getFighting(player) != null) {
            if (challengeManager.getFighting(player)) {
                challengeManager.endDuel(player);
            }
        }
    }

}



        /*
        if (command.getName().equalsIgnoreCase("duel") && sender instanceof Player p) {
            String second_word = args[0];
            String action = args.length > 0 ? args[0] : "";

            if (args.length == 1 && second_word.equals("accept")) {
                challengeManager.acceptChallengeRequest(p);
            } else if (args.length == 2 && second_word.equals("accept")){ //if they type /duel accept <playername>
                List<Player> original_sender_lst = challengeManager.challengeRequests.get(p); //get the list of all players who sent them an invite
                for (Player offerer: original_sender_lst) { //look through all the players and accept the request with the specified player name
                    if (offerer.getName().equals(args[1])){
                        challengeManager.acceptChallengeRequestTargeted(offerer,p);
                    }
                }
            } else if (args.length == 1 && (second_word.equals("reject"))) {
                challengeManager.challengeRequests.remove(p);
            } else if (args.length == 1 && (second_word.equals("restore"))) {
                try {
                    if (challengeManager.getFighting(p)) {
                        challengeManager.endDuel(p);
                    } else {
                        p.sendMessage("You should have already received your stuff. Please talk to a mod if this isnt the case.");
                    }
                } catch (NullPointerException e) {
                    p.sendMessage("Something went wrong. Please try to remember exactly what you did leading up to this, and describe the events to a mod");
                }
            } else if (args.length == 1 && (second_word.equals("help"))) {
                p.sendMessage("\u00A79Here is a summary of all duel commands:" + "\n" + "\u00A7f" + "/duel <playername> Lets you challenge the specified player to a duel" + "\n" + "/duel accept  Lets you accept the most recent duel challenge you've received" + "\n" + "/duel restore Lets you leave a duel and/or get your stuff back if there is a glitch");
            } else if (args.length == 1) { //this sends a challenge to a player
                try {
                    Player targetPlayer = Bukkit.getPlayer(second_word);
                    if (targetPlayer != null) {
                        challengeManager.sendChallengeRequest(p, targetPlayer);
                    } else {
                        p.sendMessage("the target player is not a player");
                    }
                } catch (IllegalArgumentException e) {
                    p.sendMessage("Please provide a valid player name");
                }
            } else { //unidentified command
                p.sendMessage("/duel <playername> or /duel accept");
            }
        }
        return false;

         */
