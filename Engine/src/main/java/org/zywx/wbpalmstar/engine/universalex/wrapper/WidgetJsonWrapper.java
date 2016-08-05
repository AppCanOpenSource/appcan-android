package org.zywx.wbpalmstar.engine.universalex.wrapper;

import org.zywx.wbpalmstar.base.vo.WidgetFinishVO;
import org.zywx.wbpalmstar.engine.universalex.EUExWidget;

/**
 * Created by ylt on 16/8/5.
 *
 * 兼容Widget 相关接口 Json传参
 */

public class WidgetJsonWrapper {

    public static void finishWidget(EUExWidget widget, WidgetFinishVO finishVO){
        widget.finishWidget(new String[]{
                finishVO.resultInfo,
                finishVO.appId,
                finishVO.finishMode
        });
    }

}
