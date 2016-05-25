package org.zywx.wbpalmstar.base.vo;

import java.io.Serializable;

/**
 * Created by ylt on 16/5/25.
 */

public class NameValuePairVO implements Serializable {

    private static final long serialVersionUID = 1299126129319457206L;

    private String name;
    private String value;

    public NameValuePairVO(String name, String value) {
        this.name=name;
        this.value=value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
