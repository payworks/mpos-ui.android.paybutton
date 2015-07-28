/*
 * mpos-ui : http://www.payworksmobile.com
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 payworks GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.mpos.ui.paybutton.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SignatureView extends View {

    private static final String TAG = "SignatureView";

    private Path mPath;
    private Paint mPaint;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private int bgColor;

    private float curX, curY;
    private boolean isDragged = false;

    private static final int TOUCH_TOLERANCE = 4;
    private static final int STROKE_WIDTH = 5;

    /**
     * The Listener.
     */
    private SignatureViewListener mListener;


    /**
     * The interface Signature view listener.
     */
    public interface SignatureViewListener {
        /**
         * Signature view did draw s Signature.
         * Called after views touch up
         *
         * @param signatureView the signature view
         */
        void signatureViewDidDrawSignature(SignatureView signatureView);

        /**
         * Signature view did clear signature.
         * 
         * @param signatureView the signature view
         */
        void signatureViewDidClearSignature(SignatureView signatureView);
    }


    public SignatureView(Context context) {
        super(context);
        init();
    }

    public SignatureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SignatureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setListener(SignatureViewListener listener) {
        mListener = listener;
    }

    private void init() {
        setFocusable(true);
        bgColor = Color.WHITE;
        mPath = new Path();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(bgColor ^ 0x00FFFFFF);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(STROKE_WIDTH);
    }

    /**
     * Set the color of the signature.
     *
     * @param color the hex representation of the desired color, most likely an instance of Color.*
     */
    public void setSigColor(int color) {
        mPaint.setColor(color);
    }

    /**
     * Set the color of the signature. For simpler option just us setSigColor(int color).
     *
     * @param a alpha value
     * @param r red value
     * @param g green value
     * @param b blue value\
     */
    public void setSigColor(int a, int r, int g, int b) {
        mPaint.setARGB(a, r, g, b);
    }

    /**
     * Clear the view
     */
    public void clearSignature() {
        Log.d(TAG, "::clearSig:" + "");
        if (mCanvas != null) {
            mCanvas.drawColor(bgColor);
            mCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
            mPath.reset();
            invalidate();
            if(mListener != null) {
                mListener.signatureViewDidClearSignature(this);
            }
        }
    }

    /**
     * Get the bitmap backing the view.
     */
    public Bitmap getImage() {
        return this.mBitmap;
    }

    public void setImage(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int bitW = mBitmap != null ? mBitmap.getWidth() : 0;
        int bitH = mBitmap != null ? mBitmap.getWidth() : 0;

        // If the width and height of the bitmap are bigger than the
        // new defined size, then keep the excess bitmap and return
        // (Part of the backing bitmap will be clipped off, but it
        // will still exist)
        if (bitW >= w && bitH >= h) {
            return;
        }

        if (bitW < w) bitW = w;
        if (bitH < h) bitH = h;

        // Create a new bitmap and canvas for the new size
        Bitmap newBitmap = Bitmap.createBitmap(bitW, bitH, Bitmap.Config.ARGB_8888);
        Canvas newCanvas = new Canvas();
        newCanvas.setBitmap(newBitmap);

        // If the old bitmap exists, redraw it onto the new bitmap
        if (mBitmap != null) {
            newCanvas.drawBitmap(mBitmap, 0, 0, mPaint);
        }
        // Replace the old bitmap and canvas with the new one
        mBitmap = newBitmap;
        mCanvas = newCanvas;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                break;
        }
        invalidate();
        return true;
    }

    private void touchDown(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        curX = x;
        curY = y;
        isDragged = false;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - curX);
        float dy = Math.abs(y - curY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(curX, curY, (x + curX) / 2, (y + curY) / 2);
            curX = x;
            curY = y;
            isDragged = true;
        }
    }

    private void touchUp() {
        if (isDragged) {
            mPath.lineTo(curX, curY);
        } else {
            mPath.lineTo(curX + 2, curY + 2);
        }
        mCanvas.drawPath(mPath, mPaint);
        mPath.reset();

        if(mListener != null) {
            mListener.signatureViewDidDrawSignature(this);
        }
    }
}
