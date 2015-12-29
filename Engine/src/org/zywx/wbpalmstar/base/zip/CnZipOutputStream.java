package org.zywx.wbpalmstar.base.zip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipException;

public class CnZipOutputStream extends DeflaterOutputStream implements
        ZipConstants {
    private ZipEntry entry;
    @SuppressWarnings("rawtypes")
    private Vector entries = new Vector();
    @SuppressWarnings("rawtypes")
    private Hashtable names = new Hashtable();
    private CRC32 crc = new CRC32();
    private long written;
    private long locoff = 0L;
    private String comment;
    private int method = 8;
    private boolean finished;
    private boolean closed = false;

    private String encoding = "UTF-8";
    public static final int STORED = 0;
    public static final int DEFLATED = 8;

    private void ensureOpen() throws IOException {
        if (this.closed)
            throw new IOException("Stream closed");
    }

    public CnZipOutputStream(OutputStream out) {
        super(out, new Deflater(-1, true));
        this.usesDefaultDeflater = true;
    }

    public CnZipOutputStream(OutputStream out, String encoding) {
        this(out);
        this.encoding = encoding;
    }

    public void setComment(String comment) {
        if ((comment != null) && (comment.length() > 21845)
                && (getUTF8Length(comment) > 65535)) {
            throw new IllegalArgumentException("ZIP file comment too long.");
        }
        this.comment = comment;
    }

    public void setMethod(int method) {
        if ((method != 8) && (method != 0)) {
            throw new IllegalArgumentException("invalid compression method");
        }
        this.method = method;
    }

    public void setLevel(int level) {
        this.def.setLevel(level);
    }

    @SuppressWarnings("unchecked")
    public void putNextEntry(ZipEntry e) throws IOException {
        ensureOpen();
        if (this.entry != null) {
            closeEntry();
        }
        if (e.time == -1L) {
            e.setTime(System.currentTimeMillis());
        }
        if (e.method == -1) {
            e.method = this.method;
        }
        switch (e.method) {
            case 8:
                if ((e.size == -1L) || (e.csize == -1L) || (e.crc == -1L)) {
                    e.flag = 8;
                } else if ((e.size != -1L) && (e.csize != -1L) && (e.crc != -1L)) {
                    e.flag = 0;
                } else
                    throw new ZipException(
                            "DEFLATED entry missing size, compressed size, or crc-32");

                e.version = 20;
                break;
            case 0:
                if (e.size == -1L)
                    e.size = e.csize;
                else if (e.csize == -1L)
                    e.csize = e.size;
                else if (e.size != e.csize) {
                    throw new ZipException(
                            "STORED entry where compressed != uncompressed size");
                }
                if ((e.size == -1L) || (e.crc == -1L)) {
                    throw new ZipException(
                            "STORED entry missing size, compressed size, or crc-32");
                }
                e.version = 10;
                e.flag = 0;
                break;
            default:
                throw new ZipException("unsupported compression method");
        }
        e.offset = this.written;
        if (this.names.put(e.name, e) != null) {
            throw new ZipException("duplicate entry: " + e.name);
        }
        writeLOC(e);
        this.entries.addElement(e);
        this.entry = e;
    }

    public void closeEntry() throws IOException {
        ensureOpen();
        ZipEntry e = this.entry;
        if (e != null) {
            switch (e.method) {
                case 8:
                    this.def.finish();
                    while (!this.def.finished()) {
                        deflate();
                    }
                    if ((e.flag & 0x8) == 0) {
                        if (e.size != this.def.getTotalIn()) {
                            throw new ZipException("invalid entry size (expected "
                                    + e.size + " but got " + this.def.getTotalIn()
                                    + " bytes)");
                        }
                        if (e.csize != this.def.getTotalOut()) {
                            throw new ZipException(
                                    "invalid entry compressed size (expected "
                                            + e.csize + " but got "
                                            + this.def.getTotalOut() + " bytes)");
                        }
                        if (e.crc != this.crc.getValue())
                            throw new ZipException(
                                    "invalid entry CRC-32 (expected 0x"
                                            + Long.toHexString(e.crc)
                                            + " but got 0x"
                                            + Long.toHexString(this.crc.getValue())
                                            + ")");
                    } else {
                        e.size = this.def.getTotalIn();
                        e.csize = this.def.getTotalOut();
                        e.crc = this.crc.getValue();
                        writeEXT(e);
                    }
                    this.def.reset();
                    this.written += e.csize;
                    break;
                case 0:
                    if (e.size != this.written - this.locoff) {
                        throw new ZipException("invalid entry size (expected "
                                + e.size + " but got "
                                + (this.written - this.locoff) + " bytes)");
                    }
                    if (e.crc == this.crc.getValue())
                        break;
                    throw new ZipException("invalid entry crc-32 (expected 0x"
                            + Long.toHexString(e.crc) + " but got 0x"
                            + Long.toHexString(this.crc.getValue()) + ")");
                default:
                    throw new InternalError("invalid compression method");
            }
            this.crc.reset();
            this.entry = null;
        }
    }

    public synchronized void write(byte[] b, int off, int len)
            throws IOException {
        ensureOpen();
        if ((off < 0) || (len < 0) || (off > b.length - len))
            throw new IndexOutOfBoundsException();
        if (len == 0) {
            return;
        }

        if (this.entry == null) {
            throw new ZipException("no current ZIP entry");
        }
        switch (this.entry.method) {
            case 8:
                super.write(b, off, len);
                break;
            case 0:
                this.written += len;
                if (this.written - this.locoff > this.entry.size) {
                    throw new ZipException(
                            "attempt to write past end of STORED entry");
                }
                this.out.write(b, off, len);
                break;
            default:
                throw new InternalError("invalid compression method");
        }
        this.crc.update(b, off, len);
    }

    @SuppressWarnings("rawtypes")
    public void finish() throws IOException {
        ensureOpen();
        if (this.finished) {
            return;
        }
        if (this.entry != null) {
            closeEntry();
        }
        if (this.entries.size() < 1) {
            throw new ZipException("ZIP file must have at least one entry");
        }

        long off = this.written;
        Enumeration e = this.entries.elements();
        while (e.hasMoreElements()) {
            writeCEN((ZipEntry) e.nextElement());
        }
        writeEND(off, this.written - off);
        this.finished = true;
    }

    public void close() throws IOException {
        if (!this.closed) {
            super.close();
            this.closed = true;
        }
    }

    private void writeLOC(ZipEntry e) throws IOException {
        writeInt(67324752L);
        writeShort(e.version);
        writeShort(e.flag);
        writeShort(e.method);
        writeInt(e.time);
        if ((e.flag & 0x8) == 8) {
            writeInt(0L);
            writeInt(0L);
            writeInt(0L);
        } else {
            writeInt(e.crc);
            writeInt(e.csize);
            writeInt(e.size);
        }

        byte[] nameBytes = (byte[]) null;
        try {
            if (this.encoding.toUpperCase().equals("UTF-8")) {
                nameBytes = getUTF8Bytes(e.name);
            } else
                nameBytes = e.name.getBytes(this.encoding);
        } catch (Exception byteE) {
            nameBytes = getUTF8Bytes(e.name);
        }

        writeShort(nameBytes.length);
        writeShort(e.extra != null ? e.extra.length : 0);
        writeBytes(nameBytes, 0, nameBytes.length);
        if (e.extra != null) {
            writeBytes(e.extra, 0, e.extra.length);
        }
        this.locoff = this.written;
    }

    private void writeEXT(ZipEntry e) throws IOException {
        writeInt(134695760L);
        writeInt(e.crc);
        writeInt(e.csize);
        writeInt(e.size);
    }

    private void writeCEN(ZipEntry e) throws IOException {
        writeInt(33639248L);
        writeShort(e.version);
        writeShort(e.version);
        writeShort(e.flag);
        writeShort(e.method);
        writeInt(e.time);
        writeInt(e.crc);
        writeInt(e.csize);
        writeInt(e.size);

        byte[] nameBytes = (byte[]) null;
        try {
            if (this.encoding.toUpperCase().equals("UTF-8")) {
                nameBytes = getUTF8Bytes(e.name);
            } else
                nameBytes = e.name.getBytes(this.encoding);
        } catch (Exception byteE) {
            nameBytes = getUTF8Bytes(e.name);
        }
        writeShort(nameBytes.length);
        writeShort(e.extra != null ? e.extra.length : 0);
        byte[] commentBytes;
        if (e.comment != null) {
            commentBytes = getUTF8Bytes(e.comment);
            writeShort(commentBytes.length);
        } else {
            commentBytes = (byte[]) null;
            writeShort(0);
        }
        writeShort(0);
        writeShort(0);
        writeInt(0L);
        writeInt(e.offset);
        writeBytes(nameBytes, 0, nameBytes.length);
        if (e.extra != null) {
            writeBytes(e.extra, 0, e.extra.length);
        }
        if (commentBytes != null)
            writeBytes(commentBytes, 0, commentBytes.length);
    }

    private void writeEND(long off, long len) throws IOException {
        writeInt(101010256L);
        writeShort(0);
        writeShort(0);
        writeShort(this.entries.size());
        writeShort(this.entries.size());
        writeInt(len);
        writeInt(off);
        if (this.comment != null) {
            byte[] b = getUTF8Bytes(this.comment);
            writeShort(b.length);
            writeBytes(b, 0, b.length);
        } else {
            writeShort(0);
        }
    }

    private void writeShort(int v) throws IOException {
        OutputStream out = this.out;
        out.write(v >>> 0 & 0xFF);
        out.write(v >>> 8 & 0xFF);
        this.written += 2L;
    }

    private void writeInt(long v) throws IOException {
        OutputStream out = this.out;
        out.write((int) (v >>> 0 & 0xFF));
        out.write((int) (v >>> 8 & 0xFF));
        out.write((int) (v >>> 16 & 0xFF));
        out.write((int) (v >>> 24 & 0xFF));
        this.written += 4L;
    }

    private void writeBytes(byte[] b, int off, int len) throws IOException {
        this.out.write(b, off, len);
        this.written += len;
    }

    static int getUTF8Length(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch <= '')
                count++;
            else if (ch <= '߿')
                count += 2;
            else {
                count += 3;
            }
        }
        return count;
    }

    private static byte[] getUTF8Bytes(String s) {
        char[] c = s.toCharArray();
        int len = c.length;

        int count = 0;
        for (int i = 0; i < len; i++) {
            int ch = c[i];
            if (ch <= 127)
                count++;
            else if (ch <= 2047)
                count += 2;
            else {
                count += 3;
            }
        }

        byte[] b = new byte[count];
        int off = 0;
        for (int i = 0; i < len; i++) {
            int ch = c[i];
            if (ch <= 127) {
                b[(off++)] = (byte) ch;
            } else if (ch <= 2047) {
                b[(off++)] = (byte) (ch >> 6 | 0xC0);
                b[(off++)] = (byte) (ch & 0x3F | 0x80);
            } else {
                b[(off++)] = (byte) (ch >> 12 | 0xE0);
                b[(off++)] = (byte) (ch >> 6 & 0x3F | 0x80);
                b[(off++)] = (byte) (ch & 0x3F | 0x80);
            }
        }
        return b;
    }
}