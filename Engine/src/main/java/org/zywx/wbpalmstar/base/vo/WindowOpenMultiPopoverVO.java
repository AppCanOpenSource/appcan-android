package org.zywx.wbpalmstar.base.vo;

import java.io.Serializable;

/**
 * Created by ylt on 16/8/4.
 */

public class WindowOpenMultiPopoverVO implements Serializable {

    public WindowMultiPopoverContentVO content;

    public String name;

    public int dataType;

    public int x;

    public int y;

    public int w=-1;

    public int h=-1;

    public int fontSize;

    public int flag;

    public int indexSelected;

    public WindowMultiPopoverExtraVO extras;

}
