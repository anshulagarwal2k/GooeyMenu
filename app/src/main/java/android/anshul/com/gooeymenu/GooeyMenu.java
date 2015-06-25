package android.anshul.com.gooeymenu;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import java.util.ArrayList;

/**
 * Created by Anshul on 24/06/15.
 */
public class GooeyMenu extends View {

    private static final long ANIMATION_DURATION = 1000;
    private final float START_ANGLE = 0f;
    private final float END_ANGLE = 45f;
    private int mNumberOfMenu = 5;//Todo
    private final float BEZIER_CONSTANT = 0.551915024494f;// pre-calculated value

    private int mFabButtonRadius;
    private int mMenuButtonRadius;
    private int mGab;
    private int mCenterX;
    private int mCenterY;
    private Paint mCirclePaint;
    private Paint mMenuPaint;
    private ImageView mImageView;
    private CirclePoint mCirclePoint;
    private ArrayList<CirclePoint> mMenuPoints = new ArrayList<>();
    private ArrayList<ObjectAnimator> mShowAnimation = new ArrayList<>();
    private ArrayList<ObjectAnimator> mHideAnimation = new ArrayList<>();
    private ValueAnimator mBezierAnimation, mBezierEndAnimation, mRotationAnimation;
    private boolean isMenuVisible = true;
    private Float bezierConstant = BEZIER_CONSTANT;
    private Bitmap mPlusBitmap;
    private float mRotationAngle;
    private ValueAnimator mRotationReverseAnimation;
    private GooeyMenuInterface mGooeyMenuInterface;
    private boolean gooeyMenuTouch;

    public GooeyMenu(Context context) {
        super(context);
        init();
    }

    public GooeyMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public GooeyMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public GooeyMenu(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mFabButtonRadius = getResources().getDimensionPixelSize(R.dimen.big_circle_radius);
        mMenuButtonRadius = getResources().getDimensionPixelSize(R.dimen.small_circle_radius);
        mGab = getResources().getDimensionPixelSize(R.dimen.min_gap);
        mCirclePaint = new Paint();
        mCirclePaint.setColor(getResources().getColor(R.color.default_color));
        mCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mBezierEndAnimation = ValueAnimator.ofFloat(BEZIER_CONSTANT + .2f, BEZIER_CONSTANT);
        mBezierEndAnimation.setInterpolator(new LinearInterpolator());
        mBezierEndAnimation.setDuration(300);
        mBezierEndAnimation.addUpdateListener(mBezierUpdateListener);

        mBezierAnimation = ValueAnimator.ofFloat(BEZIER_CONSTANT - .02f, BEZIER_CONSTANT + .2f);
        mBezierAnimation.setDuration(ANIMATION_DURATION/4);
        mBezierAnimation.setRepeatCount(4);
        mBezierAnimation.setInterpolator(new LinearInterpolator());
        mBezierAnimation.addUpdateListener(mBezierUpdateListener);
        mBezierAnimation.addListener(mBezierAnimationListener);

        mRotationAnimation = ValueAnimator.ofFloat(START_ANGLE, END_ANGLE);
        mRotationAnimation.setDuration(ANIMATION_DURATION / 4);
        mRotationAnimation.setInterpolator(new AccelerateInterpolator());
        mRotationAnimation.addUpdateListener(mRotationUpdateListener);
        mRotationReverseAnimation = ValueAnimator.ofFloat(END_ANGLE, START_ANGLE);
        mRotationReverseAnimation.setDuration(ANIMATION_DURATION / 4);
        mRotationReverseAnimation.setInterpolator(new AccelerateInterpolator());
        mRotationReverseAnimation.addUpdateListener(mRotationUpdateListener);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int desiredWidth;
        int desiredHeight;
        desiredWidth = getMeasuredWidth();
        desiredHeight = getContext().getResources().getDimensionPixelSize(R.dimen.min_height);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;
        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = w / 2;
        mCenterY = h - mFabButtonRadius;
        for (int i = 0; i < mNumberOfMenu; i++) {
            CirclePoint circlePoint = new CirclePoint();
            circlePoint.setRadius(mGab);
            circlePoint.setAngle((Math.PI / (mNumberOfMenu + 1)) * (i + 1));
            mMenuPoints.add(circlePoint);
            ObjectAnimator animShow = ObjectAnimator.ofFloat(mMenuPoints.get(i), "Radius", 0f, mGab);
            animShow.setDuration(ANIMATION_DURATION);
            animShow.setInterpolator(new AnticipateOvershootInterpolator());
            animShow.setStartDelay((ANIMATION_DURATION * (mNumberOfMenu - i)) / 10);
            animShow.addUpdateListener(mUpdateListener);
            mShowAnimation.add(animShow);
            ObjectAnimator animHide = animShow.clone();
            animHide.setFloatValues(mGab, 0f);
            animHide.setStartDelay((ANIMATION_DURATION * i) /10);
            mHideAnimation.add(animHide);
        }

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mImageView = new ImageView(getContext());
        mImageView.setImageResource(R.drawable.ic_launcher);
        mPlusBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.plus);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPlusBitmap = null;
        mBezierAnimation = null;
        mHideAnimation.clear();
        mHideAnimation = null;
        mShowAnimation.clear();
        mHideAnimation = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < mNumberOfMenu; i++) {
            CirclePoint circlePoint = mMenuPoints.get(i);
            float x = (float) (circlePoint.radius * Math.cos(circlePoint.angle));
            float y = (float) (circlePoint.radius * Math.sin(circlePoint.angle));
            canvas.drawCircle(x + mCenterX, mCenterY - y, mMenuButtonRadius, mCirclePaint);
        }
        canvas.save();
        canvas.translate(mCenterX, mCenterY);
        canvas.drawPath(createPath(), mCirclePaint);
        canvas.rotate(mRotationAngle);
        canvas.drawBitmap(mPlusBitmap, -mPlusBitmap.getWidth() / 2, -mPlusBitmap.getHeight() / 2, mCirclePaint);
        canvas.restore();
    }

    // Use Bezier path to create circle,
    /*    P_0 = (0,1), P_1 = (c,1), P_2 = (1,c), P_3 = (1,0)
        P_0 = (1,0), P_1 = (1,-c), P_2 = (c,-1), P_3 = (0,-1)
        P_0 = (0,-1), P_1 = (-c,-1), P_3 = (-1,-c), P_4 = (-1,0)
        P_0 = (-1,0), P_1 = (-1,c), P_2 = (-c,1), P_3 = (0,1)
        with c = 0.551915024494*/

    private Path createPath() {
        Path path = new Path();
        float c = bezierConstant * mFabButtonRadius;

        path.moveTo(0, mFabButtonRadius);
        path.cubicTo(bezierConstant * mFabButtonRadius, mFabButtonRadius, mFabButtonRadius, BEZIER_CONSTANT * mFabButtonRadius, mFabButtonRadius, 0);
        path.cubicTo(mFabButtonRadius, BEZIER_CONSTANT * mFabButtonRadius * (-1), c, (-1) * mFabButtonRadius, 0, (-1) * mFabButtonRadius);
        path.cubicTo((-1) * c, (-1) * mFabButtonRadius, (-1) * mFabButtonRadius, (-1) * BEZIER_CONSTANT * mFabButtonRadius, (-1) * mFabButtonRadius, 0);
        path.cubicTo((-1) * mFabButtonRadius, BEZIER_CONSTANT * mFabButtonRadius, (-1) * bezierConstant * mFabButtonRadius, mFabButtonRadius, 0, mFabButtonRadius);

        return path;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isGooeyMenuTouch(event)) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                mBezierAnimation.start();
                cancelAllAnimation();
                if (isMenuVisible) {
                    startHideAnimate();
                } else {
                    startShowAnimate();
                }
                isMenuVisible = !isMenuVisible;
        }
        return true;
    }

    public void setOnMenuListener(GooeyMenuInterface onMenuListener) {
        mGooeyMenuInterface = onMenuListener;
    }

    public boolean isGooeyMenuTouch(MotionEvent event) {
        if (event.getX() >= mCenterX - mFabButtonRadius && event.getX() <= mCenterX + mFabButtonRadius) {
            if (event.getY() >= mCenterY - mFabButtonRadius && event.getY() <= mCenterY + mFabButtonRadius) {
                return true;
            }
        }
        return false;
    }

    // Helper class for animation and Menu Item cicle center Points
    public class CirclePoint {
        private float x;
        private float y;
        private float radius = 0.0f;
        private double angle = 0.0f;

        public void setX(float x1) {
            x = x1;
        }

        public float getX() {
            return x;
        }

        public void setY(float y1) {
            y = y1;
        }

        public float getY() {
            return y;
        }

        public void setRadius(float r) {
            radius = r;
        }

        public float getRadius() {
            return radius;
        }

        public void setAngle(double angle) {
            this.angle = angle;
        }

        public double getAngle() {
            return angle;
        }
    }

    private void startShowAnimate() {
        mRotationAnimation.start();
        for (ObjectAnimator objectAnimator : mShowAnimation) {
            objectAnimator.start();
        }
    }

    private void startHideAnimate() {
        mRotationReverseAnimation.start();
        for (ObjectAnimator objectAnimator : mHideAnimation) {
            objectAnimator.start();
        }
    }

    private void cancelAllAnimation() {
        for (ObjectAnimator objectAnimator : mHideAnimation) {
            objectAnimator.cancel();
        }
        for (ObjectAnimator objectAnimator : mShowAnimation) {
            objectAnimator.cancel();
        }
    }

    ValueAnimator.AnimatorUpdateListener mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            invalidate();
        }
    };

    ValueAnimator.AnimatorUpdateListener mBezierUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            bezierConstant = (float) valueAnimator.getAnimatedValue();
            invalidate();
        }
    };
    ValueAnimator.AnimatorUpdateListener mRotationUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            mRotationAngle = (float) valueAnimator.getAnimatedValue();
            invalidate();
        }
    };

    ValueAnimator.AnimatorListener mBezierAnimationListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            mBezierEndAnimation.start();
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }
        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };



    public interface GooeyMenuInterface{
        /***
         * Called when menu opened
         */
         void menuOpen();
        /***
         * Called when menu Closed
         */
        void menuClose();

        /***
         *  Called when Menu item Clicked
         *  @param menuNumber give menu number which clicked.
         */
        void menuItemClicked(int menuNumber);
    }
}
