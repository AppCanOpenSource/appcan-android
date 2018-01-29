package org.zywx.wbpalmstar.base.vo;

/**
 * Created by zhangyipeng on 2018/1/4.
 */

public class WidgetConfigVO {

    public String appId;

    public String appkey;

    public String widgetName;

    public String description;

    public String indexUrl;

    public IndexWindowOptionsVO indexWindowOptions;

    public String obfuscation;

    public String errorPath;

    public String debug;

    public int animId;

    public int animDuration;

    public String cbFuncName;

    public String startInfo;

    public static class IndexWindowOptionsVO {

        public int windowStyle;
        public WindowOptionsVO windowOptions;
        public int flag;
        public WindowOpenExtrasVO extras;


    }

}
