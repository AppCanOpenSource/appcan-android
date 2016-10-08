package org.zywx.wbpalmstar.engine.container;

import android.content.Context;
import android.support.annotation.Keep;
import android.support.v4.view.ViewPager;

import org.zywx.wbpalmstar.base.vo.CreateContainerVO;

/**
 * Created by ylt on 2016/10/8.
 * 容器
 */
@Keep
public class ContainerViewPager extends ViewPager {

    private CreateContainerVO mContainerVO;

    public ContainerViewPager(Context context, CreateContainerVO containerVO) {
        super(context);
        this.mContainerVO = containerVO;
    }

    @Keep
    public CreateContainerVO getContainerVO() {
        return mContainerVO;
    }
}