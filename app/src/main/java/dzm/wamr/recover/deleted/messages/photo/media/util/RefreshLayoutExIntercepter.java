package dzm.wamr.recover.deleted.messages.photo.media.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class RefreshLayoutExIntercepter extends SwipeRefreshLayout {

    public RefreshLayoutExIntercepter(Context ctx) {
        super(ctx);
        mTouchSlop = ViewConfiguration.get(ctx).getScaledTouchSlop();
    }

    public RefreshLayoutExIntercepter(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        mTouchSlop = ViewConfiguration.get(ctx).getScaledTouchSlop();
    }

    private int mTouchSlop;
    private float mPrevX;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPrevX = event.getX();
                break;

            case MotionEvent.ACTION_MOVE:
                final float eventX = event.getX();
                float xDiff = Math.abs(eventX - mPrevX);

                if (xDiff > mTouchSlop) {
                    return false;
                }
        }

        return super.onInterceptTouchEvent(event);
    }
}
