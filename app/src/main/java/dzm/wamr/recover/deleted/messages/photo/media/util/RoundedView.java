package dzm.wamr.recover.deleted.messages.photo.media.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dzm.wamr.recover.deleted.messages.photo.media.R;

/**
 * Custom wrapper view to get round corner round view
 */
public class RoundedView extends FrameLayout {

    /**
     * The corners than can be changed
     */
    private float topLeftCornerRadius;
    private float topRightCornerRadius;
    private float bottomLeftCornerRadius;
    private float bottomRightCornerRadius;

    public RoundedView(@NonNull Context context) {
        super(context);
        init(context, null, 0);
    }

    public RoundedView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public RoundedView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.RoundedView, 0, 0);

        //get the default value form the attrs
        topLeftCornerRadius = typedArray.getDimension(R.styleable.
                RoundedView_topLeftCornerRadius, 20);
        topRightCornerRadius = typedArray.getDimension(R.styleable.
                RoundedView_topRightCornerRadius, 20);
        bottomLeftCornerRadius = typedArray.getDimension(R.styleable.
                RoundedView_bottomLeftCornerRadius, 0);
        bottomRightCornerRadius = typedArray.getDimension(R.styleable.
                RoundedView_bottomRightCornerRadius, 20);

        typedArray.recycle();
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int count = canvas.save();

        final Path path = new Path();

        float[] cornerDimensions = {
                topLeftCornerRadius, topLeftCornerRadius,
                topRightCornerRadius, topRightCornerRadius,
                bottomRightCornerRadius, bottomRightCornerRadius,
                bottomLeftCornerRadius, bottomLeftCornerRadius};

        path.addRoundRect(new RectF(0, 0, canvas.getWidth(), canvas.getHeight())
                , cornerDimensions, Path.Direction.CW);

        canvas.clipPath(path);

        super.dispatchDraw(canvas);
        canvas.restoreToCount(count);
    }

    public void setTopLeftCornerRadius(float topLeftCornerRadius) {
        this.topLeftCornerRadius = topLeftCornerRadius;
        invalidate();
    }

    public void setTopRightCornerRadius(float topRightCornerRadius) {
        this.topRightCornerRadius = topRightCornerRadius;
        invalidate();
    }

    public void setBottomLeftCornerRadius(float bottomLeftCornerRadius) {
        this.bottomLeftCornerRadius = bottomLeftCornerRadius;
        invalidate();
    }

    public void setBottomRightCornerRadius(float bottomRightCornerRadius) {
        this.bottomRightCornerRadius = bottomRightCornerRadius;
        invalidate();
    }
}
