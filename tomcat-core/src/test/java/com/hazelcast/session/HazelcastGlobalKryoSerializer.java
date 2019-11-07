package com.hazelcast.session;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.InputChunked;
import com.esotericsoftware.kryo.io.OutputChunked;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HazelcastGlobalKryoSerializer
        implements StreamSerializer {

    private final ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.register(CustomAttribute.class);
            return kryo;
        }
    };

    @Override
    public int getTypeId() {
        return 123;
    }

    @Override
    public void write(ObjectDataOutput objectDataOutput, Object object)
            throws IOException {
        OutputChunked output = new OutputChunked((OutputStream) objectDataOutput, 4096);
        kryoThreadLocal.get().writeClassAndObject(output, object);
        output.endChunk();
        output.flush();
    }

    @Override
    public Object read(ObjectDataInput objectDataInput)
            throws IOException {
        InputStream in = (InputStream) objectDataInput;
        InputChunked input = new InputChunked(in, 4096);
        return kryoThreadLocal.get().readClassAndObject(input);
    }

    @Override
    public void destroy() {
    }

}