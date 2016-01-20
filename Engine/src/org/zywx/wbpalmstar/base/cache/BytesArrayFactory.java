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

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

/**
 * BytesArrayFactory设计为单例模式，统一管理BytesArray的创建和回收，实现数组的重复利用，减少频繁创建数组的行为，
 * 从而减少gc触发的频率改善GUI卡顿的現象提高程序性能
 */
public class BytesArrayFactory {

    /**
     * BytesArray类简单封装了对字节数组的操作,可提供基于IO流的读写操作, BytesArray设计目的主要是为了读写大段字节数据，
     * 減少流输入流与输出流之间的转换和流与字节数组之间的转换造成内存开销
     * BytesArray的读写操作都是针对同一个字节数组的行为，所以为了维护数据在某种操作下的一致性，
     * 不能同时进行读写操作，可以写完了再读或者读完了再写
     */
    public static class BytesArray extends ByteArrayOutputStream {

        /**
         * 默认初始容量4K
         */
        public BytesArray() {
            super(4096);
        }

        /**
         * 指定初始容量
         *
         * @param capacity 字节数
         */
        public BytesArray(int capacity) {
            super(capacity);
        }

        /**
         * 通过现有数组来构建BytesArray
         *
         * @param buffer 数组
         * @param length 数组写入数据的长度
         */
        public BytesArray(byte[] buffer, int length) {
            super(0);
            if (buffer == null) {
                throw new NullPointerException("NullPointer");
            }
            if (length > buffer.length) {
                throw new IllegalArgumentException("Length can't large than array's length");
            }
            buf = buffer;
            count = length;
        }

        /**
         * 获得BytesArray内置的buffer数组
         *
         * @return
         */
        public byte[] getData() {
            return buf;
        }

        public void setCount(int count) {
            if (count < 0 || count > buf.length) {
                throw new IllegalArgumentException("count can't less than zero or large than " + buf.length);
            }
            this.count = count;
        }

        public int capacity() {
            return buf.length;
        }

        public int offset() {
            return 0;
        }

        /**
         * 确保数组的初始容量
         *
         * @param size
         */
        public void ensureCapacity(int size) {
            if (size <= buf.length) {
                return;
            }
            byte[] newbuf = new byte[size];
            System.arraycopy(buf, 0, newbuf, 0, count);
            buf = newbuf;
        }

        /**
         * 转换为输入流，提供对BytesArray的读操作
         *
         * @return
         */
        public ByteArrayInputStream asInputStream() {
            return new ByteArrayInputStream(buf, 0, count);

        }

        public boolean readInputStream(InputStream is, int offset, int length) {
            if (is == null) {
                throw new NullPointerException("NullPointer!");
            }
            if (offset < 0 || length < 0) {
                throw new IllegalArgumentException("skip and length error!");
            }
            boolean isRead = false;
            try {
                is.skip(offset);
                int readCount = is.read(buf, 0, length);
                if (readCount == length) {
                    isRead = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return isRead;
        }
    }

    private static BytesArrayFactory factory;
    private LinkedList<BytesArray> linkedList;
    private int maxSize;

    private BytesArrayFactory(int maxSize) {
        this.maxSize = maxSize;
        linkedList = new LinkedList<BytesArray>();
    }

    public static BytesArrayFactory getInstance(int maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException(" BytesArrayFactory size can't less than zero!");
        }
        if (factory == null) {
            synchronized (BytesArrayFactory.class) {
                if (factory == null) {
                    factory = new BytesArrayFactory(maxSize);
                }
            }
        }
        return factory;
    }

    public static BytesArrayFactory getDefaultInstance() {
        return getInstance(10);
    }

    /**
     * 从BytesArrayFactory获得一个BytesArray对象
     *
     * @return
     */
    public synchronized BytesArray requestBytesArray() {
        BytesArray array = null;
        if (linkedList.size() > 0) {
            array = linkedList.removeFirst();
        } else {
            array = new BytesArray();
        }
        return array;
    }

    /**
     * 从BytesArrayFactory获得一个指定初始大小的BytesArray对象
     *
     * @param size BytesArray的字节容量
     * @return
     */
    public synchronized BytesArray requestBytesArray(int size) {
        BytesArray array = null;
        if (linkedList.size() > 0) {
            array = linkedList.removeFirst();
            array.ensureCapacity(size);
        } else {
            array = new BytesArray(size);
        }
        return array;
    }

    /**
     * 释放使用完毕的BytesArray对象
     *
     * @param array
     */
    public synchronized void releaseBytesArray(BytesArray array) {
        if (array != null) {
            if (linkedList.size() < maxSize) {
                linkedList.addLast(array);
                array.reset();
            }
        }
    }

    public synchronized int computeSize() {
        int size = 0;
        for (BytesArray array : linkedList) {
            size += array.capacity();
        }
        Log.d("BytesArrayManager ", "itemSize:" + linkedList.size() + "  totalSize:" + size / 1024 + "KB");
        return size;
    }

}
