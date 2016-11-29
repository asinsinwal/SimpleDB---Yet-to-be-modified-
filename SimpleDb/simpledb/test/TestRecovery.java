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
public class TestRecovery {

	private static Registry reg;
	private static final int DEFAULT_PORT = 1099;
	private static final String BINDING_NAME = "simpledb";

	/**
	 * setting up the resources to be used by the test classes. This method runs
	 * once before running the test case classes.
	 * 
	 * @throws RemoteException
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
	 * releasing the resources to be used by the test classes. This method runs
	 * once after running all the test case classes.
	 * 
	 * @throws RemoteException
	 * @throws NotBoundException
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

	@Test
	public void testScenario1() {
		System.out.println("----------Running Recovery Scenario 1----------");
		// Creating a Block.
		Block blk1 = new Block("filename", 1);
		Block blk2 = new Block("filename", 2);

		// Create a RecoveryManager with id = 123.
		int txid = 123;
		int txid2 = 143;
		int txid3 = 150;
		RecoveryMgr rm = new RecoveryMgr(txid);
		RecoveryMgr rm2 = new RecoveryMgr(txid2);
		RecoveryMgr rm3 = new RecoveryMgr(txid3);

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
		int valueToSet2 = oldValue + 20;
		int lsn = rm.setInt(buff, offset, valueToSet);// buffer, offset, newval
		buff.setInt(offset, valueToSet, txid, lsn);// offset, val, txnum,lsn
		int lsn2 = rm2.setInt(buff2, offset2, valueToSet2);// buffer, offset,
															// newval
		buff2.setInt(offset2, valueToSet2, txid2, lsn2);
		int newValue = buff.getInt(offset);
		System.out.println("New Value without committing: " + newValue);
		int newValue2 = buff2.getInt(offset2);
		System.out.println("New Value2 without committing: " + newValue2);
		// buff.setInt(9, 2222, txid, lsn);
		// Flushing all transactions
		BufferMgr bm = new BufferMgr(8);
		// bm.flushAll(txid);

		rm2.commit();
		rm3.commit();
		rm2.recover();
		newValue = buff.getInt(offset);
		newValue2 = buff2.getInt(offset2);
		System.out.println("New Value after recovering: " + newValue);
		System.out.println("New Value2 after recovering: " + newValue2);
		Assert.assertEquals(oldValue, newValue);
		Assert.assertEquals(valueToSet2, newValue2);
		System.out.println("----------Recovery Scenario 1 Run Complete----------");
	}

	@Test
	public void testScenario2() {
		System.out.println("----------Running Recovery Scenario 2----------");
		// Creating a Block.
		Block blk1 = new Block("filename", 1);
		Block blk2 = new Block("filename", 2);

		// Create a RecoveryManager with id = 123.
		int txid = 123;
		int txid2 = 143;
		int txid3 = 150;
		RecoveryMgr rm = new RecoveryMgr(txid);
		RecoveryMgr rm2 = new RecoveryMgr(txid2);
		RecoveryMgr rm3 = new RecoveryMgr(txid3);

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
		int valueToSet2 = oldValue + 20;
		int lsn = rm.setInt(buff, offset, valueToSet);// buffer, offset, newval
		buff.setInt(offset, valueToSet, txid, lsn);// offset, val, txnum,lsn
		int lsn2 = rm2.setInt(buff2, offset2, valueToSet2);// buffer, offset,
															// newval
		buff2.setInt(offset2, valueToSet2, txid2, lsn2);
		int newValue = buff.getInt(offset);
		System.out.println("New Value without committing: " + newValue);
		int newValue2 = buff2.getInt(offset2);
		System.out.println("New Value2 without committing: " + newValue2);
		// buff.setInt(9, 2222, txid, lsn);
		// Flushing all transactions
		BufferMgr bm = new BufferMgr(8);
		// bm.flushAll(txid);

		rm.commit();
		rm2.commit();
		rm3.commit();
		rm2.recover();
		newValue = buff.getInt(offset);
		newValue2 = buff2.getInt(offset2);
		System.out.println("New Value after recovering: " + newValue);
		System.out.println("New Value2 after recovering: " + newValue2);
		Assert.assertEquals(valueToSet, newValue);
		Assert.assertEquals(valueToSet2, newValue2);
		System.out.println("----------Recovery Scenario 2 Run Complete----------");
	}

	@Test
	public void testScenario3() {
		System.out.println("----------Running Recovery Scenario 3----------");
		// Creating a Block.
		Block blk1 = new Block("filename", 1);
		Block blk2 = new Block("filename", 2);

		// Create a RecoveryManager with id = 123.
		int txid = 123;
		int txid2 = 143;
		int txid3 = 150;
		RecoveryMgr rm = new RecoveryMgr(txid);
		RecoveryMgr rm2 = new RecoveryMgr(txid2);
		RecoveryMgr rm3 = new RecoveryMgr(txid3);

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
		int valueToSet2 = oldValue + 20;
		int lsn = rm.setInt(buff, offset, valueToSet);// buffer, offset, newval
		buff.setInt(offset, valueToSet, txid, lsn);// offset, val, txnum,lsn
		int lsn2 = rm2.setInt(buff2, offset2, valueToSet2);// buffer, offset,
															// newval
		buff2.setInt(offset2, valueToSet2, txid2, lsn2);
		int newValue = buff.getInt(offset);
		System.out.println("New Value without committing: " + newValue);
		int newValue2 = buff2.getInt(offset2);
		System.out.println("New Value2 without committing: " + newValue2);
		// buff.setInt(9, 2222, txid, lsn);
		// Flushing all transactions
		BufferMgr bm = new BufferMgr(8);
		// bm.flushAll(txid);

		rm3.commit();
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
