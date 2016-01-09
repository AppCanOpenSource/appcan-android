package org.zywx.wbpalmstar.base.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Created by ylt on 15/11/4.
 */
public class BaseFragment extends Fragment {

    private OnViewCreatedListener mOnViewCreatedListener;

    public void setOnViewCreatedListener(OnViewCreatedListener onViewCreatedListener) {
        mOnViewCreatedListener = onViewCreatedListener;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (mOnViewCreatedListener != null) {
            mOnViewCreatedListener.onViewCreated(view);
        }
        super.onViewCreated(view, savedInstanceState);
    }

    public interface OnViewCreatedListener {
        void onViewCreated(View view);
    }

}
