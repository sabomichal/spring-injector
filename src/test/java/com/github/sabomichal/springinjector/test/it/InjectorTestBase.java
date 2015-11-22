package com.github.sabomichal.springinjector.test.it;

import com.github.sabomichal.springinjector.test.TestContext;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 * @author Martin Kovacik
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={TestContext.class}, loader=AnnotationConfigContextLoader.class)
public class InjectorTestBase {
}