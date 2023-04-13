package GamesAndActivities;

import static com.segway.robot.EmojiVoiceSample.MainActivity.*;
import android.os.Message;
import com.segway.robot.EmojiVoiceSample.BaseControlManager;
import com.segway.robot.sdk.emoji.configure.BehaviorList;
import com.segway.robot.sdk.voice.VoiceException;

import java.util.Random;

public class SimonSays {
    public static void SimonSays() {
        Random rand = new Random();
        int numberOfPlays = 10; // Change this to the desired number of plays
        BaseControlManager.baseOriginReset();

        for (int i = 0; i < numberOfPlays; i++) {
            int n = rand.nextInt(9) + 1;
            boolean simonSays = rand.nextDouble() < 0.50; // 75% chance
            String command = "";

            if (n == 1) {
                BaseControlManager.moveForward();
                command = "Move Forward";
            } else if (n == 2) {
                BaseControlManager.moveBackward();
                command = "Move Backward";
            } else if (n == 3) {
                BaseControlManager.moveLeft();
                command = "Move Left";
            } else if (n == 4) {
                BaseControlManager.moveRight();
                command = "Move Right";
            } else if (n == 5) {
                Message msg = mHandler.obtainMessage(ACTION_BEHAVE, BehaviorList.LOOK_RIGHT);
                mHandler.sendMessage(msg);
                command = "Look Right";
            } else if (n == 6) {
                Message msg = mHandler.obtainMessage(ACTION_BEHAVE, BehaviorList.LOOK_LEFT);
                mHandler.sendMessage(msg);
                command = "Look Left";
            } else if (n == 7) {
                Message msg = mHandler.obtainMessage(ACTION_BEHAVE, BehaviorList.LOOK_UP);
                mHandler.sendMessage(msg);
                command = "Look Up";
            } else if (n == 8) {
                Message msg = mHandler.obtainMessage(ACTION_BEHAVE, BehaviorList.LOOK_DOWN);
                mHandler.sendMessage(msg);
                command = "Look Down";
            } else if (n == 9) {
                Message msg = mHandler.obtainMessage(ACTION_BEHAVE, BehaviorList.TURN_AROUND);
                mHandler.sendMessage(msg);
                BaseControlManager.turnAround();
                command = "Turn Around";
            }

            if (simonSays) {
                command = "Simon Says " + command;
            }

            try {
                mSpeaker.speak(command, mTtsListener);
            } catch (VoiceException e) {
                e.printStackTrace();
            }

            BaseControlManager.goBackToStart();

            // You can add a delay between actions if you want
            try {
                Thread.sleep(10000); // Sleep for 10 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        BaseControlManager.goBackToStart();

    }

}
