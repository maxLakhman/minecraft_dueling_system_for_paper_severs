package com.velocitypowered.tp_duel1;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
    TO DO:

    - Implement the betting system
    - make it so you can specifically accept a certain duel request
    - make it so duel requests are clickable
    - make it so if they /duelfair while in a duel it doesnt glitch
*/

//the jar file is in build -> libs

public class ChallengeManager {
    public Map<Player, List<Player>> challengeRequests = null; //could make this a list if they specify it searches through at most 10 requests before it expires
    // challengeRequests given by {target: sender}
    private Map<String, Boolean> isFighting;
    private Map<String, String> opponents;
    public Map<String, MCUPlayer> fightingPlayerInfo;
    public ChallengeManager() {
        this.challengeRequests = new HashMap<>();
        this.isFighting = new HashMap<>();
        this.opponents = new HashMap<>();
        this.fightingPlayerInfo = new HashMap<>();
    }

    public void sendChallengeRequest(Player sender, Player target) {
        /*
            Add the challenge request to the challenge requests variable
         */
        if (this.challengeRequests.get(target) == null){
            List<Player> sender_list = new ArrayList<>();
            sender_list.add(sender);
            this.challengeRequests.put(target, sender_list);
        } else{
            List<Player> sender_list =  this.challengeRequests.get(target);
            sender_list.add(sender);
            this.challengeRequests.put(target, sender_list);
        }

        /*
            Create and send the clickable message to the player
         */
        TextComponent part1 = Component.text(sender.getName() + " invited you to a duel ");
        TextComponent part2 = Component.text()
                .content("Accept ")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.BOLD, true)
                .clickEvent(ClickEvent.runCommand("/duel accept " + sender.getName())).build();
        TextComponent part3 = Component.text()
                .content("Reject")
                .color(NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true)
                .clickEvent(ClickEvent.runCommand("/duel reject " + sender.getName())).build();
        TextComponent combinedComponent = part1.append(part2).append(part3);
        target.sendMessage(combinedComponent);
    }

    public void acceptChallengeRequestTargeted(Player sender, Player target){
        Player originalSender = sender; //offerer
        Player acceptor = target;  //player who typed accept

        /*
            Edge cases below
         */
        //What if one of the players goes offline
        if (originalSender == null && acceptor == null){
            return;
        }else if(originalSender == null){
            acceptor.sendMessage("One of the player objects is null, did the player who sent you an invite go offline?");
            return;
        }else if (acceptor ==null){
            originalSender.sendMessage("One of the player objects is null, did the player who sent you an invite go offline?");
            return;
        }
        //what if they challenge while in a challenge
        if (!(this.getFighting(acceptor) == null || this.getFighting(originalSender)==null)){
            if (this.getFighting(acceptor).equals(true) || this.getFighting(originalSender).equals(true)){
                acceptor.sendMessage("One of you is already in a challenge, get out and try again");
                return;
            }
        }
        //check for edge cases
        if (originalSender.isSleeping() || originalSender.isInsideVehicle() || acceptor.isInsideVehicle() || acceptor.isSleeping()){
            acceptor.sendMessage("You or your opponent is in a horse, bed, or minecart. Get out and try again");
            return;
        }

        /*
            On duel accept save player state of both players
         */
        if (originalSender.isOnline() && acceptor.isOnline()){
            //now save both players world and coords
            MCUPlayer MCUacceptor = new MCUPlayer();
            //MCUacceptor.expLevel = acceptor.getLevel();
            MCUacceptor.xCoords = acceptor.getX();
            MCUacceptor.yCoords = acceptor.getY();
            MCUacceptor.zCoords = acceptor.getZ();
            MCUacceptor.MCUWorld = acceptor.getWorld();
            this.fightingPlayerInfo.put(acceptor.getName(), MCUacceptor);

            MCUPlayer MCUsender = new MCUPlayer();
            //MCUsender.expLevel = originalSender.getLevel();
            MCUsender.xCoords = originalSender.getX();
            MCUsender.yCoords = originalSender.getY();
            MCUsender.zCoords = originalSender.getZ();
            MCUsender.MCUWorld = originalSender.getWorld();
            this.fightingPlayerInfo.put(originalSender.getName(), MCUsender);

            this.opponents.put(originalSender.getName(), acceptor.getName());
            this.opponents.put(acceptor.getName(), originalSender.getName());
            this.isFighting.put(originalSender.getName(), true);
            this.isFighting.put(acceptor.getName(), true);
            this.challengeRequests.remove(originalSender); //remove the duel request so that they cant /duel accept twice
            startDuel(originalSender, acceptor);
        }else {
            acceptor.sendMessage("No duel requests found or player is offline"); //this happens if they type /duel accept with no invitations
        }

    }

    public void acceptChallengeRequest(Player acceptor){
        Player originalSender = this.challengeRequests.get(acceptor).get(0);

        /*
            Edge cases below
         */
        //What if one of the players goes offline
        if (originalSender == null && acceptor == null){
            return;
        }else if(originalSender == null){
            acceptor.sendMessage("One of the player objects is null, did the player who sent you an invite go offline?");
            return;
        }else if (acceptor ==null){
            originalSender.sendMessage("One of the player objects is null, did the player who sent you an invite go offline?");
            return;
        }
        //what if they challenge while in a challenge
        if (!(this.getFighting(acceptor) == null || this.getFighting(originalSender)==null)){
            if (this.getFighting(acceptor).equals(true) || this.getFighting(originalSender).equals(true)){
                acceptor.sendMessage("One of you is already in a challenge, get out and try again");
                return;
            }
        }
        //check for edge cases
        if (originalSender.isSleeping() || originalSender.isInsideVehicle() || acceptor.isInsideVehicle() || acceptor.isSleeping()){
            acceptor.sendMessage("You or your opponent is in a horse, bed, or minecart. Get out and try again");
            return;
        }

        /*
            On duel accept save player state of both players
         */
        if (originalSender.isOnline() && acceptor.isOnline()){
            //now save both players world and coords
            MCUPlayer MCUacceptor = new MCUPlayer();
            //MCUacceptor.expLevel = acceptor.getLevel();
            MCUacceptor.xCoords = acceptor.getX();
            MCUacceptor.yCoords = acceptor.getY();
            MCUacceptor.zCoords = acceptor.getZ();
            MCUacceptor.MCUWorld = acceptor.getWorld();
            this.fightingPlayerInfo.put(acceptor.getName(), MCUacceptor);

            MCUPlayer MCUsender = new MCUPlayer();
            //MCUsender.expLevel = originalSender.getLevel();
            MCUsender.xCoords = originalSender.getX();
            MCUsender.yCoords = originalSender.getY();
            MCUsender.zCoords = originalSender.getZ();
            MCUsender.MCUWorld = originalSender.getWorld();
            this.fightingPlayerInfo.put(originalSender.getName(), MCUsender);

            this.opponents.put(originalSender.getName(), acceptor.getName());
            this.opponents.put(acceptor.getName(), originalSender.getName());
            this.isFighting.put(originalSender.getName(), true);
            this.isFighting.put(acceptor.getName(), true);
            this.challengeRequests.remove(originalSender); //remove the duel request so that they cant /duel accept twice
            startDuel(originalSender, acceptor);
        }else {
            acceptor.sendMessage("No duel requests found or player is offline"); //this happens if they type /duel accept with no invitations
        }
    }

    public void startDuel(Player originalSender, Player acceptor){
        /*
            Teleports the players to the dueling location
         */
        Location spawn1 = new Location(Bukkit.getWorld("world"), 0, 63, 0);//TELEPORT PLAYERS SOMEWHERE
        Location spawn2 = new Location(Bukkit.getWorld("world"), 5, 63, 0);
        spawn2.setYaw(180.0f);
        originalSender.teleport(spawn1); //teleports them to spawn
        acceptor.teleport(spawn2);
    }

    //end duel restores player locations and updates state
    public void endDuel(Player player){
        if (this.getFighting(player)) {
            //teleport them back to where they were
            Location lastLocation = new Location(this.fightingPlayerInfo.get(player.getName()).MCUWorld, this.fightingPlayerInfo.get(player.getName()).xCoords, this.fightingPlayerInfo.get(player.getName()).yCoords, this.fightingPlayerInfo.get(player.getName()).zCoords );
            player.teleport(lastLocation);

            //give them xp
            //int xpLevel = this.fightingPlayerInfo.get(player.getName()).expLevel;
            //player.setExp(xpLevel);

            //updating state that duel is over
            this.isFighting.put(player.getName(), false);
            this.opponents.remove(player.getName()); //is there an argument for making this a sttring
        }
    }

    public Boolean getFighting(Player object){
        return this.isFighting.get(object.getName());
    }

    public String getOpponent(Player object){
        return this.opponents.get(object.getName());
    }
}
