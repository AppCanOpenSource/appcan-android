package org.zywx.wbpalmstar.base.zip;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;

public class DeflaterOutputStream extends FilterOutputStream {
    protected Deflater def;
    protected byte[] buf;
    private boolean closed = false;

    boolean usesDefaultDeflater = false;

    public DeflaterOutputStream(OutputStream out, Deflater def, int size) {
        super(out);
        if ((out == null) || (def == null))
            throw new NullPointerException();
        if (size <= 0) {
            throw new IllegalArgumentException("buffer size <= 0");
        }
        this.def = def;
        this.buf = new byte[size];
    }

    public DeflaterOutputStream(OutputStream out, Deflater def) {
        this(out, def, 512);
    }

    public DeflaterOutputStream(OutputStream out) {
        this(out, new Deflater());
        this.usesDefaultDeflater = true;
    }

    public void write(int b)
            throws IOException {
        byte[] buf = new byte[1];
        buf[0] = (byte) (b & 0xFF);
        write(buf, 0, 1);
    }

    public void write(byte[] b, int off, int len)
            throws IOException {
        if (this.def.finished()) {
            throw new IOException("write beyond end of stream");
        }
        if ((off | len | off + len | b.length - (off + len)) < 0)
            throw new IndexOutOfBoundsException();
        if (len == 0) {
            return;
        }
        if (!this.def.finished()) {
            this.def.setInput(b, off, len);
            while (!this.def.needsInput())
                deflate();
        }
    }

    public void finish()
            throws IOException {
        if (!this.def.finished()) {
            this.def.finish();
            while (!this.def.finished())
                deflate();
        }
    }

    public void close()
            throws IOException {
        if (!this.closed) {
            finish();
            if (this.usesDefaultDeflater)
                this.def.end();
            this.out.close();
            this.closed = true;
        }
    }

    protected void deflate()
            throws IOException {
        int len = this.def.deflate(this.buf, 0, this.buf.length);
        if (len > 0)
            this.out.write(this.buf, 0, len);
    }
}