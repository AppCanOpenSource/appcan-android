package org.zywx.wbpalmstar.base.zip;

import java.util.Date;

public class ZipEntry
        implements ZipConstants, Cloneable {
    String name;
    long time = -1L;
    long crc = -1L;
    long size = -1L;
    long csize = -1L;
    int method = -1;
    byte[] extra;
    String comment;
    int flag;
    int version;
    long offset;
    public static final int STORED = 0;
    public static final int DEFLATED = 8;

    private static native void initIDs();

    public ZipEntry(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (name.length() > 65535) {
            throw new IllegalArgumentException("entry name too long");
        }
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setTime(long time) {
        this.time = javaToDosTime(time);
    }

    public long getTime() {
        return this.time != -1L ? dosToJavaTime(this.time) : -1L;
    }

    public void setSize(long size) {
        if ((size < 0L) || (size > 4294967295L)) {
            throw new IllegalArgumentException("invalid entry size");
        }
        this.size = size;
    }

    public long getSize() {
        return this.size;
    }

    public long getCompressedSize() {
        return this.csize;
    }

    public void setCompressedSize(long csize) {
        this.csize = csize;
    }

    public void setCrc(long crc) {
        if ((crc < 0L) || (crc > 4294967295L)) {
            throw new IllegalArgumentException("invalid entry crc-32");
        }
        this.crc = crc;
    }

    public long getCrc() {
        return this.crc;
    }

    public void setMethod(int method) {
        if ((method != 0) && (method != 8)) {
            throw new IllegalArgumentException("invalid compression method");
        }
        this.method = method;
    }

    public int getMethod() {
        return this.method;
    }

    public void setExtra(byte[] extra) {
        if ((extra != null) && (extra.length > 65535)) {
            throw new IllegalArgumentException("invalid extra field length");
        }
        this.extra = extra;
    }

    public byte[] getExtra() {
        return this.extra;
    }

    public void setComment(String comment) {
        if ((comment != null) && (comment.length() > 21845) &&
                (CnZipOutputStream.getUTF8Length(comment) > 65535)) {
            throw new IllegalArgumentException("invalid entry comment length");
        }
        this.comment = comment;
    }

    public String getComment() {
        return this.comment;
    }

    public boolean isDirectory() {
        return this.name.endsWith("/");
    }

    public String toString() {
        return getName();
    }

    private static long dosToJavaTime(long dtime) {
        Date d = new Date((int) ((dtime >> 25 & 0x7F) + 80L),
                (int) ((dtime >> 21 & 0xF) - 1L),
                (int) (dtime >> 16 & 0x1F),
                (int) (dtime >> 11 & 0x1F),
                (int) (dtime >> 5 & 0x3F),
                (int) (dtime << 1 & 0x3E));
        return d.getTime();
    }

    private static long javaToDosTime(long time) {
        Date d = new Date(time);
        int year = d.getYear() + 1900;
        if (year < 1980) {
            return 2162688L;
        }
        return year - 1980 << 25 | d.getMonth() + 1 << 21 |
                d.getDate() << 16 | d.getHours() << 11 | d.getMinutes() << 5 |
                d.getSeconds() >> 1;
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    public Object clone() {
        try {
            ZipEntry e = (ZipEntry) super.clone();
            e.extra = (this.extra == null ? null : (byte[]) this.extra.clone());
            return e;
        } catch (CloneNotSupportedException e) {
        }
        throw new InternalError();
    }
}