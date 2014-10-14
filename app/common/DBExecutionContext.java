package common;

import play.libs.Akka;
import scala.concurrent.ExecutionContext;

public class DBExecutionContext {
	private final static ExecutionContext ctx = Akka.system().dispatchers().lookup("akka.db-dispatcher");

	public static ExecutionContext getCtx() {
		return ctx;
	}
}
