package com.example.mzylowski.lab6;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

class ESSurfaceView extends GLSurfaceView
{
    private Float lastX;
    private Float lastY;
    public GLRenderer renderer;

    public ESSurfaceView(Context context)
    {
        super(context);

        // Stworzenie kontekstu OpenGL ES 2.0.
        setEGLContextClientVersion(2);
        // Przypisanie renderera do widoku.
        renderer = new GLRenderer();
        setRenderer(renderer);
    }

    private float prevX;
    private float prevY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event != null) {
            float x = event.getX();
            float y = event.getY();
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float dx = x - prevX;
                float dy = y - prevY;
                renderer.moveCamera(-dx/getWidth(), dy/getHeight());
            }

            prevX = event.getX();
            prevY = event.getY();
            return true;
        }
        else {
            return super.onTouchEvent(event);
        }
    }
}
