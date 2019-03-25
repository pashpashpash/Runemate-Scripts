package Looter;

import com.runemate.game.api.hybrid.entities.details.Interactable;
import com.runemate.game.api.hybrid.entities.details.Locatable;
import com.runemate.game.api.hybrid.local.Camera;
import com.runemate.game.api.hybrid.location.navigation.Path;
import com.runemate.game.api.hybrid.location.navigation.Traversal;
import com.runemate.game.api.hybrid.location.navigation.basic.BresenhamPath;
import com.runemate.game.api.hybrid.location.navigation.cognizant.RegionPath;
import com.runemate.game.api.hybrid.util.calculations.Distance;
import com.runemate.game.api.hybrid.util.calculations.Random;
import com.runemate.game.api.script.Execution;

import java.util.concurrent.Future;

public class Movement {

    public static void moveToInteractable(Interactable i) {
        if (i != null && i instanceof Locatable) {
            Locatable l = (Locatable) i;
            if (Distance.to(l) > Random.nextInt(15, 16)) {
                pathToLocatable(l);
            } else {
                Future<Boolean> cameraMovement = Camera.concurrentlyTurnTo(l);
                Execution.delayUntil(cameraMovement::get, 1, 10);
                if (!i.isVisible()) {
                    pathToLocatable(l);
                }
            }
        }
    }

    public static void pathToLocatable(Locatable l) {
        if (l != null) {
            Path toLocatable = RegionPath.buildTo(l);
            if (toLocatable == null) toLocatable = Traversal.getDefaultWeb().getPathBuilder().buildTo(l);
            if (toLocatable == null) toLocatable = BresenhamPath.buildTo(l);
            if (toLocatable != null) {
                toLocatable.step(true);
            }
        }
    }

}