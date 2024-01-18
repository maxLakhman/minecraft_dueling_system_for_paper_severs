package com.velocitypowered.untitled2;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class Untitled2 extends JavaPlugin implements Listener {
    private final DuelManager duelManager = new DuelManager();

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // /die kills player
        if(command.getName().equalsIgnoreCase("die")){
            if (sender instanceof Player){
                Player p = (Player) sender;
                p.setHealth(0);
                p.sendMessage("goodbye cruel world");
            }
        }


        // /amidueling
        if(command.getName().equalsIgnoreCase("amidueling")){
            if (sender instanceof Player){
                Player p = (Player) sender;
                boolean isDueling = duelManager.getDueling(p);
                p.sendMessage(" " + isDueling + " ");
            }
        }

        // sets gamemode c
        if(command.getName().equalsIgnoreCase("gamemodeC")){
            if (sender instanceof Player){
                Player p = (Player) sender;
                p.setGameMode(GameMode.CREATIVE);
                p.sendMessage("Welcome to creative!");
            }
        }

        // sets gamemode s
        if(command.getName().equalsIgnoreCase("gamemodeS")){
            if (sender instanceof Player){
                Player p = (Player) sender;
                p.setGameMode(GameMode.SURVIVAL);
                p.sendMessage("Welcome to survival");
            }
        }

        if(command.getName().equalsIgnoreCase("day")){
            if (sender instanceof Player){
                Player p = (Player) sender;
                p.getWorld().setTime(6000);
                p.sendMessage("daay now");
            }
        }

        //what about pets? player world? /spawn? sheidls and armor DUEL WITH YOURSELF?
        //make a command for teleporting someone back to their bed with stuff, health


        // /duel <playername> sends a duel invitation
        else if(command.getName().equalsIgnoreCase("duel")){
            if (sender instanceof Player){                          //checks that the sender is a player
                Player p1 = (Player) sender;
                if (args.length == 1 && (args[0].equals("accept"))){
                    duelManager.acceptDuelRequest(p1);
                }
                else if (args.length == 1 && (args[0].equals("reject"))){
                    duelManager.duelRequests.remove(p1);
                }
                else if (args.length == 1 && (args[0].equals("restore"))){
                    try {
                        if (duelManager.getDueling(p1)){
                            duelManager.endDuel(p1);
                        }else {
                            p1.sendMessage("You should have already received your stuff. Please talk to a mod if this isnt the case.");
                        }
                    }catch(NullPointerException e){
                        p1.sendMessage("idk what went wrong ");
                    }
                }
                else if (args.length == 1 && (args[0].equals("help"))){
                    p1.sendMessage("Here is a summary of all duel commands:" + System.lineSeparator() + "/duel <playername> Lets you challenge the specified player to a duel" + System.lineSeparator() + "/duel accept  Lets you accept the most recent duel challenge you've received" + System.lineSeparator() + "/duel restore Lets you leave a duel and/or get your stuff back if there is a glitch");
                }
                else if(args.length == 1) {                    //makes sure they typed in 1 argument
                    try {
                        String otherPlayerName = args[0];
                        Player targetPlayer = Bukkit.getPlayer(otherPlayerName);
                        assert targetPlayer != null;
                        duelManager.sendDuelRequest(p1, targetPlayer);
                    } catch (IllegalArgumentException e) {
                        p1.sendMessage("Please provide a valid player name");
                    }
                }
                else{
                    p1.sendMessage("/duel <playername> or /duel accept");
                }
            }
        }
        return true;
    }

    //DC edge cases
    //it works if 1 guy leaves and other dies
    //works if both leave
    //works if both die
    //try both ways to be safe
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
        if(duelManager.getDueling(event.getPlayer()) ){ //if the player who died was dueling && duelManager.getDueling(event.getPlayer())!=null
            Player ourguy = event.getPlayer();
            ourguy.sendMessage("You lost! Loser!");

            Bukkit.getScheduler().runTaskLater(this, () -> {
                Player opponent = Bukkit.getPlayer(duelManager.getOpponent(ourguy));

                duelManager.endDuel(ourguy); //if this is called he was dueling
                //ourguy.setLevel(((int) (ourguy.getLevel() * .8))); //sets player level lower if they die

                assert opponent != null;
                if (opponent.isOnline() && duelManager.getDueling(opponent)){
                    duelManager.endDuel(opponent);
                }
                //guy1
            }, 20L);
        }
    }

    //this makes sure the player doesnt drop items when they die
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event){ //prevent them from pressing Q
        Player deadPlayer = event.getPlayer();
        if (duelManager.getDueling(deadPlayer)!= null){
            if(duelManager.getDueling(deadPlayer)){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {// Prevent items from dropping on death
        Player deadPlayer = event.getPlayer();
        if (duelManager.getDueling(deadPlayer)!= null){
            if(duelManager.getDueling(deadPlayer)){
                event.getDrops().clear();
            }
        }
    }

}
