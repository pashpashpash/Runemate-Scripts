package Looter;

import com.runemate.game.api.hybrid.Environment;
import com.runemate.game.api.hybrid.RuneScape;
import com.runemate.game.api.hybrid.entities.GroundItem;
import com.runemate.game.api.hybrid.entities.Item;
import com.runemate.game.api.hybrid.entities.Player;
import com.runemate.game.api.hybrid.entities.definitions.ItemDefinition;
import com.runemate.game.api.hybrid.local.hud.interfaces.*;
import com.runemate.game.api.hybrid.region.Npcs;
import com.runemate.game.api.hybrid.region.Players;

import com.runemate.game.api.hybrid.util.calculations.Random;
import com.runemate.game.api.hybrid.net.GrandExchange;
import com.runemate.game.api.script.Execution;
import com.runemate.game.api.script.framework.AbstractScript;

import java.io.IOException;
import java.io.PrintWriter;


import java.util.*;

public final class Methods {

    public static HashMap<String, Integer> itemPrices = new HashMap<>();
    public static int total = 0;
    public static boolean arrayIsValid(String[] array) {
        return array != null && array.length > 0;
    }

    public static Boolean isInCombat() {
        Player player = Players.getLocal();
        return RuneScape.isLoggedIn() && player != null && (player.getTarget() != null
                || !Npcs.newQuery().actions("Attack").targeting(player).reachable().results().isEmpty());
    }

    public static void logout() {
        if (!isInCombat()) {
            if (RuneScape.isLoggedIn()) {
                if (RuneScape.logout()) {
                    Execution.delayUntil(() -> !RuneScape.isLoggedIn(), 3000);
                }
            }
        }
        AbstractScript script = Environment.getScript();
        if (script != null) {
            script.stop();
        }

    }



    public static int changeHealthValue(int setValue) {
        int maxHealth = Health.getMaximum();
        float increase = (float)maxHealth/100*Random.nextInt(0, 10);
        int eatValue = setValue +  Math.round(increase);
        return eatValue > maxHealth ? setValue : eatValue;
    }

    public static void out(String s) {
        System.out.println("Looter - " + s);
    }

    public static Boolean hasRoomForItem(Item item) {
        if (item != null) {
            ItemDefinition itemDefinition = item.getDefinition();
            if (itemDefinition != null) {
                int itemId = itemDefinition.getId();
                int notedId = itemDefinition.getNotedId();
                return !Inventory.isFull() || (itemId == notedId && Inventory.contains(notedId)) || (notedId == -1 && Inventory.contains(itemId));
            }
        }
        out("Loot: We don't have room for that item");
        return false;
    }

    public static boolean itemIsNoted(Item item) {
        if (item != null) {
            ItemDefinition itemDefinition = item.getDefinition();
            if (itemDefinition != null) {
                int itemId = itemDefinition.getId();
                int notedId = itemDefinition.getNotedId();
                return itemId == notedId  || notedId == -1;
            }
        }
        return false;
    }


    public static Boolean isWorthLooting(GroundItem gItem) {

        int itemValue = 0;
        String itemName = gItem.getDefinition().getName();
        int itemId = gItem.getId();
        if (itemName.equals("Coins")) {
            System.out.println("Loot Value: We have found coins, getting quantity..");
            itemValue = gItem.getQuantity();
            System.out.println("Loot Value: Coin worth = " + itemValue);
        } else {
            if (itemPrices.containsKey(itemName)) {
                itemValue = itemPrices.get(itemName);
            } else if (GrandExchange.lookup(itemId) != null) {
                if(GrandExchange.lookup(itemId) != null) {
                    try {
                        itemValue = GrandExchange.lookup(itemId).getPrice();
                        itemPrices.put(itemName, itemValue);
                        System.out.println(gItem.toString() + " | Value : " + itemValue);
                        String text = process(itemPrices);
                        try (PrintWriter out = new PrintWriter("itemsDropped.txt")) {
                            out.println(total += itemValue * gItem.getQuantity());
                            out.println(text + System.lineSeparator());
                        } catch (IOException ioe) {
                            System.out.println("ERROR");
                        }
                    } catch (NullPointerException npe) {
                        npe.printStackTrace();
                    }
                }
            }
        }
        return itemValue >= 100;
    }

    public static String process(HashMap mp) {
        HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
        HashMap<String, Integer> sortedMap = new HashMap<String, Integer>();
        tempMap = mp;
        sortedMap = sortByValue(tempMap);

        Iterator it = sortedMap.entrySet().iterator();
        String newString = "";
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry)it.next();
            newString += (pair.getKey() + " | " + pair.getValue() + System.lineSeparator());
        }
        return newString;
    }

    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Integer> > list =
                new LinkedList<Map.Entry<String, Integer> >(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2)
            {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

}