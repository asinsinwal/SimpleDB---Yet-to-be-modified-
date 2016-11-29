package simpledb.test;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.junit.Test;

import junit.framework.TestCase;
import simpledb.remote.RemoteDriverImpl;
import simpledb.server.SimpleDB;

/**
 * @author Sumit
 *
 */
public class TestBuffer extends TestCase {

	private Registry reg;
	private static final int DEFAULT_PORT = 1099;
	private static final String BINDING_NAME = "simpledb";

	/**
	 * setting up resources
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		SimpleDB.init("simpleDB");
		// create a registry specific for the server on the default port
		this.reg = LocateRegistry.createRegistry(DEFAULT_PORT);
		// and post the server entry in it
		this.reg.rebind(BINDING_NAME, new RemoteDriverImpl());
		System.out.println("database server ready");
	}

	/**
	 * releasing resources
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		this.reg.unbind(BINDING_NAME);
		this.reg = null;
	}

	@Test
	public void testScenario1() {

	}
}
