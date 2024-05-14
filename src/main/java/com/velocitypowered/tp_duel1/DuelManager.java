package com.velocitypowered.tp_duel1;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

//the jar file is in build -> libs
public class DuelManager {
    //test
    //duel xp gives less
    //if two ppl die at the same time, the player that doenst respawn first gets stuff while dead and is glitched but leaves and rejoins
    //dueling players shouldnt break blocks or pick up items

    //TO do
    //targeted duel requests
    //fix up starting positions /duel set
    //betting feature
    //make it so they can press the text button to accept or reject duel requests
    //add different types of duels, like a bow duel tnt and bow and trident duel
    //exploit u can /duel with urself
    //if the player is offline and dueling when the server restarts, they lose their stuff but thats out of my hands kinda

    public final Map<Player, Player> duelRequests; //could make this a list if they specify it searches through at most 10 requests before it expires

    private final Map<String, Boolean> isDueling;

    public final Map<String, MCUPlayer> duelingPlayerInfo;
    private final Map<String, String> opponents;

    //this creates the object that we use
    public DuelManager() {
        this.duelRequests = new HashMap<>();
        this.isDueling = new HashMap<>();
        this.opponents = new HashMap<>();
        this.duelingPlayerInfo = new HashMap<>();
    }

    public void sendDuelRequest(Player sender, Player target) {
        this.duelRequests.put(target, sender);
        target.sendMessage(sender.getName() + " has challenged you to a duel. Type /duel accept to accept it.");
    }


    public void acceptDuelRequest(Player acceptor, ChallengeManager challengeManager){
        Player originalSender = this.duelRequests.get(acceptor);

        //makes sure theyre online and not null
        if (originalSender == null && acceptor == null){
            return;
        }else if(originalSender == null){
            acceptor.sendMessage("One of the player objects is null, did the player who sent you an invite go offline?");
            return;
        }else if (acceptor ==null){
            originalSender.sendMessage("One of the player objects is null, did the player who sent you an invite go offline?");
            return;
        }

        //makes sure not already dueling
        if(this.getDueling(acceptor).equals(true) || this.getDueling(originalSender).equals(true)){
            return;
        }

        if(challengeManager.getFighting(acceptor).equals(true) || challengeManager.getFighting(originalSender).equals(true)){
            acceptor.sendMessage("One of you is in a challenge, get out and try again");
            return;
        }


        if (originalSender.isOnline() && acceptor.isOnline()){
            //check for edge cases
            if (originalSender.isSleeping() || originalSender.isInsideVehicle() || acceptor.isInsideVehicle() || acceptor.isSleeping()){
                acceptor.sendMessage("You or your opponent is in a horse, bed, or minecart. Get out and try again");
            }
            //now save both players inventories, health, XP, location and world
            savePlayerInfo(originalSender, acceptor);
            clearInventories(originalSender, acceptor);
            giveStuff(originalSender, acceptor);
            this.opponents.put(originalSender.getName(), acceptor.getName());
            this.opponents.put(acceptor.getName(), originalSender.getName());
            this.isDueling.put(originalSender.getName(), true);
            this.isDueling.put(acceptor.getName(), true);
            this.duelRequests.remove(originalSender); //remove the duel request so that they cant /duel accept twice
            startDuel(originalSender, acceptor);
        }else {
            assert acceptor != null;
            acceptor.sendMessage("No duel requests found"); //this happens if they type /duel accept with no invitations
        }
    }

    public void acceptDuelRequestTargeted(Player acceptor, Player originalSender){

    }

    public void savePlayerInfo(Player originalSender, Player acceptor){
            MCUPlayer newMCUguy = new MCUPlayer();
            newMCUguy.actualPlayer = originalSender;
            newMCUguy.HP = originalSender.getHealth();
            newMCUguy.MCUInv = originalSender.getInventory().getContents();
            newMCUguy.xCoords = originalSender.getLocation().getX();
            newMCUguy.yCoords = originalSender.getLocation().getY();
            newMCUguy.zCoords = originalSender.getLocation().getZ();
            newMCUguy.MCUWorld = originalSender.getWorld();
            newMCUguy.expLevel = originalSender.getLevel();
            newMCUguy.armorContents = originalSender.getEquipment().getArmorContents();
            newMCUguy.offhandItem = originalSender.getEquipment().getItemInOffHand();
            duelingPlayerInfo.put(originalSender.getName(), newMCUguy);

            MCUPlayer newMCUacceptor = new MCUPlayer();
            newMCUacceptor.actualPlayer = acceptor;
            newMCUacceptor.HP = acceptor.getHealth();
            newMCUacceptor.MCUInv = acceptor.getInventory().getContents();
            newMCUacceptor.xCoords = acceptor.getLocation().getX();
            newMCUacceptor.yCoords = acceptor.getLocation().getY();
            newMCUacceptor.zCoords = acceptor.getLocation().getZ();
            newMCUacceptor.MCUWorld = acceptor.getWorld();
            newMCUacceptor.expLevel = acceptor.getLevel();
            newMCUacceptor.armorContents = acceptor.getEquipment().getArmorContents();
            newMCUacceptor.offhandItem = acceptor.getEquipment().getItemInOffHand();
            duelingPlayerInfo.put(acceptor.getName(), newMCUacceptor);
    }


    public void clearInventories(Player originalSender, Player acceptor){
        originalSender.getInventory().clear();
        acceptor.getInventory().clear();
        originalSender.updateInventory();
        acceptor.updateInventory();
    }

    public void giveStuff(Player originalSender, Player acceptor){
        //sets the inventory for the original sender
        originalSender.getInventory().addItem(new ItemStack(Material.DIAMOND_AXE));
        originalSender.getInventory().addItem(new ItemStack(Material.BOW));
        originalSender.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
        originalSender.getInventory().addItem(new ItemStack(Material.ARROW, 6));
        originalSender.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
        originalSender.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        originalSender.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        originalSender.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));

        acceptor.getInventory().addItem(new ItemStack(Material.DIAMOND_AXE));
        acceptor.getInventory().addItem(new ItemStack(Material.BOW));
        acceptor.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
        acceptor.getInventory().addItem(new ItemStack(Material.ARROW, 6));
        acceptor.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
        acceptor.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
        acceptor.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
        acceptor.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));

        originalSender.updateInventory();
        acceptor.updateInventory();
    }



    public void restoreInventory(Player player){
        MCUPlayer playerInfo = duelingPlayerInfo.get(player.getName());
        player.setHealth(playerInfo.HP);
        player.getInventory().setContents(playerInfo.MCUInv);
        player.updateInventory();
        Location prevLocation = new Location(playerInfo.MCUWorld, playerInfo.xCoords, playerInfo.yCoords, playerInfo.zCoords);
        player.teleport(prevLocation);
        player.setLevel(playerInfo.expLevel);
    }

    public void startDuel(Player originalSender, Player acceptor){
        Location spawn1 = new Location(Bukkit.getWorld("world"), 0, 63, 0);//TELEPORT PLAYERS SOMEWHERE
        Location spawn2 = new Location(Bukkit.getWorld("world"), 5, 63, 0);
        spawn2.setYaw(180.0f);
        originalSender.teleport(spawn1); //teleports them to spawn
        acceptor.teleport(spawn2);
    }


    //end duel restores player stuff and updates state
    public void endDuel(Player player){
        if (getDueling(player)) { //only restore it if the player is dueling ie this hasnt been called yet
            player.getInventory().clear(); //this clears the inventories when they die
            player.updateInventory();
            restoreInventory(player);
            //updating state that duel is over
            this.isDueling.put(player.getName(), false);
            this.duelingPlayerInfo.remove(player.getName());
            this.opponents.remove(player.getName()); //is there an argument for making this a sttring
        }
    }

    public Boolean getDueling(Player object){
        return this.isDueling.get(object.getName());
    }

    public String getOpponent(Player object){
        return this.opponents.get(object.getName());
    }
}
