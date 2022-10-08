package lc.kristen.sqlite.runnable;

import java.util.ArrayList;
import java.util.Date;

import lc.kristen.sqlite.FileConfiguration;

public class HealthMonitor implements Runnable {

	public static volatile ArrayList<String> lockedSqlDatabaseFiles = new ArrayList<>();

	private static volatile boolean started = false;

	public static void start() {
		if (started == false) {
			Thread thread = new Thread(new HealthMonitor());
			thread.setName(HealthMonitor.class + "");
			thread.start();
		}
	}

	@Override
	public void run() {
		started = true;
		while (true) {
			try {
				System.out.println("------------ HEALTH REPORT ------------");
				for (String fn : lockedSqlDatabaseFiles) {
					System.out.println(fn);
				}
				System.out.println("locked sql database files");
				System.out.println(new Date());
				System.out.println(FileConfiguration.class);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					break;
				}
			} catch (Exception e) {
				break;
			}
		}
	}

}
