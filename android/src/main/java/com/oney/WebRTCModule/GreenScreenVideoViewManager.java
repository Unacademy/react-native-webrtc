package com.oney.WebRTCModule;

import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.HashMap;
import java.util.Map;

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

    @ReactProp(name = "mirror")
    public void setMirror(WebRTCGreenScreenView view, boolean mirror) {
        view.setMirror(mirror);
    }

    @ReactProp(name = "objectFit")
    public void setObjectFit(WebRTCGreenScreenView view, String objectFit) {
        view.setObjectFit(objectFit);
    }

    @ReactProp(name = "streamURL")
    public void setStreamURL(WebRTCGreenScreenView view, String streamURL) {
        view.setStreamURL(streamURL);
    }

    @ReactProp(name = "zOrder")
    public void setZOrder(WebRTCGreenScreenView view, int zOrder) {
        view.setZOrder(zOrder);
    }

    @ReactProp(name = "onDimensionsChange")
    public void setOnDimensionsChange(WebRTCGreenScreenView view, boolean onDimensionsChange) {
        view.setOnDimensionsChange(onDimensionsChange);
    }

    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        Map<String, Object> eventTypeConstants = new HashMap<>();
        Map<String, String> dimensionsChangeEvent = new HashMap<>();
        dimensionsChangeEvent.put("registrationName", "onDimensionsChange");
        eventTypeConstants.put("onDimensionsChange", dimensionsChangeEvent);
        return eventTypeConstants;
    }
}
