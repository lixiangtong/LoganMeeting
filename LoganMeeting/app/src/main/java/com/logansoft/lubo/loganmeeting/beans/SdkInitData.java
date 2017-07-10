package com.logansoft.lubo.loganmeeting.beans;

/**
 * Created by logansoft on 2017/7/10.
 */

public class SdkInitData {
    private String oemID;
    private String cfgPathFileName;
    private String loggerPathFileName;
    private boolean showSDKLogConsole;

    public String getOemID() {
        return oemID;
    }

    public void setOemID(String oemID) {
        this.oemID = oemID;
    }

    public String getCfgPathFileName() {
        return cfgPathFileName;
    }

    public void setCfgPathFileName(String cfgPathFileName) {
        this.cfgPathFileName = cfgPathFileName;
    }

    public String getLoggerPathFileName() {
        return loggerPathFileName;
    }

    public void setLoggerPathFileName(String loggerPathFileName) {
        this.loggerPathFileName = loggerPathFileName;
    }

    public boolean isShowSDKLogConsole() {
        return showSDKLogConsole;
    }

    public void setShowSDKLogConsole(boolean showSDKLogConsole) {
        this.showSDKLogConsole = showSDKLogConsole;
    }
}
