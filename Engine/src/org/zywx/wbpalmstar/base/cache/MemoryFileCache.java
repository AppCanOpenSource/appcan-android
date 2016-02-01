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

package org.zywx.wbpalmstar.base.cache;

import android.app.ActivityManager;
import android.content.Context;
import android.os.MemoryFile;
import android.util.Log;

import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.cache.BytesArrayFactory.BytesArray;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * android.os.MemoryFile是对Linux ashmem(匿名共享内存 anonymous share memory)的简单封装.
 * 读写MemoryFile相比读写文件系统可大幅提高I/O效率, 所以MemoryFileCache被设计用来存储缓存数据
 */
public class MemoryFileCache {
    public static final String TAG = "MemoryCache";

    /**
     * 封装写入MemoryFile的数据的信息
     */
    public static final class CacheBlock {

        /* 在MemoryFile中的缓存块起始索引 */
        public int startIndex;

        /* 在MemoryFile中的缓存块结束索引 */
        public int endIndex;

        /* 在MemoryFile中的缓存块的写入层次，用来在读取时比对之前写入的数据块是否已经被覆盖写入 */
        public int writeLayer;

        /* Default Constructor */
        public CacheBlock() {

        }

        /**
         * 带参数的构造器
         *
         * @param startIndex 数据块的起始索引
         * @param endIndex   数据块的结束索引
         * @param writeLayer 数据写入的层
         */
        public CacheBlock(int startIndex, int endIndex, int writeLayer) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.writeLayer = writeLayer;
        }

        /**
         * 校验数据的基本正确性，是否处于合理范围
         *
         * @return
         */
        public boolean isCorrect() {
            if (startIndex >= 0 && endIndex > startIndex && writeLayer >= 0) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean equals(Object block) {
            if (this == block) {
                return true;
            }
            if (!(block instanceof CacheBlock)) {
                return false;
            }
            CacheBlock mb = (CacheBlock) block;
            return mb.startIndex == this.startIndex && mb.endIndex == this.endIndex && mb.writeLayer == this.writeLayer;
        }

    }

    /* 主要操作对象 MemoryFile */
    private MemoryFile memoryFile;

    /* 当前写入的层次 */
    private int currentLayer;

    /* 当前写入的位置 */
    private int postion;

    /* MemoryFile是否关闭 */
    private boolean isClosed = false;

    /**
     * 创建指定大小的共享内存区域
     *
     * @param capacity 共享内存大小,单位字节
     * @throws IOException
     */
    public MemoryFileCache(int capacity) throws IOException {
        if (capacity < 0) {
            throw new IllegalArgumentException("MemoryCache init capacity can't less than zero!!!");
        }
        memoryFile = new MemoryFile(String.valueOf(System.currentTimeMillis()), capacity);
        memoryFile.allowPurging(false);// 不允许被清除，防止写入数据丢失
        BDebug.d(TAG, "MemoryFileCache()  capacity:" + memoryFile.length());
    }

    /**
     * 获得共享內存当前可用字节数，如果共享内存已关闭，返回零
     *
     * @return 可用字节数
     */
    public int getAvailableSize() {
        if (isClosed) {
            return 0;
        }
        return memoryFile.length() - postion;
    }

    /**
     * 判断共享内存是否已经被关闭
     *
     * @return
     */
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * 将bytesArray包含的数据写入MemoryFile中
     *
     * @param bytesArray 将被写入的数据
     * @return 包含写入数据信息的CacheBlock对象，用于下次读取该数据
     */
    public synchronized CacheBlock writeData(BytesArray bytesArray) {
        if (bytesArray == null) {
            throw new NullPointerException("NullPointer!");
        }
        if (isClosed) {
            return null;
        }
        CacheBlock cb = null;
        final int length = bytesArray.size();
        try {
            int startIndex = postion;
            if (length > getAvailableSize()) {// 当前层无法完整写入bytesArray所有数据,从头开始写入(覆盖写入)
                postion = startIndex = 0;// 定位到开始
                currentLayer++;// 层数加1
            }
            memoryFile.writeBytes(bytesArray.getData(), 0, postion, length);
            postion += length;
            cb = new CacheBlock(startIndex, postion, currentLayer);
        } catch (IOException e) {
            e.printStackTrace();
            BDebug.e(TAG, "writeData() Exception:" + e.getMessage());
        }
        return cb;
    }

    /**
     * 将ByteArrayInputStream包含的数据写入MemoryFile
     *
     * @param is       包含数据的ByteArrayInputStream
     * @param buffSize 缓存区大小
     * @return 包含写入数据信息的CacheBlock对象，用于下次读取该数据
     */
    public synchronized CacheBlock writeData(ByteArrayInputStream is, int buffSize) {
        long start = System.currentTimeMillis();
        if (is == null) {
            throw new NullPointerException("NullPointer!");
        }
        if (buffSize <= 0) {
            throw new IllegalArgumentException("bufferSize can't not less than zero!");
        }
        if (isClosed) {
            return null;
        }
        int dataLength = is.available();
        if (dataLength > memoryFile.length()) {
            return null;
        }
        CacheBlock cb = null;
        try {
            int startIndex = postion;
            if (dataLength > getAvailableSize()) {// 已经写满,从头开始写入(覆盖写入)
                postion = startIndex = 0;
                currentLayer++;
            }
            byte[] buffer = new byte[buffSize];
            int actualSize = -1;
            while ((actualSize = is.read(buffer)) != -1) {
                memoryFile.writeBytes(buffer, 0, postion, actualSize);
                postion += actualSize;
            }
            cb = new CacheBlock(startIndex, postion, currentLayer);
        } catch (IOException e) {
            e.printStackTrace();
            BDebug.e(TAG, "writeData() ERROR:" + e.getMessage());
        }
        BDebug.d(TAG, "writeData(): costTime:" + (System.currentTimeMillis() - start) + "ms   size:"
                + ((float) dataLength / 1024) + "KB");
        return cb;
    }

    /**
     * 查询CacheBlock包含的数据是否还能读取出来
     *
     * @param block 查询的CacheBlock
     * @return true-->尚未被覆盖写入,可读; false-->已经被覆盖写入,不可读,或者共享内存已关闭.
     */
    public synchronized boolean canReadData(CacheBlock block) {
        if (block == null) {
            throw new NullPointerException("NullPointer!");
        }
        if (!block.isCorrect()) {
            throw new IllegalArgumentException("MemoryBlock info isn't correct!");
        }
        if (isClosed) {
            return false;
        }
        boolean canRead = false;
        final int layerDelta = currentLayer - block.writeLayer;
        final int totalLength = memoryFile.length();
        // "*" 表示写入数据 ; "-" 表示没有写入数据
        if (layerDelta == 0 && block.endIndex < totalLength) {// 没有覆盖写入
            // |********currentLayer******-----|
            canRead = true;
        } else if (layerDelta == 1 && block.startIndex >= postion && block.endIndex < totalLength) {
            // 已经覆盖写入，但currentLayer postion小于startIndex
            // |*********currentLayer************|
            // |************old layer*************readable data*********|
            canRead = true;
        }
        BDebug.d(TAG, "canReadData: " + canRead + "  start:" + block.startIndex + "  end:" + block.endIndex
                + "  layer:" + block.writeLayer);
        return canRead;
    }

    /**
     * 读取MemoryFile里面的CacheBlock,写入到bytesArray中
     *
     * @param block      包含MemoryFile写入数据位置信息的CacheBlock
     * @param bytesArray 被写入数据的BytesArray
     * @return 是否成功被写入
     */
    public synchronized boolean readData(CacheBlock block, BytesArray bytesArray) {
        final long startTime = System.currentTimeMillis();
        if (!canReadData(block)) {
            return false;
        }
        if (bytesArray == null) {
            return false;
        }
        int size = block.endIndex - block.startIndex;
        bytesArray.ensureCapacity(size);
        int readLength = 0;
        try {
//			InputStream is=memoryFile.getInputStream();
//		    bytesArray.readInputStream(is, block.startIndex, size);
//		    is.close();
            readLength = memoryFile.readBytes(bytesArray.getData(), block.startIndex, 0, size);
            bytesArray.setCount(readLength);
        } catch (IOException e) {
            BDebug.e(TAG, "readData() " + e.getMessage());
            e.printStackTrace();
        } finally {
            BDebug.i(
                    TAG,
                    "readData() readSize:" + ((float) readLength / 1024) + "K" + " Start:" + block.startIndex
                            + "  End:" + block.endIndex + "  Layer:" + block.writeLayer + "  costTime:"
                            + (System.currentTimeMillis() - startTime) + "ms");
        }
        return readLength == size ? true : false;
    }


    /**
     * 关闭共享内存
     */
    public synchronized void close() {
        try {
            isClosed = true;
            memoryFile.allowPurging(true);
        } catch (IOException e) {
            Log.e(TAG, "close()  " + e.getMessage());
            e.printStackTrace();
        } finally {
            memoryFile = null;
        }
    }


    /**
     * 共享内存推荐大小
     *
     * @param context 上下文
     * @return 共享内存字节数
     */
    public static int getRecommendMemoryFileSize(Context context) {
        /**
         * 实测不同分辨率机型单个App可用内存大小
         * mdpi(320*480):16MB
         * hdpi(480*800;480*854):32MB
         * xhdpi(540*960;640*960):48MB
         * xhdpi(720*1280):64MB
         */
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final int memoryClass = activityManager.getMemoryClass();
        return memoryClass * 1024 * 1024;
    }


}
