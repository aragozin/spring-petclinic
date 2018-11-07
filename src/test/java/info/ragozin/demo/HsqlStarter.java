package info.ragozin.demo;

import static info.ragozin.demo.DemoInitializer.file;
import static info.ragozin.demo.DemoInitializer.initLifeGrant;
import static info.ragozin.demo.DemoInitializer.kill;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.hsqldb.Server;
import org.hsqldb.persist.HsqlProperties;

public class HsqlStarter {

    protected HsqlProperties props;
    protected Server server;
    protected Thread serverThread;
	
    public static void stop() {
    		kill("stop");
    }
    
    public static void start() {
		try {	
			kill("hsqldb");
			String java = System.getProperty("java.home");
			File javaBin = new File(new File(java, "bin"), "java");
			
			String cp = ManagementFactory.getRuntimeMXBean().getClassPath();
			
			List<String> cmd = new ArrayList<String>();
			cmd.add(javaBin.getPath());
			initJvmArgs(cmd);
			cmd.add("-cp");
			cmd.add(cp);
			boolean jettyStarter = false;
			if (jettyStarter) {
				cmd.add("org.eclipse.jetty.start.Main");
				cmd.add("--help");
			}			
			else { 
				cmd.add(HsqlStarter.class.getName());
			}
			
			file("var/hsqldb/logs").mkdirs();
			
			ProcessBuilder pb = new ProcessBuilder(cmd.toArray(new String[0]));
			pb.directory(file("var/hsqldb"));
			pb.redirectOutput(Redirect.to(file("var/hsqldb/logs/console.out")));
			pb.redirectError(Redirect.to(file("var/hsqldb/logs/console.err")));
			if (pb.start().waitFor(10, TimeUnit.SECONDS)) {
				throw new RuntimeException("Failed to start");
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}    	
    }

    public static boolean isRunning() {
        boolean isRunning = false;
        final int port = 9001;
        final String url = "jdbc:hsqldb:hsql://127.0.0.1:" + port + "/petclinic";
        final String username = "SA";
        final String password = "";
        
        Connection conn = null;
        Socket sock = null;
        try {
        	conn = DriverManager.getConnection(url, username, password);
            isRunning = true;
        } catch (SQLException e) {
            try {
                // see if the port is being used already (by something other than HSQL)
            	sock = new Socket(InetAddress.getByName(null), port);
                System.err.println("Port," + port + ", is already in use but not by HSQL. "
                          + "To find out the ID of the process using that port, open a terminal. Then, "
                          + "if on Mac OS or Linux, use `lsof -i :" + port + "`, "
                          + "or, if on Windows, use `netstat -ano | findstr " + port + "`.");
                isRunning = true;
            } catch (Exception ignored) {
                // otherwise, it's not in use, yet
            }
        }
        finally {
        	if (conn != null) {
        		try {
        			conn.close();
        		}
        		catch(Exception e) {
        			// ignore
        		}
        	}
        	if (sock != null) {
        		try {
        			sock.close();
        		}
        		catch(Exception e) {
        			// ignore
        		}
        	}
        }
        
        return isRunning;
    }
    
	private static void initJvmArgs(List<String> cmd) {
		cmd.addAll(Arrays.asList("-server", 
		"-Xss256k", "-Xms256m", "-Xmx256m",
		"-Duser.timezone=UTC",
		"-XX:NewRatio=3", "-XX:SurvivorRatio=4", "-XX:TargetSurvivorRatio=90",
		"-XX:MaxTenuringThreshold=8", "-XX:+UseConcMarkSweepGC", "-XX:+UseParNewGC",
		"-XX:ConcGCThreads=4", "-XX:ParallelGCThreads=4", "-XX:+CMSScavengeBeforeRemark",
		"-XX:PretenureSizeThreshold=64m", "-XX:+UseCMSInitiatingOccupancyOnly", 
		"-XX:CMSInitiatingOccupancyFraction=50", "-XX:CMSMaxAbortablePrecleanTime=6000",
		"-XX:+CMSParallelRemarkEnabled", "-XX:+ParallelRefProcEnabled",
		"-verbose:gc", "-XX:+PrintHeapAtGC", "-XX:+PrintGCDetails", "-XX:+PrintGCDateStamps",
		"-XX:+PrintGCTimeStamps", "-XX:+PrintTenuringDistribution", "-XX:+PrintGCApplicationStoppedTime"));
		
		cmd.add("-Xloggc:logs/hsql_gc.log");			
	}
    
    public static void main(String[] args) {
    	initLifeGrant("hsqldb");
    	new HsqlStarter().startServer();
    }
    
    public HsqlStarter() {
        Properties databaseConfig = new Properties();
        databaseConfig.setProperty("server.database.0", "file:" + DemoInitializer.path("var/hsqldb/petclinic"));
        databaseConfig.setProperty("server.dbname.0", "petclinic");
        databaseConfig.setProperty("server.remote_open", "true");
        databaseConfig.setProperty("hsqldb.reconfig_logging", "false");
        databaseConfig.setProperty("server.port", "9001");
        
        this.props = new HsqlProperties(databaseConfig);
	}
    
	private void startServer() {
        server = new Server();
        
        try {
            server.setProperties(props);
            serverThread = new Thread(new Runnable() {
                
                @Override
                public void run() {
                    server.start();
                }
            }, "HSQLDB Background Thread");
            serverThread.setDaemon(false);
            serverThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
