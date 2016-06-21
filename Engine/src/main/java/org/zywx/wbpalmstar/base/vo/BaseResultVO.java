package org.zywx.wbpalmstar.base.vo;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by ylt on 16/5/30.
 */
public class BaseResultVO implements Serializable {

    private static final long serialVersionUID = 4721678659081546388L;

    /**
     * 该字段会作为接口最后一个参数加到接口中。一般作为Id。
     */
    private Object ext;

    /**
     * 接口对应Map
     */
    private HashMap<String,String> funcMaps;

    public HashMap<String, String> getFuncMaps() {
        return funcMaps;
    }

    public void setFuncMaps(HashMap<String, String> funcMaps) {
        this.funcMaps = funcMaps;
    }

    public Object getExt() {
        return ext;
    }

    public void setExt(Object ext) {
        this.ext = ext;
    }
}
