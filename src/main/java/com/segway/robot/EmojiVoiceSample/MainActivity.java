package com.segway.robot.EmojiVoiceSample;


import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.emoji.Emoji;
import com.segway.robot.sdk.emoji.EmojiPlayListener;
import com.segway.robot.sdk.emoji.EmojiView;
import com.segway.robot.sdk.emoji.configure.BehaviorList;
import com.segway.robot.sdk.emoji.exception.EmojiException;
import com.segway.robot.sdk.emoji.player.RobotAnimator;
import com.segway.robot.sdk.emoji.player.RobotAnimatorFactory;
import com.segway.robot.sdk.locomotion.sbv.Base;
import com.segway.robot.sdk.voice.Languages;
import com.segway.robot.sdk.voice.Recognizer;
import com.segway.robot.sdk.voice.Speaker;
import com.segway.robot.sdk.voice.VoiceException;
import com.segway.robot.sdk.voice.grammar.GrammarConstraint;
import com.segway.robot.sdk.voice.recognition.RecognitionListener;
import com.segway.robot.sdk.voice.recognition.RecognitionResult;
import com.segway.robot.sdk.voice.recognition.WakeupListener;
import com.segway.robot.sdk.voice.recognition.WakeupResult;
import com.segway.robot.sdk.voice.tts.TtsListener;

import java.io.IOException;
import java.util.Random;

import StoryTeller.StoryReader;


/**
 * @author jacob
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "Loomo";

    private static final int ACTION_SHOW_MSG = 1;
    private static final int ACTION_START_RECOGNITION = 2;
    private static final int ACTION_STOP_RECOGNITION = 3;
    private static final int ACTION_BEHAVE = 4;

    private TextView mTextView;
    private EmojiView mEmojiView;
    private Emoji mEmoji;
    private Recognizer mRecognizer;
    private Speaker mSpeaker;
    private int mSpeakerLanguage;
    private int mRecognitionLanguage;
    private GrammarConstraint mMoveSlotGrammar;
    private boolean mRecognitionReady;
    private boolean mSpeakerReady;
    private HeadControlManager mHandcontrolManager;

    MediaPlayer player = new MediaPlayer();


    private ServiceBinder.BindStateListener mRecognitionBindStateListener;
    private ServiceBinder.BindStateListener mSpeakerBindStateListener;
    private WakeupListener mWakeupListener;
    private RecognitionListener mRecognitionListener;
    private TtsListener mTtsListener;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ACTION_SHOW_MSG:
                    mTextView.setText(msg.obj.toString());
                    break;
                case ACTION_START_RECOGNITION:
                    try {
                        mRecognizer.startWakeupAndRecognition(mWakeupListener, mRecognitionListener);
                    } catch (VoiceException e) {
                        e.printStackTrace();
                    }
                    break;
                case ACTION_STOP_RECOGNITION:
                    try {
                        mRecognizer.stopRecognition();
                    } catch (VoiceException e) {
                        e.printStackTrace();
                    }
                    break;
                case ACTION_BEHAVE:
                    try {
                        mEmoji.startAnimation(RobotAnimatorFactory.getReadyRobotAnimator((Integer) msg.obj), new EmojiPlayListener() {
                            @Override
                            public void onAnimationStart(RobotAnimator animator) {
                            }

                            @Override
                            public void onAnimationEnd(RobotAnimator animator) {
                                mEmojiView.setClickable(true);
                                mHandcontrolManager.setWorldPitch(0.6f);
                            }

                            @Override
                            public void onAnimationCancel(RobotAnimator animator) {
                                mEmojiView.setClickable(true);
                                mHandcontrolManager.setWorldPitch(0.6f);
                            }
                        });
                    } catch (EmojiException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEmojiView = (EmojiView) findViewById(R.id.face);
        mEmojiView.setOnClickListener(this);
        mTextView = (TextView) findViewById(R.id.textView);

        initEmoji();
        initListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindServices();
    }

    @Override
    public void onClick(final View v) {
        v.setClickable(false);
        int behavior;
        int randomSeed = (int) (Math.random() * 4);
        switch (randomSeed) {
            case 0:
                behavior = BehaviorList.LOOK_LEFT;
                break;
            case 1:
                behavior = BehaviorList.LOOK_RIGHT;
                break;
            case 2:
                behavior = BehaviorList.LOOK_AROUND;
                break;
            case 3:
                behavior = BehaviorList.LOOK_CURIOUS;
                break;
            default:
                behavior = BehaviorList.LOOK_AROUND;
                break;
        }
        Message msg = mHandler.obtainMessage(ACTION_BEHAVE, behavior);
        mHandler.sendMessage(msg);
    }

    private void initListeners() {

        mRecognitionBindStateListener = new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "Recognition onBind");
                try {
                    //get recognition language when service bind and init Constrained grammar.
                    mRecognitionLanguage = mRecognizer.getLanguage();
                    if (mRecognitionLanguage == Languages.EN_US) {
                        mMoveSlotGrammar = ControlGrammar.createMoveSlotGrammar();
                        mRecognizer.addGrammarConstraint(mMoveSlotGrammar);
                        // if both ready, start recognition
                        mRecognitionReady = true;
                        if (mSpeakerReady && mRecognitionReady) {
                            Message msg = mHandler.obtainMessage(ACTION_START_RECOGNITION);
                            mHandler.sendMessage(msg);
                        }
                    } else {
                        mEmojiView.setClickable(false);
                        Message msg = mHandler.obtainMessage(ACTION_SHOW_MSG, "Only US English is supported");
                        mHandler.sendMessage(msg);
                    }

                } catch (VoiceException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onUnbind(String s) {
                // make toast to indicate unbind success.
                Message msg = mHandler.obtainMessage(ACTION_SHOW_MSG, "Recognition service disconnected");
                mHandler.sendMessage(msg);

                // Stop recognition
                mRecognitionReady = false;
                msg = mHandler.obtainMessage(ACTION_STOP_RECOGNITION);
                mHandler.sendMessage(msg);
            }
        };

        mSpeakerBindStateListener = new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "Speaker onBind");
                try {

                    mSpeakerLanguage = mSpeaker.getLanguage();
                    if (mSpeakerLanguage == Languages.EN_US) {
                        try {
                            mSpeaker.speak("Hello, my name is Loomo. Welcome to Terran Orbital's Bring Your Kids to Work Day.", mTtsListener);
                            //mSpeaker.speak("Hi.", mTtsListener);

                        } catch (VoiceException e) {
                            e.printStackTrace();
                        }

                        // if both ready, start recognition
                        mSpeakerReady = true;
                        if (mSpeakerReady && mRecognitionReady) {
                            Message msg = mHandler.obtainMessage(ACTION_START_RECOGNITION);
                            mHandler.sendMessage(msg);
                        }
                    }else {
                        mEmojiView.setClickable(false);
                        Message msg = mHandler.obtainMessage(ACTION_SHOW_MSG, "Only US English is supported");
                        mHandler.sendMessage(msg);
                    }
                } catch (VoiceException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onUnbind(String s) {
                // make toast to indicate unbind success.
                Message msg = mHandler.obtainMessage(ACTION_SHOW_MSG, "Speaker service unbind success");
                mHandler.sendMessage(msg);

                // stop recognition
                mSpeakerReady = false;
                msg = mHandler.obtainMessage(ACTION_STOP_RECOGNITION);
                mHandler.sendMessage(msg);
            }
        };

        mWakeupListener = new WakeupListener() {
            @Override
            public void onStandby() {
                Log.d(TAG, "onStandby");
            }

            @Override
            public void onWakeupResult(WakeupResult wakeupResult) {
                Log.d(TAG, "wakeup word:" + wakeupResult.getResult() + ", angle: " + wakeupResult.getAngle());
            }

            @Override
            public void onWakeupError(String s) {
                Log.d(TAG, "onWakeupError:" + s);
                Message msg = mHandler.obtainMessage(ACTION_SHOW_MSG, "wakeup error:" + s);
                mHandler.sendMessage(msg);
            }
        };

        mRecognitionListener = new RecognitionListener() {
            @Override
            public void onRecognitionStart() {
                Log.d(TAG, "onRecognitionStart");
            }

            @Override
            public boolean onRecognitionResult(RecognitionResult recognitionResult) {
                //show the recognition result and recognition result confidence.
                String result = recognitionResult.getRecognitionResult();
                BaseControlManager.baseOriginReset();
                Log.e(TAG, "recognition result: " + result + ", confidence:" + recognitionResult.getConfidence());
                Message resultMsg = mHandler.obtainMessage(ACTION_SHOW_MSG, "recognition result: " + result + ", confidence:" + recognitionResult.getConfidence());
                mHandler.sendMessage(resultMsg);

               if (result.contains("look") && result.contains("left")) {
                    Message msg = mHandler.obtainMessage(ACTION_BEHAVE, BehaviorList.LOOK_LEFT);
                    mHandler.sendMessage(msg);
                } else if (result.contains("look") && result.contains("right")) {
                    Message msg = mHandler.obtainMessage(ACTION_BEHAVE, BehaviorList.LOOK_RIGHT);
                    mHandler.sendMessage(msg);
                } else if (result.contains("look") && result.contains("up")) {
                    Message msg = mHandler.obtainMessage(ACTION_BEHAVE, BehaviorList.LOOK_UP);
                    mHandler.sendMessage(msg);
                } else if (result.contains("look") && result.contains("down")) {
                    Message msg = mHandler.obtainMessage(ACTION_BEHAVE, BehaviorList.LOOK_DOWN);
                    mHandler.sendMessage(msg);
                } else if (result.contains("turn") && result.contains("left")) {
                   BaseControlManager.rotateLeft();
                    Message msg = mHandler.obtainMessage(ACTION_BEHAVE, BehaviorList.TURN_LEFT);
                    mHandler.sendMessage(msg);
                } else if (result.contains("turn") && result.contains("right")) {
                    BaseControlManager.rotateRight();
                    Message msg = mHandler.obtainMessage(ACTION_BEHAVE, BehaviorList.TURN_RIGHT);
                    mHandler.sendMessage(msg);
                } else if (result.contains("turn") && result.contains("around")) {
                    BaseControlManager.turnAround();
                    Message msg = mHandler.obtainMessage(ACTION_BEHAVE, BehaviorList.TURN_AROUND);
                    mHandler.sendMessage(msg);
                } else if (result.contains("turn") && result.contains("full")) {
                    Message msg = mHandler.obtainMessage(ACTION_BEHAVE, BehaviorList.TURN_FULL);
                    mHandler.sendMessage(msg);
               } else if (result.contains("move") || result.contains("go")) {
                   if (result.contains("forward")) {
                       BaseControlManager.moveForward();
                   } else if (result.contains("backward")) {
                       BaseControlManager.moveBackward();
                   } else if ( result.contains("left")) {
                       BaseControlManager.moveLeft();
                   } else if (result.contains("right")) {
                       BaseControlManager.moveRight();
                   }
                   else if (result.contains("up")) {
                       playVoice();
                       HeadControlManager.rotateUp();
                   }

               }
               else if (result.contains("Play") && result.contains("simon says")) {
                   SimonSays();
               }


               else if (result.contains("Tell me a story about") && result.contains("space")) {
                   String fileName = "Story.txt";
                   try {
                       String story = StoryReader.readFileFromAssets(MainActivity.this, fileName);
                       Message msg = mHandler.obtainMessage(ACTION_BEHAVE, BehaviorList.APPLE_WOW_EMOTION);
                       mHandler.sendMessage(msg);
                       mSpeaker.speak(story, mTtsListener);
                   } catch (IOException e) {
                       e.printStackTrace();
                   } catch (VoiceException e) {
                       e.printStackTrace();
                   }
               }

                return false;
            }

            @Override
            public boolean onRecognitionError(String s) {
                Log.d(TAG, "onRecognitionError: " + s);
                Message errorMsg = mHandler.obtainMessage(ACTION_SHOW_MSG, "recognition error: " + s);
                mHandler.sendMessage(errorMsg);
                return false;
            }
        };

        mTtsListener = new TtsListener() {
            @Override
            public void onSpeechStarted(String s) {
                Log.d(TAG, "onSpeechStarted() called with: s = [" + s + "]");
            }

            @Override
            public void onSpeechFinished(String s) {
                Log.d(TAG, "onSpeechFinished() called with: s = [" + s + "]");
            }

            @Override
            public void onSpeechError(String s, String s1) {
                Log.d(TAG, "onSpeechError() called with: s = [" + s + "], s1 = [" + s1 + "]");
                Message msg = mHandler.obtainMessage(ACTION_SHOW_MSG, "speech error: " + s1);
                mHandler.sendMessage(msg);
            }
        };
    }

    private void bindServices() {
        mRecognizer = Recognizer.getInstance();
        mSpeaker = Speaker.getInstance();
        mRecognizer.bindService(this, mRecognitionBindStateListener);
        mSpeaker.bindService(this, mSpeakerBindStateListener);
    }

    private void unBindServices() {
        mRecognizer.unbindService();
        mSpeaker.unbindService();
    }


    private void initEmoji() {
        mEmoji = Emoji.getInstance();
        mEmoji.init(this);
        mEmoji.setEmojiView((EmojiView) findViewById(R.id.face));
        mHandcontrolManager = new HeadControlManager(this);
        mEmoji.setHeadControlHandler(mHandcontrolManager);
        mEmoji.setBaseControlHandler(new BaseControlManager(this));
    }


    // This function, SimonSays() will be called when the robot hears "Play Simon Says". It will make the robot use a random function
    // to move around the room. It will also make the robot say a random phrase that correlates with the movement, and a .75 chance to say "Simon Says" before each command
    private void SimonSays() {
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

    public void playVoice() {
        try {
            AssetManager assetManager = this.getAssets();
            AssetFileDescriptor descriptor = assetManager.openFd("CountryRoads.m4a");
            player.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            player.prepare();
            player.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void endVoice() {
        if (player.isPlaying()) {
            player.stop();
            player.reset();
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        unBindServices();
        finish();
    }
}
