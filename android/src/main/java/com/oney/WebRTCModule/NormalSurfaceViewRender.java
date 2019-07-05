package com.oney.WebRTCModule;

import android.content.Context;
import android.util.AttributeSet;

import org.webrtc.SurfaceViewRenderer;

public class NormalSurfaceViewRender extends SurfaceViewRenderer implements ViewRenderInterface {
    public NormalSurfaceViewRender(Context context) {
        super(context);
    }

    public NormalSurfaceViewRender(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setGreenScreenFlags() {

    }
}
