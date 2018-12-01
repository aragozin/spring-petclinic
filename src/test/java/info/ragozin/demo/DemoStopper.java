package info.ragozin.demo;

import org.junit.Test;

public class DemoStopper {

	@Test
	public void stop() {
		System.out.println("Stopping Tomcat ...");
		ProcessWatchDog.kill(DemoInitializer.file("pids/tomcat.lg"));
		System.out.println("Stopping HSQL ...");
		ProcessWatchDog.kill(DemoInitializer.file("pids/hsqldb.lg"));
		System.out.println("Demo stopped");
	}	
}
