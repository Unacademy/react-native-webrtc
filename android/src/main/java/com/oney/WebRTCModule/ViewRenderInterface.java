package com.oney.WebRTCModule;

import android.support.annotation.ColorInt;

import org.webrtc.RendererCommon;
import org.webrtc.VideoRenderer;

public interface ViewRenderInterface extends VideoRenderer.Callbacks {
    public void setBackgroundColor(@ColorInt int color);
    public void clearImage();
    public void setMirror(boolean mirror);
    public void setScalingType(RendererCommon.ScalingType scalingType);
    public void setZOrderMediaOverlay(boolean isMediaOverlay);
    public void setZOrderOnTop(boolean onTop);
    public void init(org.webrtc.EglBase.Context sharedContext, RendererCommon.RendererEvents rendererEvents);
    public void init(org.webrtc.EglBase.Context sharedContext, RendererCommon.RendererEvents rendererEvents, int[] configAttributes, RendererCommon.GlDrawer drawer);
    public void layout(int l, int t, int r, int b);
    public void release();
    public void requestLayout();
    public void setGreenScreenFlags();
}
