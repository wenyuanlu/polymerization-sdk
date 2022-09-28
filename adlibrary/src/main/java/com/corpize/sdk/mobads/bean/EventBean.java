package com.corpize.sdk.mobads.bean;

import java.io.Serializable;
import java.util.List;

/**
 * author ：yh
 * date : 2020-02-15 22:17
 * description :
 */
public class EventBean implements Serializable {

    private List<String> midpoint;
    private List<String> acceptInvitation;
    private List<String> fullscreen;
    private List<String> mute;
    private List<String> pause;
    private List<String> unmute;
    private List<String> close;
    private List<String> complete;
    private List<String> thirdQuartile;
    private List<String> start;
    private List<String> firstQuartile;

    public List<String> getMidpoint () {
        return midpoint;
    }

    public void setMidpoint (List<String> midpoint) {
        this.midpoint = midpoint;
    }

    public List<String> getAcceptInvitation () {
        return acceptInvitation;
    }

    public void setAcceptInvitation (List<String> acceptInvitation) {
        this.acceptInvitation = acceptInvitation;
    }

    public List<String> getFullscreen () {
        return fullscreen;
    }

    public void setFullscreen (List<String> fullscreen) {
        this.fullscreen = fullscreen;
    }

    public List<String> getMute () {
        return mute;
    }

    public void setMute (List<String> mute) {
        this.mute = mute;
    }

    public List<String> getPause () {
        return pause;
    }

    public void setPause (List<String> pause) {
        this.pause = pause;
    }

    public List<String> getUnmute () {
        return unmute;
    }

    public void setUnmute (List<String> unmute) {
        this.unmute = unmute;
    }

    public List<String> getClose () {
        return close;
    }

    public void setClose (List<String> close) {
        this.close = close;
    }

    public List<String> getComplete () {
        return complete;
    }

    public void setComplete (List<String> complete) {
        this.complete = complete;
    }

    public List<String> getThirdQuartile () {
        return thirdQuartile;
    }

    public void setThirdQuartile (List<String> thirdQuartile) {
        this.thirdQuartile = thirdQuartile;
    }

    public List<String> getStart () {
        return start;
    }

    public void setStart (List<String> start) {
        this.start = start;
    }

    public List<String> getFirstQuartile () {
        return firstQuartile;
    }

    public void setFirstQuartile (List<String> firstQuartile) {
        this.firstQuartile = firstQuartile;
    }

}
