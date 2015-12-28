/*
 *  Copyright (C) 2014 The AppCan Open Source Project.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.zywx.wbpalmstar.engine.universalex;

public interface EUExCallback {

    // key
    public static final String F_JK_NAME = "name";
    public static final String F_JK_NUM = "num";
    public static final String F_JK_EMAIL = "email";
    public static final String F_JK_ADDRESS = "address";
    public static final String F_JK_STREET = "Street";
    public static final String F_JK_ZIP = "ZIP";
    public static final String F_JK_STATE = "State";
    public static final String F_JK_NOTE = "note";
    public static final String F_JK_COMPANY = "company";
    public static final String F_JK_TITLE = "title";
    public static final String F_JK_CPU = "cpu";
    public static final String F_JK_OS = "os";
    public static final String F_JK_MANUFACTURER = "manufacturer";
    public static final String F_JK_KEYBOARD = "keyboard";
    public static final String F_JK_MODEL = "model";
    public static final String F_JK_BLUETOOTH = "blueTooth";
    public static final String F_JK_WIFI = "wifi";
    public static final String F_JK_CAMERA = "camera";
    public static final String F_JK_GPS = "gps";
    public static final String F_JK_GPRS = "gprs";
    public static final String F_JK_TOUCH = "touch";
    public static final String F_JK_IMEI = "imei";
    public static final String F_JK_CONNECTION_STATUS = "connectStatus";
    public static final String F_JK_REST_DISK_SIZE = "restDiskSize";
    public static final String F_JK_MOBILE_OPERATOR_NAME = "mobileOperatorName";
    public static final String F_JK_MAC_ADDRESS = "macAddress";
    public static final String F_JK_DEVICE_TOKEN = "deviceToken";

    public static final String F_JK_CODE = "code";
    public static final String F_JK_TYPE = "type";
    public static final String F_JK_ICON = "icon";
    public static final String F_JK_APP_ID = "appId";
    public static final String F_JK_WIDGET_ID = "widgetId";
    public static final String F_JK_VERSION = "version";
    public static final String F_JK_RESULT = "result";
    public static final String F_JK_VALUE = "value";
    public static final String F_JK_URL = "url";
    public static final String F_JK_SIZE = "size";

    public static final String F_JK_YEAR = "year";
    public static final String F_JK_MONTH = "month";
    public static final String F_JK_DAY = "day";
    public static final String F_JK_HOUR = "hour";
    public static final String F_JK_MINUTE = "minute";

    public static final String F_JK_MULTIPOP_STATE = "multiPopState";
    public static final String F_JK_MULTIPOP_MOVE_PERCENT = "multiPopMovePercent";
    public static final String F_JK_MULTIPOP_MOVE_PX = "multiPopMovePx";
    public static final String F_JK_MULTIPOP_NAME = "multiPopName";
    public static final String F_JK_MULTIPOP_INDEX = "multiPopSelectedIndex";


    // jv
    public static final int F_JV_UPDATE = 0;
    public static final int F_JV_NO_UPDATE = 1;
    public static final int F_JV_ERROR = 2;
    public static final int F_JV_NO_REGIST = 3;

    public static final int F_JV_IOS = 0;
    public static final int F_JV_ANDROID = 1;
    public static final int F_JV_CHROME = 2;

    // constant
    public static final int F_C_TEXT = 0;
    public static final int F_C_JSON = 1;
    public static final int F_C_INT = 2;

    public static final int F_C_CPU = 0;
    public static final int F_C_OS = 1;
    public static final int F_C_MANUFACTURER = 2;
    public static final int F_C_KEYBOARD = 3;
    public static final int F_C_BLUETOOTH = 4;
    public static final int F_C_WIFI = 5;
    public static final int F_C_CAMERA = 6;
    public static final int F_C_GPS = 7;
    public static final int F_C_GPRS = 8;
    public static final int F_C_TOUCH = 9;
    public static final int F_C_IMEI = 10;
    public static final int F_C_DEVICE_TOKEN = 11;
    public static final int F_C_CONNECT_STATUS = 13;
    public static final int F_C_REST_DISK_SIZE = 14;
    public static final int F_C_MOBILE_OPERATOR_NAME = 15;
    public static final int F_C_MAC_ADDRESS = 16;
    public static final int F_C_MODEL = 17;

    public static final int F_C_NEW = 1;
    public static final int F_C_READ = 2;
    public static final int F_C_WRITE = 4;

    public static final int F_C_APPEND = 1;

    public static final int F_C_TRUE = 1;
    public static final int F_C_FALSE = 0;

    public static final int F_C_SUCCESS = 0;
    public static final int F_C_FAILED = 1;
    public static final int F_C_NOT_EXIST = 2;

    public static final int F_C_PAYSUCCSS = 0;
    public static final int F_C_PAYING = 1;
    public static final int F_C_PAYFAILED = 2;
    public static final int F_C_PayPLUGINERROR = 3;

    public static final int F_C_UpLoading = 0;
    public static final int F_C_FinishUpLoad = 1;
    public static final int F_C_UpLoadError = 2;
    public static final int F_C_DownLoading = 0;
    public static final int F_C_FinishDownLoad = 1;
    public static final int F_C_DownLoadError = 2;
    public static final int F_C_CB_CancelDownLoad = 3;

    public static final int F_C_Key_Back = 0;
    public static final int F_C_Key_Menu = 1;

    public static final int F_C_File = 0;
    public static final int F_C_Folder = 1;

    public static final int F_E_UEXDOWNLOAD_DOWNLOAD_1 = 1070201;
    public static final int F_E_UEXFILEMGR_CREATEFILE_6 = 1090106;
    public static final int F_E_UEXFILEMGR_CREATEFILE_1 = 1090101;
    public static final int F_E_UEXFILEMGR_createDir_1 = 1090201;
    public static final int F_E_UEXFILEMGR_OPENFILE_2 = 1090302;
    public static final int F_E_UEXFILEMGR_OPENFILE_1 = 1090301;
    public static final int F_E_UEXFILEMGR_DELETEFILEBYPATH_1 = 1090401;
    public static final int F_E_UEXFILEMGR_DELETEFILEBYPATH_2 = 1090402;
    public static final int F_E_UEXFILEMGR_DELETEFILEBYID_1 = 1090501;
    public static final int F_E_UEXFILEMGR_DELETEFILEBYID_2 = 1090502;
    public static final int F_E_UEXFILEMGR_ISFILEEXISTBYPATH_1 = 1090801;
    public static final int F_E_UEXFILEMGR_ISFILEEXISTBYID_1 = 1090901;
    public static final int F_E_UEXFILEMGR_GETFILETYPEBYPATH_1 = 1090601;
    public static final int F_E_UEXFILEMGR_GETFILETYPEBYID_1 = 1090701;
    public static final int F_E_UEXFILEMGR_GETFILETYPEBYID_2 = 1090702;
    public static final int F_E_UEXFILEMGR_EXPLORER_2 = 1091002;
    public static final int F_E_UEXFILEMGR_EXPLORER_6 = 1091006;

    public static final int F_E_UEXFILEMGR_SEEKFILE_1 = 1091101;
    public static final int F_E_UEXFILEMGR_SEEKBEGINOFFILE_1 = 1091201;
    public static final int F_E_UEXFILEMGR_SEEKENDOFFILE_1 = 1091301;
    public static final int F_E_UEXFILEMGR_WRITEFILE_1 = 1091401;
    public static final int F_E_UEXFILEMGR_READFILE_1 = 1091501;
    public static final int F_E_UEXFILEMGR_GETFILESIZE_1 = 1091601;
    public static final int F_E_UEXFILEMGR_GETFILEPATH_1 = 1091701;
    public static final int F_E_UEXFILEMGR_CLOSEFILE_1 = 1091801;
    public static final int F_E_UEXFILEMGR_GETREADEROFFSET_1 = 1091901;
    public static final int F_E_UEXJABBER_SENDFILE_2 = 1110302;
    public static final int F_E_UEXJABBER_RECEIVEFILE_1 = 1110401;

    public static final int F_E_UEXCAMERA_OPEN = 1030106; // uexCamera对象，open存储设备错误
    public static final int F_E_UEXlOCATION_GETADDRESS = 1120111; // uexLocation对象，getAddress网络错误
    public static final int F_E_UEXSENSOR_OPEN = 1170101; // uexSensor对象，open参数错误
    public static final int F_E_UEXSENSOR_CLOSE = 1170201; // uexSensor对象，close参数错误
    public static final int F_E_UEXWINDOW_OPEN = 1240101; // uexWindow对象，open参数错误
    public static final int F_E_UEXWINDOW_OPENS = 1240201; // uexWindow对象，openSlibing参数错误
    public static final int F_E_UEXWINDOW_SHOWS = 1240301; // uexWindow对象，showSlibing参数错误
    public static final int F_E_UEXWINDOW_CLOSE = 1240401; // uexWindow对象，close参数错误
    public static final int F_E_UEXWINDOW_CLOSES = 1240501; // uexWindow对象，closeSlibing参数错误
    public static final int F_E_UEXWINDOW_EVAL = 1240601; // uexWindow对象，evaluateScript参数错误
    public static final int F_E_UEXWINDOW_SETKEY = 1240701; // uexWindow对象，setReportKey参数错误
    public static final int F_E_UEXWINDOW_WBACK = 1240801; // uexWindow对象，windowBack参数错误
    public static final int F_E_UEXWINDOW_WFORWARD = 1240901; // uexWindow对象，windowForward参数错误
    public static final int F_E_UEXWINDOW_TOAST = 1241001; // uexWindow对象，toast参数错误
    public static final int F_E_UEXXMLHTTP_OPEN = 1250101; // uexXmlHttpMgr对象，open参数错误
    public static final int F_ERROR_CODE_EMAIL_OPEN_ARGUMENTS_ERROR = 1080101;// 邮件附件路径参数错误
    public static final int F_ERROR_CODE_EMAIL_OPEN_FILE_NOT_EXIST = 1080102;// 邮件附件不存在
    public static final int F_ERROR_CODE_IMAGE_BROWSER_OPEN_ARGUMENT_ERROR = 1100201;// 图片链接参数错误
    public static final int F_ERROR_CODE_MMS_SEND_ARGUMENT_ERROR = 1140101;// 彩信发送附件路径参数错误
    public static final int F_ERROR_CODE_MMS_SEND_FILE_NOT_EXIST = 1140102;// 彩信发送附件文件不存在
    public static final int F_ERROR_CODE_SMS_SEND_ARGUMENTS_ERROR = 1180201;// 短信发送参数错误
    public static final int F_ERROR_CODE_VIDEO_OPEN_ARGUMENTS_ERROR = 1210101;// 播放视频文件路径参数错误

    public static final int F_ERROR_CODE_IMAGE_BROWSER_SAVE_ARGUMENTS_ERROR = 1210301;// 保存图片文件路径参数错误

    //*****EUExAudio*****
    public static final int F_E_AUDIO_MUSIC_OPEN_PARAMETER_ERROR_CODE = 1010101;//参数错误
    public static final int F_E_AUDIO_MUSIC_OPEN_NO_OPEN_ERROR_CODE = 1010104;//文件未打开错误

    public static final int F_E_AUDIO_MUSIC_PLAY_NO_OPEN_ERROR_CODE = 1010204;//文件未打开错误

    public static final int F_E_AUDIO_MUSIC_PAUSE_NO_OPEN_ERROR_CODE = 1010304;//文件未打开错误
    public static final int F_E_AUDIO_MUSIC_STOP_NO_OPEN_ERROR_CODE = 1010404;//文件未打开错误

    public static final int F_E_AUDIO_VOLUMEUP_NO_OPEN_ERROR_CODE = 1010504;//文件未打开错误
    public static final int F_E_AUDIO_VOLUMEDOWN_NO_OPEN_ERROR_CODE = 1010604;//文件未打开错误

    public static final int F_E_AUDIO_SOUND_OPENS_PARAMETER_ERROR_CODE = 1010701;//参数错误

    public static final int F_E_AUDIO_RECORD_NONSUPPORT_ERROR_CODE = 1010808;//设备不支持错误


    public static final int F_E_AUDIO_SOUND_PLAY_PARAMETER_ERROR_CODE = 1011001;//参数错误
    public static final int F_E_AUDIO_SOUND_PLAY_NO_OPEN_ERROR_CODE = 1011004;//文件未打开错误

    public static final int F_E_AUDIO_SOUND_STOP_PARAMETER_ERROR_CODE = 1011101;//参数错误
    public static final int F_E_AUDIO_SOUND_STOP_NO_OPEN_ERROR_CODE = 1011104;//文件未打开错误


}
