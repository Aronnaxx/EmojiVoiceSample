package StoryTeller;

import android.os.Message;

import com.segway.robot.EmojiVoiceSample.EmojiVoiceSampleActivity;
import com.segway.robot.sdk.emoji.configure.BehaviorList;
import com.segway.robot.sdk.voice.VoiceException;

import java.io.IOException;

//public class SpaceStory extends EmojiVoiceSampleActivity {
//
//    String fileName = "Story.txt";
//                   try {
//        String story = StoryReader.readFileFromAssets(EmojiVoiceSampleActivity.this, fileName);
//        Message msg = mHandler.obtainMessage(ACTION_BEHAVE, BehaviorList.APPLE_WOW_EMOTION);
//        mHandler.sendMessage(msg);
//        mSpeaker.speak(story, mTtsListener);
//    } catch (
//    IOException e) {
//        e.printStackTrace();
//    } catch (
//    VoiceException e) {
//        e.printStackTrace();
//    }
//}
