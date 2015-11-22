package com.github.sabomichal.springinjector.test.it;

import junit.framework.TestCase;
import org.junit.Test;
import org.springframework.util.SerializationUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static junit.framework.TestCase.assertEquals;

public class ITInjector extends InjectorTestBase {

	@Test
	public void testAspectJ() {
		TestCase.assertEquals(42, new DependentComponent().answer());
	}

	@Test
	public void testTransient() {
		DependentComponent dc = new DependentComponent();
		assertEquals(42, dc.answerTransient());
		// serialize and deserialize
		dc = (DependentComponent) SerializationUtils.deserialize(SerializationUtils.serialize(dc));
		assertEquals(42, dc.answerTransient());
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testSerializeAndInject() throws Exception {
		DependentComponent dc = new DependentComponent();
		assertEquals(42, dc.answerTransient());

		// serialization
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(dc);
		oos.close();

		// deserialization
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bais);
		dc = (DependentComponent) ois.readObject();
		ois.close();
		assertEquals(42, dc.answerTransient());

	}

}