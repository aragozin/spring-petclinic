package info.ragozin.demo;

import org.junit.Test;

public class TomcatRunnerCheck {

	@Test
	public void check() throws InterruptedException {
		
		Runtime.getRuntime().addShutdownHook(new Thread("ShutdownHook") {
			@Override
			public void run() {
				TomcatRunner.stop();
			}
		});		
		TomcatRunner.start().waitFor();
	}	
}
