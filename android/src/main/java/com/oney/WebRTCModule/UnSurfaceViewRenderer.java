package com.oney.WebRTCModule;


import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;

import java.util.concurrent.CountDownLatch;

import org.webrtc.EglBase;
import org.webrtc.EglRenderer;
import org.webrtc.EglRenderer.FrameListener;
import org.webrtc.GlRectDrawer;
import org.webrtc.Logging;
import org.webrtc.RendererCommon.GlDrawer;
import org.webrtc.RendererCommon.RendererEvents;
import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.RendererCommon.VideoLayoutMeasure;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.ThreadUtils;
import org.webrtc.VideoFrame;
import org.webrtc.VideoRenderer.Callbacks;
import org.webrtc.VideoRenderer.I420Frame;
import org.webrtc.VideoSink;

public class UnSurfaceViewRenderer extends SurfaceView implements Callback, Callbacks, VideoSink, ViewRenderInterface {
    private static final String TAG = "SurfaceViewRenderer";
    private final String resourceName = this.getResourceName();
    private final VideoLayoutMeasure videoLayoutMeasure = new VideoLayoutMeasure();
    private final EglRenderer eglRenderer;
    private RendererEvents rendererEvents;
    private final Object layoutLock = new Object();
    private boolean isRenderingPaused = false;
    private boolean isFirstFrameRendered;
    private int rotatedFrameWidth;
    private int rotatedFrameHeight;
    private int frameRotation;
    private boolean enableFixedSize;
    private int surfaceWidth;
    private int surfaceHeight;

    public UnSurfaceViewRenderer(Context context) {
        super(context);
        this.eglRenderer = new EglRenderer(this.resourceName);
        this.getHolder().addCallback(this);
       // this.setZOrderMediaOverlay(true);
     //   this.getHolder().setFormat(PixelFormat.TRANSPARENT);

       // this.setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        //setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        ///setEGLConfigChooser(8, 8, 8, 8, 16, 0);
       //
//        this.setZOrderOnTop(true);
//        this.getHolder().setFormat(PixelFormat.TRANSLUCENT);

       // setPreserveEGLContextOnPause(true);
        //setOpaque(false);
       //this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }

    public void setGreenScreenFlags() {
        this.setZOrderOnTop(true);
        this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }

    public UnSurfaceViewRenderer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.eglRenderer = new EglRenderer(this.resourceName);
        this.getHolder().addCallback(this);
//        this.setZOrderOnTop(true);
//        this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        // getHolder().setFormat(PixelFormat.RGB_565);

        //this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }

    public void init(org.webrtc.EglBase.Context sharedContext, RendererEvents rendererEvents) {
        this.init(sharedContext, rendererEvents, EglBase.CONFIG_RGBA, new GlRectDrawer());
    }

    public void init(org.webrtc.EglBase.Context sharedContext, RendererEvents rendererEvents, int[] configAttributes, GlDrawer drawer) {
        ThreadUtils.checkIsOnMainThread();
        this.rendererEvents = rendererEvents;
        Object var5 = this.layoutLock;
        synchronized (this.layoutLock) {
            this.isFirstFrameRendered = false;
            this.rotatedFrameWidth = 0;
            this.rotatedFrameHeight = 0;
            this.frameRotation = 0;
        }

        this.eglRenderer.init(sharedContext, configAttributes, drawer);
    }

    public void release() {
        this.eglRenderer.release();
    }

    public void addFrameListener(FrameListener listener, float scale, GlDrawer drawerParam) {
        this.eglRenderer.addFrameListener(listener, scale, drawerParam);
    }

    public void addFrameListener(FrameListener listener, float scale) {
        this.eglRenderer.addFrameListener(listener, scale);
    }

    public void removeFrameListener(FrameListener listener) {
        this.eglRenderer.removeFrameListener(listener);
    }

    public void setEnableHardwareScaler(boolean enabled) {
        ThreadUtils.checkIsOnMainThread();
        this.enableFixedSize = enabled;
        this.updateSurfaceSize();
    }

    public void setMirror(boolean mirror) {
        this.eglRenderer.setMirror(mirror);
    }

    public void setScalingType(ScalingType scalingType) {
        ThreadUtils.checkIsOnMainThread();
        this.videoLayoutMeasure.setScalingType(scalingType);
        this.requestLayout();
    }

    public void setScalingType(ScalingType scalingTypeMatchOrientation, ScalingType scalingTypeMismatchOrientation) {
        ThreadUtils.checkIsOnMainThread();
        this.videoLayoutMeasure.setScalingType(scalingTypeMatchOrientation, scalingTypeMismatchOrientation);
        this.requestLayout();
    }

    public void setFpsReduction(float fps) {
        Object var2 = this.layoutLock;
        synchronized (this.layoutLock) {
            this.isRenderingPaused = fps == 0.0F;
        }

        this.eglRenderer.setFpsReduction(fps);
    }

    public void disableFpsReduction() {
        Object var1 = this.layoutLock;
        synchronized (this.layoutLock) {
            this.isRenderingPaused = false;
        }

        this.eglRenderer.disableFpsReduction();
    }

    public void pauseVideo() {
        Object var1 = this.layoutLock;
        synchronized (this.layoutLock) {
            this.isRenderingPaused = true;
        }

        this.eglRenderer.pauseVideo();
    }

    public void renderFrame(I420Frame frame) {
        this.updateFrameDimensionsAndReportEvents(frame);
        this.eglRenderer.renderFrame(frame);
    }

    public void onFrame(VideoFrame frame) {
        this.updateFrameDimensionsAndReportEvents(frame);
        this.eglRenderer.onFrame(frame);
    }

    protected void onMeasure(int widthSpec, int heightSpec) {
        ThreadUtils.checkIsOnMainThread();
        Object var4 = this.layoutLock;
        Point size;
        synchronized (this.layoutLock) {
            size = this.videoLayoutMeasure.measure(widthSpec, heightSpec, this.rotatedFrameWidth, this.rotatedFrameHeight);
        }

        this.setMeasuredDimension(size.x, size.y);
        this.logD("onMeasure(). New size: " + size.x + "x" + size.y);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        ThreadUtils.checkIsOnMainThread();
        this.eglRenderer.setLayoutAspectRatio((float) (right - left) / (float) (bottom - top));
        this.updateSurfaceSize();
    }

    private void updateSurfaceSize() {
        ThreadUtils.checkIsOnMainThread();
        Object var1 = this.layoutLock;
        synchronized (this.layoutLock) {
            if (this.enableFixedSize && this.rotatedFrameWidth != 0 && this.rotatedFrameHeight != 0 && this.getWidth() != 0 && this.getHeight() != 0) {
                float layoutAspectRatio = (float) this.getWidth() / (float) this.getHeight();
                float frameAspectRatio = (float) this.rotatedFrameWidth / (float) this.rotatedFrameHeight;
                int drawnFrameWidth;
                int drawnFrameHeight;
                if (frameAspectRatio > layoutAspectRatio) {
                    drawnFrameWidth = (int) ((float) this.rotatedFrameHeight * layoutAspectRatio);
                    drawnFrameHeight = this.rotatedFrameHeight;
                } else {
                    drawnFrameWidth = this.rotatedFrameWidth;
                    drawnFrameHeight = (int) ((float) this.rotatedFrameWidth / layoutAspectRatio);
                }

                int width = Math.min(this.getWidth(), drawnFrameWidth);
                int height = Math.min(this.getHeight(), drawnFrameHeight);
                this.logD("updateSurfaceSize. Layout size: " + this.getWidth() + "x" + this.getHeight() + ", frame size: " + this.rotatedFrameWidth + "x" + this.rotatedFrameHeight + ", requested surface size: " + width + "x" + height + ", old surface size: " + this.surfaceWidth + "x" + this.surfaceHeight);
                if (width != this.surfaceWidth || height != this.surfaceHeight) {
                    this.surfaceWidth = width;
                    this.surfaceHeight = height;
                    this.getHolder().setFixedSize(width, height);
                }
            } else {
                this.surfaceWidth = this.surfaceHeight = 0;
                this.getHolder().setSizeFromLayout();
            }

        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        ThreadUtils.checkIsOnMainThread();
        this.eglRenderer.createEglSurface(holder.getSurface());
        this.surfaceWidth = this.surfaceHeight = 0;
        this.updateSurfaceSize();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        ThreadUtils.checkIsOnMainThread();
        final CountDownLatch completionLatch = new CountDownLatch(1);
        this.eglRenderer.releaseEglSurface(new Runnable() {
            public void run() {
                completionLatch.countDown();
            }
        });
        ThreadUtils.awaitUninterruptibly(completionLatch);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        ThreadUtils.checkIsOnMainThread();
        this.logD("surfaceChanged: format: " + format + " size: " + width + "x" + height);
    }

    private String getResourceName() {
        try {
            return this.getResources().getResourceEntryName(this.getId()) + ": ";
        } catch (NotFoundException var2) {
            return "";
        }
    }

    public void clearImage() {
        this.eglRenderer.clearImage();
    }

    private void updateFrameDimensionsAndReportEvents(I420Frame frame) {
        Object var2 = this.layoutLock;
        synchronized (this.layoutLock) {
            if (!this.isRenderingPaused) {
                if (!this.isFirstFrameRendered) {
                    this.isFirstFrameRendered = true;
                    this.logD("Reporting first rendered frame.");
                    if (this.rendererEvents != null) {
                        this.rendererEvents.onFirstFrameRendered();
                    }
                }

                if (this.rotatedFrameWidth != frame.rotatedWidth() || this.rotatedFrameHeight != frame.rotatedHeight() || this.frameRotation != frame.rotationDegree) {
                    this.logD("Reporting frame resolution changed to " + frame.width + "x" + frame.height + " with rotation " + frame.rotationDegree);
                    if (this.rendererEvents != null) {
                        this.rendererEvents.onFrameResolutionChanged(frame.width, frame.height, frame.rotationDegree);
                    }

                    this.rotatedFrameWidth = frame.rotatedWidth();
                    this.rotatedFrameHeight = frame.rotatedHeight();
                    this.frameRotation = frame.rotationDegree;
                    this.post(new Runnable() {
                        public void run() {
                            UnSurfaceViewRenderer.this.updateSurfaceSize();
                            UnSurfaceViewRenderer.this.requestLayout();
                        }
                    });
                }

            }
        }
    }

    private void updateFrameDimensionsAndReportEvents(final VideoFrame frame) {
        Object var2 = this.layoutLock;
        synchronized (this.layoutLock) {
            if (!this.isRenderingPaused) {
                if (!this.isFirstFrameRendered) {
                    this.isFirstFrameRendered = true;
                    this.logD("Reporting first rendered frame.");
                    if (this.rendererEvents != null) {
                        this.rendererEvents.onFirstFrameRendered();
                    }
                }

                if (this.rotatedFrameWidth != frame.getRotatedWidth() || this.rotatedFrameHeight != frame.getRotatedHeight() || this.frameRotation != frame.getRotation()) {
                    this.logD("Reporting frame resolution changed to " + frame.getBuffer().getWidth() + "x" + frame.getBuffer().getHeight() + " with rotation " + frame.getRotation());
                    if (this.rendererEvents != null) {
                        this.rendererEvents.onFrameResolutionChanged(frame.getBuffer().getWidth(), frame.getBuffer().getHeight(), frame.getRotation());
                    }

                    this.rotatedFrameWidth = frame.getRotatedWidth();
                    this.rotatedFrameHeight = frame.getRotatedHeight();
                    this.frameRotation = frame.getRotation();

                    this.post(new Runnable() {
                        @Override
                        public void run() {
                            UnSurfaceViewRenderer.this.updateFrameDimensionsAndReportEvents(frame);
                        }
                    });
                }

            }
        }
    }

    private void logD(String string) {
        Logging.d("SurfaceViewRenderer", this.resourceName + string);
    }


}
