package com.segway.robot.EmojiVoiceSample;

import android.content.Context;
import android.util.Log;

import com.segway.robot.algo.Pose2D;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.emoji.BaseControlHandler;
import com.segway.robot.sdk.locomotion.sbv.Base;

/**
 * Created by laoxinqiang on 2017/4/19.
 */

public class BaseControlManager implements BaseControlHandler {
    private static final String TAG = "BaseControlManager";

    private static Base mBase;
    private static boolean mIsBindSuccess = false;

    public BaseControlManager(Context context) {
        Log.d(TAG, "BaseControlHandler() called");
        mBase = Base.getInstance();
        mBase.bindService(context.getApplicationContext(), mBindStateListener);
    }


    // This will make the robot move
    public static void moveForward() {
        if (mIsBindSuccess) {
            mBase.addCheckPoint(0.5f, 0f);
        }
    }
    public static void moveBackward() {
        if (mIsBindSuccess) {
            mBase.addCheckPoint(-0.5f, 0f);
        }
    }
    public static void moveLeft() {
        if (mIsBindSuccess) {
            mBase.addCheckPoint(0f, 0.5f);
        }
    }
    public static void moveRight() {
        if (mIsBindSuccess) {
            mBase.addCheckPoint(0f, -0.5f);
        }
    }
    public static void rotateLeft() {
        if (mIsBindSuccess) {
            mBase.addCheckPoint(0f,0f, (float) Math.PI/2);
        }
    }
    // These following functions will make it move the other directions
    public static void rotateRight() {
        if (mIsBindSuccess) {
            mBase.addCheckPoint(0f, 0f, (float) (-1*Math.PI)/2);
        }
    }
    public static void turnAround() {
        if (mIsBindSuccess) {
            mBase.addCheckPoint(0f,0f, (float) Math.PI);
        }
    }
    public static void goBackToStart() {
        if (mIsBindSuccess) {
            mBase.addCheckPoint(0f,0f, (float) 0);
        }
    }

    public static void baseOriginReset() {
        mBase.setControlMode(Base.CONTROL_MODE_NAVIGATION);
        //mBase.clearCheckPointsAndStop();
        mBase.cleanOriginalPoint();
        Pose2D newOriginPoint = mBase.getOdometryPose(-1);
        mBase.setOriginalPoint(newOriginPoint);
    }


    private ServiceBinder.BindStateListener mBindStateListener = new ServiceBinder.BindStateListener() {
        @Override
        public void onBind() {
            Log.d(TAG, "onBind() called");
            mIsBindSuccess = true;
        }

        @Override
        public void onUnbind(String reason) {
            Log.d(TAG, "onUnbind() called with: reason = [" + reason + "]");
            mIsBindSuccess = false;
        }
    };

    @Override
    public void setLinearVelocity(float velocity) {
        if (mIsBindSuccess) {
            mBase.setLinearVelocity(velocity);
        }

    }

    @Override
    public void setAngularVelocity(float velocity) {
        if (mIsBindSuccess) {
            mBase.setAngularVelocity(velocity);
        }
    }

    @Override
    public void stop() {
        if (mIsBindSuccess) {
            mBase.stop();
        }
    }

    @Override
    public Ticks getTicks() {
        return null;
    }
}
