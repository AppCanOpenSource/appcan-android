package org.zywx.wbpalmstar.platform.encryption;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

public class PEncryption {
    public static final String F_KEY = "982398e4^$%^&%^&%^&%&^$#$#sdfsda90239%^)f8y99e7we98			 "
            + "yhfdsuyf892yr98ghwequifyh879esa6yf83g2ui1rfgtvbiygf			 "
            + "9218374e8923yhr32hjfklsdahfjsadhjkfsadfbhdjkdsahfuy			 "
            + "28973yr^%UBFG%^&*IO^bjk789234y6cxzv98324df96621378*			 "
            + "^&$69879872364327848e^$%^$*(&(&wrtf32fuihewr87ft872";
    private static final String F_ENDSTR = "3G2WIN Safe Guard";
    private static final int[] sk = {0x89, 0x72, 0xaa, 0x4c, 0x0c, 0x56, 0xcf,
            0x61, 0x36, 0x01, 0x0f, 0xab, 0x31, 0x54, 0x8b, 0x49, 0x80, 0xc4,
            0x90, 0x7a, 0x35, 0x09, 0x9d, 0x66, 0x96, 0x1d, 0x3d, 0xf6, 0xb3,
            0x9f, 0x83, 0x9c, 0x55, 0x0b, 0xa3, 0x7b, 0xed, 0x4a, 0x74, 0xa5,
            0x42, 0x4b, 0xa8, 0xea, 0x16, 0xa6, 0xe0, 0x78, 0xa2, 0x25, 0x1c,
            0x47, 0xef, 0x19, 0x70, 0x82, 0x7c, 0xe2, 0xaf, 0x67, 0xdf, 0xa1,
            0xad, 0x1a, 0x9a, 0x8f, 0xf7, 0xdd, 0x33, 0x5c, 0x84, 0xe1, 0x4e,
            0x2a, 0x0a, 0xc3, 0x57, 0xf8, 0xf2, 0x63, 0xbb, 0x11, 0x99, 0x07,
            0x43, 0x79, 0xd6, 0x02, 0x50, 0x41, 0xf5, 0xb7, 0xd7, 0x1e, 0xde,
            0x6e, 0xfa, 0xfc, 0xd0, 0xa7, 0x3f, 0xbc, 0x2c, 0xbe, 0x81, 0xbd,
            0x32, 0x17, 0x0e, 0x7e, 0xf1, 0x92, 0x8c, 0x22, 0xd4, 0x15, 0xb6,
            0x39, 0x30, 0x68, 0x71, 0x87, 0x06, 0x94, 0xa0, 0x14, 0x73, 0xbf,
            0x3a, 0x93, 0x03, 0x21, 0x45, 0xc2, 0x97, 0xc1, 0x2f, 0x75, 0x6f,
            0x27, 0x13, 0xce, 0x2e, 0xc0, 0x26, 0x5b, 0xfd, 0x38, 0x1b, 0x28,
            0x69, 0x2d, 0xb8, 0x12, 0x91, 0xc8, 0xa4, 0x46, 0x62, 0x9b, 0x58,
            0x95, 0x3e, 0xc9, 0x04, 0xd1, 0x0d, 0xe8, 0x6a, 0xd2, 0x8e, 0x2b,
            0x20, 0xb4, 0x5a, 0x10, 0x5f, 0xda, 0x52, 0x76, 0x64, 0xba, 0xe7,
            0x23, 0xdc, 0x8a, 0x60, 0x53, 0xae, 0x9e, 0xc6, 0x37, 0x6d, 0xd9,
            0xb0, 0x88, 0xb9, 0xac, 0xe5, 0xca, 0x00, 0xcc, 0xb1, 0xc5, 0xd3,
            0x40, 0xfe, 0x1f, 0x8d, 0x18, 0xcb, 0x98, 0x6c, 0x08, 0x85, 0xcd,
            0xdb, 0x34, 0xd5, 0x6b, 0xec, 0x29, 0xe9, 0x59, 0x3b, 0xb2, 0xf9,
            0xe4, 0xee, 0xe3, 0x4f, 0xf4, 0xa9, 0xd8, 0x4d, 0xeb, 0xff, 0xb5,
            0x86, 0x5d, 0xf3, 0x5e, 0x65, 0x77, 0x48, 0x51, 0xfb, 0xf0, 0x3c,
            0xe6, 0xc7, 0x24, 0x7d, 0x44, 0x05, 0x7f};

    private static void swap(int[] pInts, int i, int j) {
        int temp;
        temp = pInts[i];
        pInts[i] = pInts[j];
        pInts[j] = temp;
    }

    private static void re_S(int[] S) {
        int i = sk.length;
        System.arraycopy(sk, 0, S, 0, i);
    }

    private static void re_T(int[] T, String key) {
        int i;
        int keylen;
        keylen = key.length();
        byte[] keys = key.getBytes();
        for (i = 0; i < 256; i++) {
            int k = i % keylen;
            T[i] = keys[k];
        }

    }

    private static void re_Sbox(int[] S, int[] T) {
        int i;
        int j = 0;
        for (i = 0; i < 256; i++) {
            j = (j + S[i] + T[i]) % 256;
            swap(S, i, j);
        }
    }

    private static void re_RC4(int[] S, String key) {
        int[] T = new int[256];
        // PBytes T = new PBytes(bytes);
        re_S(S);
        re_T(T, key);
        re_Sbox(S, T);
    }

    private static int RC4(int[] src, int n, int[] dest, String key) {
        int[] S = new int[256];
        // PInts S = new PInts(ints);
        // unsigned char readbuf[1];
        int i, j, t;
        int nIndex = 0;
        re_RC4(S, key);
        i = j = 0;
        while (nIndex < n) {

            i = (i + 1) % 256;
            j = (j + S[i]) % 256;
            swap(S, i, j);
            t = (S[i] + (S[j] % 256)) % 256;
            dest[nIndex] = src[nIndex] ^ S[t];
            nIndex++;
        }
        return n;
    }

    /**
     * 加密解密
     *
     * @param pBuffer 要加密或者 要解密的 字节流
     * @param n       要加密或者 要解密的 字节流的大小
     * @param pKey    加密解密的key
     * @param nKeyLen key 的长度
     * @return 返回加密后或者 解密后的字节流
     */
    public static byte[] os_decrypt(byte[] pBuffer, int n, String pKey) {
        // PBytes pBytes = new PBytes(pBuffer);
        ByteArrayInputStream in = new ByteArrayInputStream(pBuffer);
        int[] newInt = new int[in.available()];
        int[] resInt = new int[in.available()];
        int ch;
        int i = 0;
        while ((ch = in.read()) != -1) {
            newInt[i] = ch;
            i++;
        }
        RC4(newInt, n, resInt, pKey);
        byte[] newData = new byte[n];
        for (int k = 0; k < n; k++) {
            newData[k] = (byte) resInt[k];
        }
        return newData;
    }

    /**
     * 网页解密
     *
     * @param src 要解密的 字节流
     * @param n   要加密或者 要解密的 字节流的大小
     * @return 返回解密后的字节流
     */
    public static byte[] decode(byte[] src) {
        int n = src.length;
        if (stricmp(n, F_ENDSTR, src)) {

            return os_decrypt(src, n - F_ENDSTR.length(), F_KEY);
        }

        return null;
    }

    private static boolean stricmp(int size, String endStr, byte[] inByte) {
        if (size <= endStr.length() || endStr == null || endStr.length() == 0
                || inByte == null || inByte.length == 0) {
            return false;
        }

        try {
            int i = endStr.getBytes("utf-8").length;
            int k = inByte.length;
            byte[] testByte = new byte[i];
            System.arraycopy(inByte, k - i, testByte, 0, i);
            String test = new String(testByte, "utf-8");
            if (endStr.equals(test)) {
                return true;
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
}
