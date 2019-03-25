package PlayerSenseTemplate;

import com.runemate.game.api.script.framework.LoopingBot;

public class Main extends LoopingBot {

    @Override
    public void onStart(String... args) {
        System.out.println("PLAYER SENSE TEMPLATE");
        CustomPlayerSense.initializeKeys();
    }
    @Override
    public void onLoop() {
        System.out.println("Loop");
    }
}




