package org.zywx.wbpalmstar.engine.webview;

import android.os.Build;

public class ACEWebConst {

	public static final String DESKTOP_USERAGENT = "Mozilla/5.0 (Macintosh; "
			+ "U; Intel Mac OS X 10_6_3; en-us) AppleWebKit/533.16 (KHTML, "
			+ "like Gecko) Version/5.0 Safari/533.16";
	public static final String IPHONE_USERAGENT = "Mozilla/5.0 (iPhone; U; "
			+ "CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 "
			+ "(KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7";
	public static final String IPAD_USERAGENT = "Mozilla/5.0 (iPad; U; "
			+ "CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 "
			+ "(KHTML, like Gecko) Version/4.0.4 Mobile/7B367 Safari/531.21.10";
	public static final String FROYO_USERAGENT = "Mozilla/5.0 (Linux; U; "
			+ "Android " + Build.VERSION.RELEASE + "; en-us; " + Build.MODEL
			+ " Build/FRF91) AppleWebKit/533.1 "
			+ "(KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";

	public static final String USERAGENT = FROYO_USERAGENT;
	public static final String USERAGENT_APPCAN = " Appcan/3.1";

	public static String USERAGENT_NEW=USERAGENT+USERAGENT_APPCAN;

}
