package HCIMLooter;

import Looter.Methods;
import Looter.Movement;
import com.runemate.game.api.hybrid.entities.GroundItem;
import com.runemate.game.api.hybrid.local.hud.interfaces.Bank;
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory;
import com.runemate.game.api.hybrid.local.hud.interfaces.WorldHop;
import com.runemate.game.api.hybrid.queries.GroundItemQueryBuilder;
import com.runemate.game.api.hybrid.queries.results.LocatableEntityQueryResults;
import com.runemate.game.api.hybrid.region.GroundItems;
import com.runemate.game.api.rs3.local.hud.interfaces.LootInventory;
import com.runemate.game.api.script.Execution;
import com.runemate.game.api.script.framework.LoopingBot;
import com.runemate.game.api.hybrid.entities.Player;
import com.runemate.game.api.hybrid.local.Worlds;
import com.runemate.game.api.hybrid.location.Area;
import com.runemate.game.api.hybrid.location.Coordinate;
import com.runemate.game.api.hybrid.location.navigation.Path;
import com.runemate.game.api.hybrid.location.navigation.Traversal;
import com.runemate.game.api.hybrid.region.Players;

import java.util.Vector;

public class Main extends LoopingBot {
    private final Area.Rectangular rougesDen = new Area.Rectangular(new Coordinate(3040,4978,1), new Coordinate(3051, 4961, 1));
    private final Area.Rectangular barbFishing = new Area.Rectangular(new Coordinate(3100,3435,0), new Coordinate(3109, 3423, 0));
    private final Area.Rectangular bankArea = new Area.Rectangular(new Coordinate(3180,3447,0), new Coordinate(3190, 3433, 0));

    private final int[] burntItems = {323,324,343,344,357,358,367,368,369,370,375,376,381,382,387,388,393,394,399,400,528,529,1867,1868,1903,1904,2005,2006,2013,2014,2144,2145,2146,2147,2175,2176,2199,2200,2247,2248,2305,2306,2311,2312,2329,2330,2345,2346,2426,2427,3127,3148,3149,3375,3376,3383,5002,5006,5990,5991,6301,6302,6699,6700,7090,7091,7092,7093,7094,7095,7222,7226,7227,7231,7520,7531,7570,7571,7948,7949,7954,7955,7961,7962,7963,7964,7965,9982,9983,9990,9991,10140,10141,13437,13438,14541,14544,15268,15269,15274,15275,18179,18181,18183,18185,18187,18189,18191,18193,18195,18197,21522,28650,28651,28652,28653,28654,28658,30077};

    private Vector<Integer> goodWorlds = new Vector<>();

    @Override
    public void onStart(String... args) {
        System.out.println("PLAYER SENSE TEMPLATE");
        CustomPlayerSense.initializeKeys();
        Worlds.setPreferred(302);
    }
    @Override
    public void onLoop() {

        System.out.println("Loop");
        Player me = Players.getLocal();

        if(Inventory.isFull()) {
            if(!bankArea.contains(me)) {
                System.out.println("Not in the bank area. Time to travel there.");
                Path path = Traversal.getDefaultWeb().getPathBuilder().buildTo(bankArea.getRandomCoordinate());
                if(path != null) {
                    path.step();
                }
            } else { //player in bank
                bank();
            }
        } else {
            if(Bank.isOpen()) { //just in case
                Bank.close();
            }

            if (!rougesDen.contains(me)) {
                System.out.println("Not in the rouge's den. Time to travel there.");
                Path path = Traversal.getDefaultWeb().getPathBuilder().buildTo(rougesDen.getRandomCoordinate());
                if(path != null) {
                    path.step();
                }
            } else { //in the rouge's den
                System.out.println("Chillin' with the HCIMs");

                LocatableEntityQueryResults<GroundItem> lootResults = getLoot().results();
                if(lootResults.size() == 0) { //we should hop? (not sure hopping even makes sense though)
//                    if(com.runemate.game.api.hybrid.util.calculations.Random.nextInt(1,129) == 27) { //low odds
//                        WorldHop.hopToRandom(false); //true = members only
//                    }
                } else {
                    if(lootResults.size() > 10) {
                        System.out.println("This is a good world = " + Worlds.getCurrent());
                        if(!goodWorlds.contains(Worlds.getCurrent())) {
                            goodWorlds.add(Worlds.getCurrent());
                        }
                    }
                    GroundItem targetItem = lootResults.sortByDistance().limit(2).random();

                    if (targetItem != null) {
                        if(Methods.isWorthLooting(targetItem) || contains(burntItems, targetItem)) {
                            System.out.println("Looter – Found a worthwhile item! " + targetItem.toString());
                            takeGroundItem(targetItem);
                        }
                    }
                }
            }
        }


    }
    private boolean bank() {
        if (Bank.open()) {
            if (Bank.depositInventory()) {
                if (Bank.close()) {
                    return true;
                }
            }
        }
        return false;
    }


    public GroundItemQueryBuilder getLoot() {
        Inventory.newQuery().filter(a -> a.getDefinition().getName().contains("memory"));
        GroundItemQueryBuilder lootQuery = GroundItems.newQuery();
        if (rougesDen != null) {
            lootQuery = GroundItems.newQuery().reachable();
        }
        return lootQuery;
    }

    private void takeGroundItem(GroundItem item) {
        int invCount = Inventory.getQuantity();
        String itemName = item.getDefinition().getName();
        if (item.isVisible()) {
            if (item.interact("Take", itemName)) {
                System.out.println("Loot: Successful");
                Execution.delayUntil(() -> Inventory.getQuantity() > invCount || LootInventory.isOpen(), 10, 30);
                return;
            }
        } else {
            System.out.println("Loot: We need to move to the item");
            Movement.moveToInteractable(item);
        }
        System.out.println("Loot: Unsuccessful");
    }


    public static boolean contains(final int[] array, final GroundItem gi) {
        boolean result = false;
        for(int i : array){
            if(i == gi.getId()){
                result = true;
                System.out.println("Burnt item found! LIT");
                break;
            }
        }
        return result;
    }
}
