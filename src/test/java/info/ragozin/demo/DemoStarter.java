package info.ragozin.demo;

import org.junit.Assert;
import org.junit.Test;


public class DemoStarter {

	@Test
	public void startDemo() throws InterruptedException {
		
		System.out.println("Starting HSQL");
		HsqlStarter.start();
		Assert.assertTrue(HsqlStarter.isRunning());
		
		System.out.println("Starting Tomcat");
		@SuppressWarnings("unused")
		Process proc = TomcatRunner.start();
		System.out.println("Tomcat instance started");
		System.out.println("");
		System.out.println("Remove \"pids\" directory to stop demo enviroment");
		System.out.println("");
		System.out.println("");
	}	
}
