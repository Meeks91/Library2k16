//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.example.heavymagikhq.libraryofficial;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import java.lang.reflect.Field;

public class CoverManager {
    private static final int EXPLOSION_SIZE = 200;
    private int mMaxDistance = 0;
    private static CoverManager mCoverManager;
    private static Bitmap mDest;
    private DropCover mDropCover;
    private WindowManager mWindowManager;
    private RenderActionInterface mThread;
    private Explosion mExplosion;
    private int mStatusBarHeight = 0;
    private int mResourceId = -1;
    private CoverManager.OnDragCompeteListener mOnDragCompeteListener;

    private CoverManager() {
    }

    public WindowManager getWindowManager() {
        return this.mWindowManager;
    }

    public static CoverManager getInstance() {
        if(mCoverManager == null) {
            mCoverManager = new CoverManager();
        }

        return mCoverManager;
    }

    public void init(Activity activity) {
        if(this.mDropCover == null) {
            this.mDropCover = new DropCover(activity);
            this.mWindowManager = activity.getWindowManager();
            this.mDropCover.setStatusBarHeight(this.getStatusBarHeight(activity));
            this.getStatusBarHeight(activity);
        }

    }

    public void setEffectResource(int resourceId) {
        this.mResourceId = resourceId;
    }

    public void start(View target, float x, float y, CoverManager.OnDragCompeteListener onDragCompeteListener) {
        this.mOnDragCompeteListener = onDragCompeteListener;
        Log.v("distanceChecker", "5");
        if(this.mDropCover != null && this.mDropCover.getParent() == null) {
            mDest = this.drawViewToBitmap(target);

            this.mDropCover.setTarget( target, mDest);
            int[] locations = new int[2];
            target.getLocationOnScreen(locations);
            this.attachToWindow(target.getContext());
            this.mDropCover.init( (float)locations[0], (float) locations[1], x, y);

        }
    }

    public void update(float x, float y) {
        this.mDropCover.update(x, y);
    }

    public void finishDrag(final View target, final float x, final float y) {
        target.postDelayed(new Runnable() {
            public void run() {
                double distance = CoverManager.this.mDropCover.stopDrag(target, x, y, CoverManager.this.mResourceId);

                CoverManager.this.startEffect(distance, x, y);
            }
        }, 30L);
    }

    private Bitmap drawViewToBitmap(View view) {
        if(this.mDropCover == null) {
            this.mDropCover = new DropCover(view.getContext());
        }

        int width = view.getWidth();
        int height = view.getHeight();
        if(mDest == null || mDest.getWidth() != width || mDest.getHeight() != height) {
            mDest = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        }

        Canvas c = new Canvas(mDest);
        view.draw(c);
        return mDest;
    }

    private void attachToWindow(Context context) {
        if(this.mDropCover == null) {
            this.mDropCover = new DropCover(context);
        }

        LayoutParams params = new LayoutParams();
        params.type = params.TYPE_APPLICATION;
        params.height = -1;
        params.width = -1;
        params.format = 1;
        params.flags = 16;
		params.flags = params.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS; // MEEKS we needs this to make the status bar not turn black.
																//If we don't set this primaryDark colour of the status bar wont get drawn when the bubble and the surfaceView it draws on is added to the window
        this.mWindowManager.addView(this.mDropCover, params);
    }

    public boolean isRunning() {
        return this.mDropCover == null?false:this.mDropCover.getParent() != null;
    }

    public void startEffect(double distance, float x, float y) {

        if (distance > mMaxDistance) {
            if (mOnDragCompeteListener != null)
                mOnDragCompeteListener.onDragComplete();


            if (mResourceId > 0) {
//     use the mThread below Meeks        mThread = new GifUpdateThread(mDropCover.getTargetX(),mDropCover.getTargetY(),mDropCover.getHolder(), mDropCover.getContext().getApplicationContext(),mResourceId);

                mDropCover.hasBezierDetached = false; //Meeks , call this to reset a boolean condition which stop the line of the bubble reattaching itself when it has been detached
                                                    //by the user strecting the bubble far enough for it too detach.
                mThread = new GifUpdateThread(x, y - mStatusBarHeight,mDropCover.getHolder(), mDropCover.getContext().getApplicationContext(),mResourceId);
            } else {


                mDropCover.hasBezierDetached = false;
                initExplosion(x, y - mStatusBarHeight);
                mThread = new ExplosionUpdateThread(mDropCover.getHolder(), mDropCover);
            }
            mThread.actionStart();
        } else {
            Log.v("distanceChecker", "x is 3");
            if (mDropCover.getParent() != null) {
                Log.v("distanceChecker", "x is 3a");
                getWindowManager().removeView(mDropCover);

            }
//            target.setVisibility(View.VISIBLE);
        }
    }

    public void stopEffect() {
        if(this.mThread != null) {
            this.mThread.actionStop();
            this.mThread = null;
        }

    }

    public void setEffectDuration(int lifeTime) {
        Particle.setLifeTime(lifeTime);
    }

    public void setMaxDragDistance(int maxDistance) {
        if(this.mDropCover != null) {
            this.mDropCover.setMaxDragDistance(maxDistance);
        }

    }

    public void initExplosion(float x, float y) {
        if(this.mExplosion == null || this.mExplosion.getState() == 1) {
            this.mExplosion = new Explosion(200, (int)x, (int)y);
        }

    }

    public boolean render(Canvas canvas) {
        boolean isAlive = false;
        canvas.drawColor(0, Mode.CLEAR);
        canvas.drawColor(Color.argb(0, 0, 0, 0));
        if(this.mExplosion != null) {
            isAlive = this.mExplosion.draw(canvas);
        }

        return isAlive;
    }

    public void updateExplosion() {
        if(this.mExplosion != null && this.mExplosion.isAlive()) {
            this.mExplosion.update(this.mDropCover.getHolder().getSurfaceFrame());
        }

    }

    public void removeViews() {
        if(this.mDropCover != null && this.mDropCover.getParent() != null) {
            this.getWindowManager().removeView(this.mDropCover);
        }

    }

    public int getStatusBarHeight(Activity activity) {
        if(this.mStatusBarHeight == 0) {
            Class c = null;
            Object obj = null;
            Field field = null;
            boolean x = false;
            int sbar = 38;

            try {
                c = Class.forName("com.android.internal.R$dimen");
                obj = c.newInstance();
                field = c.getField("status_bar_height");
                int x1 = Integer.parseInt(field.get(obj).toString());
                sbar = activity.getResources().getDimensionPixelSize(x1);
            } catch (Exception var8) {
                var8.printStackTrace();
            }

            this.mStatusBarHeight = sbar;
            return sbar;
        } else {
            return this.mStatusBarHeight;
        }
    }

    public interface OnDragCompeteListener {
        void onDragComplete();
    }
}
