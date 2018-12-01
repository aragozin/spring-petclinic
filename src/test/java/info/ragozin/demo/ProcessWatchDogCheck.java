package info.ragozin.demo;

import java.io.File;

import org.junit.Test;

public class ProcessWatchDogCheck {

	@SuppressWarnings("unused")
	@Test
	public void start() throws InterruptedException {
		ProcessWatchDog watchdog = new ProcessWatchDog(new File("target/check.lg"));
		while(true) {
			Thread.sleep(1000);
		}
	}

	@Test
	public void check() throws InterruptedException {
		ProcessWatchDog.check(new File("target/check.lg"));
	}

	@Test
	public void kill() throws InterruptedException {
		ProcessWatchDog.kill(new File("target/check.lg"));
	}
	
}
