package Looter;

import com.runemate.game.api.hybrid.input.Mouse;
import com.runemate.game.api.hybrid.local.Camera;
import com.runemate.game.api.hybrid.local.Worlds;
import com.runemate.game.api.hybrid.local.hud.interfaces.Bank;
import com.runemate.game.api.hybrid.local.hud.interfaces.Interfaces;
import com.runemate.game.api.hybrid.local.hud.interfaces.WorldHop;
import com.runemate.game.api.hybrid.region.GameObjects;
import com.runemate.game.api.hybrid.util.Time;
import com.runemate.game.api.hybrid.util.calculations.Random;
import com.runemate.game.api.script.framework.LoopingBot;
import com.runemate.game.api.hybrid.location.Area;
import com.runemate.game.api.hybrid.location.Coordinate;

import com.runemate.game.api.hybrid.location.navigation.Path;
import com.runemate.game.api.hybrid.location.navigation.Traversal;
import com.runemate.game.api.hybrid.entities.Player;
import com.runemate.game.api.hybrid.region.Players;
import com.runemate.game.api.hybrid.queries.GroundItemQueryBuilder;
import com.runemate.game.api.hybrid.queries.results.LocatableEntityQueryResults;
import com.runemate.game.api.hybrid.region.GroundItems;
import com.runemate.game.api.hybrid.entities.GroundItem;
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory;

import com.runemate.game.api.rs3.local.hud.interfaces.LootInventory;
import com.runemate.game.api.script.Execution;
import com.runemate.game.api.osrs.local.hud.interfaces.ControlPanelTab;

import com.runemate.game.api.hybrid.GameEvents;


public class Main extends LoopingBot {
    private final Area.Circular grandExchange = new Area.Circular(new Coordinate(3164, 3489, 0), 13);
    private final Area.Rectangular rougesDen = new Area.Rectangular(new Coordinate(3040, 4978, 1), new Coordinate(3051, 4961, 1));

    private final int[] burntItems = {323, 324, 343, 344, 357, 358, 367, 368, 369, 370, 375, 376, 381, 382, 387, 388, 393, 394, 399, 400, 528, 529, 1867, 1868, 1903, 1904, 2005, 2006, 2013, 2014, 2144, 2145, 2146, 2147, 2175, 2176, 2199, 2200, 2247, 2248, 2305, 2306, 2311, 2312, 2329, 2330, 2345, 2346, 2426, 2427, 3127, 3148, 3149, 3375, 3376, 3383, 5002, 5006, 5990, 5991, 6301, 6302, 6699, 6700, 7090, 7091, 7092, 7093, 7094, 7095, 7222, 7226, 7227, 7231, 7520, 7531, 7570, 7571, 7948, 7949, 7954, 7955, 7961, 7962, 7963, 7964, 7965, 9982, 9983, 9990, 9991, 10140, 10141, 13437, 13438, 14541, 14544, 15268, 15269, 15274, 15275, 18179, 18181, 18183, 18185, 18187, 18189, 18191, 18193, 18195, 18197, 21522, 28650, 28651, 28652, 28653, 28654, 28658, 30077};
    private final int preferredWorld = 301;


    @Override
    public void onStart(String... args) {
        Worlds.setPreferred(preferredWorld);
        CustomPlayerSense.initializeKeys();
        System.out.println("LOOTER [FEATURING PLAYERSENSE]");

        setLoopDelay(100, 120);
        Mouse.setSpeedMultiplier(2.5);

    }

    @Override
    public void onLoop() {


        randomActions();

        if (correctWorld()) {
            Player me = Players.getLocal();
            if (!grandExchange.contains(me)) {
                System.out.println("NOT IN THE G.E. TIME TO RUN TO THE GE.");
                Path path = Traversal.getDefaultWeb().getPathBuilder().buildTo(grandExchange.getRandomCoordinate());
                if (path != null) {
                    path.step();
                }
            } else { //in the grand exchange
                if (com.runemate.game.api.hybrid.util.calculations.Random.nextInt(1, 129) == 27) { //low odds of randomwalk
                    System.out.println("Random Walk");
                    Path path = Traversal.getDefaultWeb().getPathBuilder().buildTo(grandExchange.getRandomCoordinate());
                    if (path != null) {
                        path.step();
                    }
                }

                if (Inventory.isFull()) {
                    if (bank()) {
                        System.out.println("Items banked!");
                    }
                } else { //if invent is not full
                    if (Bank.isOpen()) {
                        Bank.close();
                    }

                    LocatableEntityQueryResults<GroundItem> lootResults = getLoot().results();
                    GroundItem targetItem = lootResults.sortByDistance().limit(2).random();

                    if (targetItem != null) {
                        if (Methods.isWorthLooting(targetItem) || contains(burntItems, targetItem)) {
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
        if (grandExchange != null) {
            lootQuery = GroundItems.newQuery().within(grandExchange).reachable();
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
        for (int i : array) {
            if (i == gi.getId()) {
                result = true;
                System.out.println("Burnt item found! LIT");
                break;
            }
        }
        return result;
    }


    public boolean correctWorld() {
        if (Worlds.getCurrent() != preferredWorld) {
            WorldHop.hopTo(preferredWorld);
        } else {
            return true;
        }
        return false;
    }

    public boolean randomActions() {
        if (Random.nextInt(0, 200) == 27) {
            Camera.turnTo(Camera.getYaw() + Random.nextInt(-30, 30));
            int yaw = Camera.getYaw();
            double pitch = Camera.getPitch();

            Camera.turnTo(Math.abs(yaw+Random.nextInt(50,100)), pitch);
            Execution.delay(1000, 2000);

        } else if (Random.nextInt(0, 200) == 27) {
            Mouse.click(GameObjects.getLoaded().random(), Mouse.Button.RIGHT);

            Execution.delay(1111, 2222);

        } else if (Random.nextInt(0, 200) == 27) {
            Mouse.scroll(true, Random.nextInt(100, 400));
        } else if (Random.nextInt(0, 200) == 27) {
            ControlPanelTab.SKILLS.open();
            Execution.delay(200,600);
            try {
                Interfaces.getAt(320, getRandomWidget()).hover();
            } catch (java.lang.NullPointerException npe) {
                System.out.println("Caught Exception:");
                npe.printStackTrace();
            }
            Execution.delay(1111,2222);
            ControlPanelTab.INVENTORY.open();
            Execution.delay(200,600);
        }
        return true;

    }
    private int getRandomWidget() {
        final int[] widgetsToChose = { 6, 8 , 12, 14 };
        return widgetsToChose[Random.nextInt(0, widgetsToChose.length)];
    }

}