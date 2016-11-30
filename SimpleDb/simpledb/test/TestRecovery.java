package simpledb.test;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import simpledb.buffer.Buffer;
import simpledb.buffer.BufferMgr;
import simpledb.file.Block;
import simpledb.remote.RemoteDriverImpl;
import simpledb.server.SimpleDB;
import simpledb.tx.recovery.RecoveryMgr;

/**
 * @author Sumit
 */
@SuppressWarnings("static-access")
public class TestRecovery {

	private static Registry reg;
	private static final int DEFAULT_PORT = 1098;
	private static final String BINDING_NAME = "simpledb";

	/**
	 * Setting up the resources to be used by the test cases. This method runs
	 * once before running the test cases.
	 * 
	 * @throws RemoteException
	 *             If some error occurs while binding the registry.
	 */
	@BeforeClass
	public static void setUp() throws RemoteException {
		System.out.println(
				"----------------------------------------Test Recovery Setting up----------------------------------------");
		SimpleDB.init("simpleDB");
		// create a registry specific for the server on the default port
		reg = LocateRegistry.createRegistry(DEFAULT_PORT);
		// and post the server entry in it
		reg.rebind(BINDING_NAME, new RemoteDriverImpl());
		System.out.println(
				"----------------------------------------Test Recovery Set Up done----------------------------------------");
	}

	/**
	 * Releasing the resources to be used by the test cases. This method runs
	 * once after running all the test cases.
	 * 
	 * @throws RemoteException
	 *             If some error occurs while un-binding the registry.
	 * @throws NotBoundException
	 *             If some error occurs while un-binding the registry.
	 */
	@AfterClass
	public static void tearDown() throws RemoteException, NotBoundException {
		System.out.println(
				"----------------------------------------Test Recovery Tearing down----------------------------------------");
		reg.unbind(BINDING_NAME);
		reg = null;
		System.out.println(
				"----------------------------------------Test Recovery Tear down done----------------------------------------");
	}

	/**
	 * <h2>Test Scenario 1:</h2>
	 * <p>
	 * Two transactions:<br>
	 * Tx1 - is setting an Integer value and it is not committed. <br>
	 * Tx2 - is setting an Integer value and it is committed.
	 * </p>
	 * <p>
	 * Now, upon crash, when recover is called, the value set by Tx2 is
	 * recovered since it was committed and the value written by Tx1 was
	 * reverted because it was not committed. This is asserted by the following
	 * assert statements:<br>
	 * <b>&emsp;&emsp;Assert.assertEquals(oldValue, newValue);<br>
	 * </b> <b>&emsp;&emsp;Assert.assertEquals(valueToSet2, newValue2);<br>
	 * </b> Here,<br>
	 * oldValue = old value for Tx1, newValue = Value of Tx1, after recover is
	 * called.<br>
	 * valueToSet2 = value being set by Tx2, newValue2 = Value of Tx2, after
	 * recover is called.<br>
	 * </p>
	 * 
	 */
	@Test
	public void testScenario1() {
		System.out.println("----------Running Recovery Scenario 1----------");
		// Creating a Block.
		Block blk1 = new Block("filename", 1);
		Block blk2 = new Block("filename", 2);

		// Create a RecoveryManager
		int txid = 123;
		int txid2 = 143;
		RecoveryMgr rm = new RecoveryMgr(txid);
		RecoveryMgr rm2 = new RecoveryMgr(txid2);

		// Sample setInt
		BufferMgr basicBufferMgr = new SimpleDB().bufferMgr();
		Buffer buff = basicBufferMgr.pin(blk1);
		Buffer buff2 = basicBufferMgr.pin(blk2);
		int offset = 8;
		int offset2 = 16;
		int oldValue = buff.getInt(offset);
		int oldValue2 = buff2.getInt(offset2);
		System.out.println("Old Value: " + oldValue);
		System.out.println("Old Value2: " + oldValue2);
		int valueToSet = oldValue + 10;
		int valueToSet2 = oldValue2 + 20;
		int lsn = rm.setInt(buff, offset, valueToSet);
		buff.setInt(offset, valueToSet, txid, lsn);
		int lsn2 = rm2.setInt(buff2, offset2, valueToSet2);
		buff2.setInt(offset2, valueToSet2, txid2, lsn2);
		int newValue = buff.getInt(offset);
		System.out.println("New Value without committing: " + newValue);
		int newValue2 = buff2.getInt(offset2);
		System.out.println("New Value2 without committing: " + newValue2);

		rm2.commit();
		rm2.recover();
		newValue = buff.getInt(offset);
		newValue2 = buff2.getInt(offset2);
		System.out.println("New Value after recovering: " + newValue);
		System.out.println("New Value2 after recovering: " + newValue2);
		Assert.assertEquals(oldValue, newValue);
		Assert.assertEquals(valueToSet2, newValue2);
		System.out.println("----------Recovery Scenario 1 Run Complete----------");
	}

	/**
	 * <h2>Test Scenario 2:</h2>
	 * <p>
	 * Two transactions:<br>
	 * Tx1 - is setting an Integer value and it is committed. <br>
	 * Tx2 - is setting an Integer value and it is committed.
	 * </p>
	 * <p>
	 * Now, upon crash, when recover is called, the value set by Tx1 and Tx2
	 * both are recovered since both were committed. This is asserted by the
	 * following assert statements:<br>
	 * <b>&emsp;&emsp;Assert.assertEquals(valueToSet, newValue);<br>
	 * </b> <b>&emsp;&emsp;Assert.assertEquals(valueToSet2, newValue2);<br>
	 * </b> Here,<br>
	 * valueToSet = value being set by Tx1, newValue = Value of Tx1, after
	 * recover is called.<br>
	 * valueToSet2 = value being set by Tx2, newValue2 = Value of Tx2, after
	 * recover is called.<br>
	 * </p>
	 * 
	 */
	@Test
	public void testScenario2() {
		System.out.println("----------Running Recovery Scenario 2----------");
		// Creating a Block.
		Block blk1 = new Block("filename", 1);
		Block blk2 = new Block("filename", 2);

		// Create a RecoveryManager
		int txid = 123;
		int txid2 = 143;
		RecoveryMgr rm = new RecoveryMgr(txid);
		RecoveryMgr rm2 = new RecoveryMgr(txid2);

		// Sample setInt
		BufferMgr basicBufferMgr = new SimpleDB().bufferMgr();
		Buffer buff = basicBufferMgr.pin(blk1);
		Buffer buff2 = basicBufferMgr.pin(blk2);
		int offset = 8;
		int offset2 = 16;
		int oldValue = buff.getInt(offset);
		int oldValue2 = buff2.getInt(offset2);
		System.out.println("Old Value: " + oldValue);
		System.out.println("Old Value2: " + oldValue2);
		int valueToSet = oldValue + 10;
		int valueToSet2 = oldValue2 + 20;
		int lsn = rm.setInt(buff, offset, valueToSet);
		buff.setInt(offset, valueToSet, txid, lsn);
		int lsn2 = rm2.setInt(buff2, offset2, valueToSet2);
		buff2.setInt(offset2, valueToSet2, txid2, lsn2);
		int newValue = buff.getInt(offset);
		System.out.println("New Value without committing: " + newValue);
		int newValue2 = buff2.getInt(offset2);
		System.out.println("New Value2 without committing: " + newValue2);

		rm.commit();
		rm2.commit();
		rm2.recover();
		newValue = buff.getInt(offset);
		newValue2 = buff2.getInt(offset2);
		System.out.println("New Value after recovering: " + newValue);
		System.out.println("New Value2 after recovering: " + newValue2);
		Assert.assertEquals(valueToSet, newValue);
		Assert.assertEquals(valueToSet2, newValue2);
		System.out.println("----------Recovery Scenario 2 Run Complete----------");
	}

	/**
	 * <h2>Test Scenario 3:</h2>
	 * <p>
	 * Two transactions:<br>
	 * Tx1 - is setting an Integer value and it is not committed. <br>
	 * Tx2 - is setting an Integer value and it is not committed.
	 * </p>
	 * <p>
	 * Now, upon crash, when recover is called, the value set by Tx1 and Tx2,
	 * both are reverted since neither was committed. This is asserted by the
	 * following assert statements:<br>
	 * <b>&emsp;&emsp;Assert.assertEquals(oldValue, newValue);<br>
	 * </b> <b>&emsp;&emsp;Assert.assertEquals(oldValue2, newValue2);<br>
	 * </b> Here,<br>
	 * oldValue = old value for Tx1, newValue = Value of Tx1, after recover is
	 * called.<br>
	 * oldValue2 = old value for Tx2, newValue2 = Value of Tx2, after recover is
	 * called.<br>
	 * </p>
	 * 
	 */
	@Test
	public void testScenario3() {
		System.out.println("----------Running Recovery Scenario 3----------");
		// Creating a Block.
		Block blk1 = new Block("filename", 1);
		Block blk2 = new Block("filename", 2);

		// Create a RecoveryManager
		int txid = 123;
		int txid2 = 143;
		RecoveryMgr rm = new RecoveryMgr(txid);
		RecoveryMgr rm2 = new RecoveryMgr(txid2);

		// Sample setInt
		BufferMgr basicBufferMgr = new SimpleDB().bufferMgr();
		Buffer buff = basicBufferMgr.pin(blk1);
		Buffer buff2 = basicBufferMgr.pin(blk2);
		int offset = 8;
		int offset2 = 16;
		int oldValue = buff.getInt(offset);
		int oldValue2 = buff2.getInt(offset2);
		System.out.println("Old Value: " + oldValue);
		System.out.println("Old Value2: " + oldValue2);
		int valueToSet = oldValue + 10;
		int valueToSet2 = oldValue2 + 20;
		int lsn = rm.setInt(buff, offset, valueToSet);
		buff.setInt(offset, valueToSet, txid, lsn);
		int lsn2 = rm2.setInt(buff2, offset2, valueToSet2);
		buff2.setInt(offset2, valueToSet2, txid2, lsn2);
		int newValue = buff.getInt(offset);
		System.out.println("New Value without committing: " + newValue);
		int newValue2 = buff2.getInt(offset2);
		System.out.println("New Value2 without committing: " + newValue2);

		rm2.recover();
		newValue = buff.getInt(offset);
		newValue2 = buff2.getInt(offset2);
		System.out.println("New Value after recovering: " + newValue);
		System.out.println("New Value2 after recovering: " + newValue2);
		Assert.assertEquals(oldValue, newValue);
		Assert.assertEquals(oldValue2, newValue2);
		System.out.println("----------Recovery Scenario 3 Run Complete----------");
	}

}
