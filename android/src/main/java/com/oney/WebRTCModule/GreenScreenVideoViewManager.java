package com.oney.WebRTCModule;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;

import org.webrtc.MediaStream;

public class GreenScreenVideoViewManager extends SimpleViewManager<WebRTCGreenScreenView> {
  private static final String REACT_CLASS = "GreenScreenVideoView";

  @Override
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  public WebRTCGreenScreenView createViewInstance(ThemedReactContext context) {
    return new WebRTCGreenScreenView(context);
  }

  /**
   * Sets the indicator which determines whether a specific {@link WebRTCGreenScreenView}
   * is to mirror the video specified by {@code streamURL} during its rendering.
   * For more details, refer to the documentation of the {@code mirror} property
   * of the JavaScript counterpart of {@code WebRTCGreenScreenView} i.e. {@code RTCView}.
   *
   * @param view The {@code WebRTCGreenScreenView} on which the specified {@code mirror} is
   * to be set.
   * @param mirror If the specified {@code WebRTCGreenScreenView} is to mirror the video
   * specified by its associated {@code streamURL} during its rendering,
   * {@code true}; otherwise, {@code false}.
   */
  @ReactProp(name = "mirror")
  public void setMirror(WebRTCGreenScreenView view, boolean mirror) {
    view.setMirror(mirror);
  }

  /**
   * In the fashion of
   * https://www.w3.org/TR/html5/embedded-content-0.html#dom-video-videowidth
   * and https://www.w3.org/TR/html5/rendering.html#video-object-fit, resembles
   * the CSS style {@code object-fit}.
   *
   * @param view The {@code WebRTCGreenScreenView} on which the specified {@code objectFit}
   * is to be set.
   * @param objectFit For details, refer to the documentation of the
   * {@code objectFit} property of the JavaScript counterpart of
   * {@code WebRTCGreenScreenView} i.e. {@code RTCView}.
   */
  @ReactProp(name = "objectFit")
  public void setObjectFit(WebRTCGreenScreenView view, String objectFit) {
    view.setObjectFit(objectFit);
  }

  @ReactProp(name = "streamURL")
  public void setStreamURL(WebRTCGreenScreenView view, String streamURL) {
    view.setStreamURL(streamURL);
  }

  /**
   * Sets the z-order of a specific {@link WebRTCGreenScreenView} in the stacking space of
   * all {@code WebRTCGreenScreenView}s. For more details, refer to the documentation of
   * the {@code zOrder} property of the JavaScript counterpart of
   * {@code WebRTCGreenScreenView} i.e. {@code RTCView}.
   *
   * @param view The {@code WebRTCGreenScreenView} on which the specified {@code zOrder} is
   * to be set.
   * @param zOrder The z-order to set on the specified {@code WebRTCGreenScreenView}.
   */
  @ReactProp(name = "zOrder")
  public void setZOrder(WebRTCGreenScreenView view, int zOrder) {
    view.setZOrder(zOrder);
  }

//  @ReactProp(name = "useGreenScreen", defaultBoolean = false)
//  public void setUseGreenScreen(WebRTCGreenScreenView view, boolean value) {
//    view.setUseGreenScreen(value);
//  }
}
