package com.example.heavymagikhq.libraryofficial;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build.VERSION;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dk.view.drop.CoverManager.OnDragCompeteListener;

public class WaterDrop extends RelativeLayout {
    private String colourOfWaterDrop = "#8e4585";//303F9F
    private Paint mPaint = new Paint();
    private TextView mTextView;
    private OnDragCompeteListener mOnDragCompeteListener;
    private boolean mHolderEventFlag;


    public WaterDrop(Context context) {
        super(context);
        init();
    }

    public WaterDrop(Context context, AttributeSet attrs) {
        super(context, attrs);
   //     initXMLAttribute(attrs);
        init();
            }

    //Meeks I have this for custom XML attributes but decided not to use it
//    private void initXMLAttribute(AttributeSet attrs) {
//
//        if (attrs != null) {
//            String packageName = "http://schemas.android.com/apk/res-auto";
//            colourOfWaterDrop = attrs.getAttributeValue(packageName, "colourOfWaterDrop");
//                    }
//    }

    public void setText(String str) {
        mTextView.setText(str);
    }

    public void setTextSize(int size) {
        mTextView.setTextSize(size);
    }

    /**
     * -1 for origin
     *
     * @param resourceId
     */
    public void setEffectResource(int resourceId){
        CoverManager.getInstance().setEffectResource(resourceId);
    }

    private void init() {
        mPaint.setAntiAlias(true);
        if (VERSION.SDK_INT > 11) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        mTextView = new TextView(getContext());
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mTextView.setTextSize(13);
        mTextView.setTextColor(0xffffffff);
        mTextView.setLayoutParams(params);
        addView(mTextView);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        mPaint.setColor(Color.parseColor(colourOfWaterDrop));
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, mPaint);
        super.dispatchDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        ViewGroup parent = getScrollableParent();
        Log.v("waterDrop", "getScrollableParent returns: " + parent.getClass().getSimpleName());
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mHolderEventFlag = !CoverManager.getInstance().isRunning();
            if (mHolderEventFlag) {
                if (parent != null)
                    parent.requestDisallowInterceptTouchEvent(true);


                CoverManager.getInstance().start(this, event.getRawX(), event.getRawY(), mOnDragCompeteListener);
            }
            break;
        case MotionEvent.ACTION_MOVE:
            if (mHolderEventFlag) {
                CoverManager.getInstance().update(event.getRawX(), event.getRawY());
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            if (mHolderEventFlag) {
                if (parent != null)
                    parent.requestDisallowInterceptTouchEvent(false);



                CoverManager.getInstance().finishDrag(this, event.getRawX(), event.getRawY());
            }
            break;
        }

        return true;
    }

    private ViewGroup getScrollableParent() {
        View target = this;
        while (true) {
            View parent;
            try {
                /**
                 * ViewRootImpl cannot be cast to android.view.View
                 */
                parent = (View) target.getParent();
            } catch (Exception e) {
                return null;
            }
            if (parent == null)
                return null;
            //MEEKS had to add parent instanceof RecyclerView to get the library to work as otherwise when getScrollableParent is called it would return null if the WaterDrop is in a Recylcerview
            //and this screws up the animation
            if (parent instanceof ListView || parent instanceof ScrollView || parent instanceof RecyclerView) {
                return (ViewGroup) parent;
            }
            target = parent;
        }

    }

    public void setOnDragCompeteListener(OnDragCompeteListener onDragCompeteListener) {
        mOnDragCompeteListener = onDragCompeteListener;
    }
}
