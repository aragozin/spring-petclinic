package info.ragozin.demo;

import static info.ragozin.demo.DemoInitializer.file;
import static info.ragozin.demo.DemoInitializer.initLifeGrant;
import static info.ragozin.demo.DemoInitializer.kill;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.catalina.AccessLog;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.AccessLogAdapter;
import org.apache.catalina.startup.HostConfig;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.valves.AccessLogValve;
import org.apache.catalina.valves.Constants;

public class TomcatRunner {

	public static void stop() {
		kill("tomcat");
	}
	
    public static Process start() {
		try {	
			kill("tomcat");
			String java = System.getProperty("java.home");
			File javaBin = new File(new File(java, "bin"), "java");
			
			String cp = ManagementFactory.getRuntimeMXBean().getClassPath();
			
			List<String> cmd = new ArrayList<String>();
			cmd.add(javaBin.getPath());
			initJvmArgs(cmd);
			cmd.add("-cp");
			cmd.add(cp);
			cmd.add(TomcatRunner.class.getName());
			
			file("var/tomcat").mkdirs();
			
			ProcessBuilder pb = new ProcessBuilder(cmd.toArray(new String[0]));
			pb.directory(file(".").getCanonicalFile());
			pb.redirectOutput(Redirect.to(file("var/tomcat/console.out")));
			pb.redirectError(Redirect.to(file("var/tomcat/console.err")));
			Process process = pb.start();
			if (process.waitFor(10, TimeUnit.SECONDS)) {
				throw new RuntimeException("Failed to start");
			}
			return process;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}    	
    }
    
	private static void initJvmArgs(List<String> cmd) {
		List<String> args = Arrays.asList(DemoInitializer.prop("tomcat.jvm-options", "").split("\\s+"));
		cmd.addAll(args);		
		cmd.add("-Xloggc:" + file("var/tomcat/gc.log").getAbsolutePath());			
	}

	public void initLogging() throws SecurityException, IOException {
		Logger logger = Logger.getLogger("");
		Handler fileHandler = new FileHandler(DemoInitializer.path("var/tomcat/catalina.out"), true);
		fileHandler.setFormatter(new SimpleFormatter());
		fileHandler.setLevel(Level.INFO);
		fileHandler.setEncoding("UTF-8");
		logger.addHandler(fileHandler);
	}
	
    public void launch() throws LifecycleException, SecurityException, IOException {
    	
//    	initLogging();

    	Tomcat tomcat = new Tomcat();
        tomcat.setPort(DemoInitializer.propAsInt("tomcat.port", 9966));
        tomcat.setBaseDir(DemoInitializer.path("var/tomcat"));
        AccessLogValve accessLog = new AccessLogValve();
        accessLog.setPattern(Constants.AccessLog.COMBINED_PATTERN);
        tomcat.getEngine().getPipeline().addValve(accessLog);
        
        Host host = tomcat.getHost();
        host.setDeployOnStartup(true);
        host.setAutoDeploy(true);
        host.addLifecycleListener(new HostConfig());

		Context context = tomcat.addWebapp("/petclinic", new File("src/main/webapp").getAbsolutePath());
        context.setSessionTimeout(1); // reduce session live time to avoid OOM under sustained load

        tomcat.start();
        tomcat.getServer().await();
    }

    public static void main(String[] args) throws Exception {
//    	System.out.println("Starting);
    	initLifeGrant("tomcat");
    	new TomcatRunner().launch();
    }    
}