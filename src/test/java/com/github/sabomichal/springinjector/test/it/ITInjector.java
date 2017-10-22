package com.github.sabomichal.springinjector.test.it;

import com.github.sabomichal.springinjector.test.TestContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.util.SerializationUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={TestContext.class}, loader=AnnotationConfigContextLoader.class)
public class ITInjector {

    private volatile Exception exc;

    @Test
    public void testInjection() {
        Assert.assertEquals(42, new DependentComponent().answer());
    }

    @Test
    public void testTransient() throws InterruptedException {
        DependentComponent dc = new DependentComponent();
        Assert.assertEquals(42, dc.answer());

        // serialize and deserialize
        final byte[] ba = SerializationUtils.serialize(dc);
        Thread es = new Thread(() -> {
            try {
                DependentComponent dc_ = (DependentComponent) SerializationUtils.deserialize(ba);
                Assert.assertEquals(42, dc_.answer());
            } catch (Exception e) {
                exc = e;
            }
        });

        es.start();
        es.join();

        if (exc != null) {
            fail(exc.getMessage());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSerializaciaAInjectovanie() throws Exception {
        DependentComponent dc = new DependentComponent();
        Assert.assertEquals(42, dc.answer());

        // serializacia
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(dc);
        oos.close();

        // deserializacia
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        dc = (DependentComponent) ois.readObject();
        ois.close();
        Assert.assertEquals(42, dc.answer());

    }

}