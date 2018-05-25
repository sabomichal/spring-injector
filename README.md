[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.sabomichal/spring-injector/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.sabomichal/spring-injector)
# spring-injector
*A fast alternative to the Spring [@Configurable](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/aop.html#aop-using-aspectj) magic.*

Do you need to inject spring beans into non managed e.g. domain objects? Are you tired of slow aspectj compilation or load-time weaving? Then spring-injector is the solution.

This project is based on the [Spring Injector](https://ci.apache.org/projects/wicket/apidocs/8.x/org/apache/wicket/spring/injection/annot/SpringComponentInjector.html) from [Apache Wicket](http://wicket.apache.org/) project. 

## Usage
In order to inject dependents into any object just call `SpringInjector.get().inject(this)`. And that's it!

```java
public class DependentComponent implements Serializable {
	private static final long serialVersionUID = 1L;

	@Inject
	private InjectedComponent injectedComponent;

	public DependentComponent() {
		SpringInjector.get().inject(this);
	}

	public int answer() {
		return injectedComponent.answer();
	}
}
```

See the test classes for more detailed use case.
