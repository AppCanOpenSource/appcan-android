package org.zywx.wbpalmstar.base.zip;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.ZipException;

public class InflaterInputStream extends FilterInputStream {
    protected Inflater inf;
    protected byte[] buf;
    protected int len;
    private boolean closed = false;

    private boolean reachEOF = false;

    boolean usesDefaultInflater = false;

    private byte[] singleByteBuf = new byte[1];

    private byte[] b = new byte[512];

    private void ensureOpen() throws IOException {
        if (this.closed)
            throw new IOException("Stream closed");
    }

    public InflaterInputStream(InputStream in, Inflater inf, int size) {
        super(in);
        if ((in == null) || (inf == null))
            throw new NullPointerException();
        if (size <= 0) {
            throw new IllegalArgumentException("buffer size <= 0");
        }
        this.inf = inf;
        this.buf = new byte[size];
    }

    public InflaterInputStream(InputStream in, Inflater inf) {
        this(in, inf, 512);
    }

    public InflaterInputStream(InputStream in) {
        this(in, new Inflater());
        this.usesDefaultInflater = true;
    }

    public int read() throws IOException {
        ensureOpen();
        return read(this.singleByteBuf, 0, 1) == -1 ? -1
                : this.singleByteBuf[0] & 0xFF;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if ((off | len | off + len | b.length - (off + len)) < 0)
            throw new IndexOutOfBoundsException();
        if (len == 0)
            return 0;
        try {
            int n;
            do {
                // int n;
                if ((this.inf.finished()) || (this.inf.needsDictionary())) {
                    this.reachEOF = true;
                    return -1;
                }
                if (this.inf.needsInput())
                    fill();
            } while ((n = this.inf.inflate(b, off, len)) == 0);

            return n;
        } catch (DataFormatException e) {
//			String s = e.getMessage();
            // if (s != null) tmpTernaryOp = s;
        }
        throw new ZipException("Invalid ZLIB data format");
    }

    public int available() throws IOException {
        ensureOpen();
        if (this.reachEOF) {
            return 0;
        }
        return 1;
    }

    public long skip(long n) throws IOException {
        if (n < 0L) {
            throw new IllegalArgumentException("negative skip length");
        }
        ensureOpen();
        int max = (int) Math.min(n, 2147483647L);
        int total = 0;
        while (total < max) {
            int len = max - total;
            if (len > this.b.length) {
                len = this.b.length;
            }
            len = read(this.b, 0, len);
            if (len == -1) {
                this.reachEOF = true;
                break;
            }
            total += len;
        }
        return total;
    }

    public void close() throws IOException {
        if (!this.closed) {
            if (this.usesDefaultInflater)
                this.inf.end();
            this.in.close();
            this.closed = true;
        }
    }

    protected void fill() throws IOException {
        ensureOpen();
        this.len = this.in.read(this.buf, 0, this.buf.length);
        if (this.len == -1) {
            throw new EOFException("Unexpected end of ZLIB input stream");
        }
        this.inf.setInput(this.buf, 0, this.len);
    }
}