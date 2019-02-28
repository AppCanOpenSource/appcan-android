/*
 * Copyright (c) 2015.  The AppCan Open Source Project.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package org.zywx.wbpalmstar.engine.universalex;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.webkit.JavascriptInterface;

import org.zywx.wbpalmstar.base.EUExAbstractDispatcher;
import org.zywx.wbpalmstar.base.vo.DispatchResultVO;
import org.zywx.wbpalmstar.engine.callback.EUExDispatcherCallback;

/**
 * Created by ylt on 15/8/21.
 */
public class EUExDispatcher extends EUExAbstractDispatcher {

    public EUExDispatcherCallback mDispatcherCallback;

    public EUExDispatcher(EUExDispatcherCallback callback) {
        this.mDispatcherCallback = callback;
    }

    @JavascriptInterface
    public String dispatch(final String pluginName, final String methodName, final String[] params){
        final DispatchResultVO resultVO = new DispatchResultVO();

        Handler handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                synchronized (resultVO) {
                    DispatchResultVO resultVO = (DispatchResultVO) msg.obj;
                    resultVO.setResult(mDispatcherCallback.onDispatch(pluginName, methodName, params));
                    resultVO.notify();
                }
            }
        };
        synchronized (resultVO) {
            Message msg = Message.obtain();
            msg.obj = resultVO;
            handler.sendMessage(msg);

            try {
                resultVO.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return resultVO.getResult();
        }
    }

}