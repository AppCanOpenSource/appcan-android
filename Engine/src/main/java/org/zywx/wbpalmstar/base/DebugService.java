package org.zywx.wbpalmstar.base;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import org.zywx.wbpalmstar.widgetone.dataservice.WDataManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by ylt on 2016/12/21.
 */
public class DebugService extends IntentService {

    public static final String KEY_TYPE_DEBUG = "type_debug";
    public static final String KEY_LOG_DATA = "log_data";

    public static final int TYPE_LOG = 1;


    private static final int logServerPort = 30050; //AppCan IDE接收log 的端口号


    private DatagramSocket mSocket;


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public DebugService() {
        super("AppCanDebugService");
        createUDP();
    }

    @Override
    public void onDestroy() {
        closeUDP();
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int type = intent.getIntExtra(KEY_TYPE_DEBUG, 0);
        if (type == TYPE_LOG) {
            String log = intent.getStringExtra(KEY_LOG_DATA);
            if (TextUtils.isEmpty(log)) {
                return;
            }
            byte[] data = log.getBytes();
            InetAddress inetAddress;
            try {
                if (mSocket == null) {
                    return;
                }
                inetAddress = InetAddress.getByName(WDataManager.sRootWgt.m_logServerIp);
                DatagramPacket sendPacket = new DatagramPacket(data, data.length,
                        inetAddress, logServerPort);
                mSocket.send(sendPacket);
            } catch (IOException e) {
                closeUDP();
                e.printStackTrace();
            } catch (SecurityException e) {
                closeUDP();
                e.printStackTrace();
            }
        } else {

        }

    }

    private void createUDP() {
        try {
            if (mSocket == null) {
                mSocket = new DatagramSocket();
                mSocket.setBroadcast(true);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void closeUDP() {
        if (mSocket != null) {
            mSocket.close();
            mSocket = null;
        }
    }


}
