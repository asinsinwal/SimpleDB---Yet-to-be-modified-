package simpledb.test;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import simpledb.buffer.Buffer;
import simpledb.buffer.BufferAbortException;
import simpledb.buffer.BufferMgr;
import simpledb.file.Block;
import simpledb.remote.RemoteDriverImpl;
import simpledb.server.SimpleDB;

/**
 * @author Sumit
 */
@SuppressWarnings("static-access")
public class TestBuffer {

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
				"----------------------------------------Test Buffer Setting up----------------------------------------");
		SimpleDB.init("simpleDB");
		// create a registry specific for the server on the default port
		reg = LocateRegistry.createRegistry(DEFAULT_PORT);
		// and post the server entry in it
		reg.rebind(BINDING_NAME, new RemoteDriverImpl());
		System.out.println(
				"----------------------------------------Test Buffer Set Up done----------------------------------------");
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
				"----------------------------------------Test Buffer Tearing down----------------------------------------");
		reg.unbind(BINDING_NAME);
		reg = null;
		System.out.println(
				"----------------------------------------Test Buffer Tear down done----------------------------------------");
	}

	@Test
	public void testScenario1() throws RemoteException {
		System.out.println("----------Running Buffer Test Scenario 1----------");
		BufferMgr basicBufferMgr = new SimpleDB().bufferMgr();

		System.out.println("Creating 10 Blocks");
		Block[] blocks = new Block[10];
		for (int i = 0; i < 10; i++) {
			System.out.println("\tCreating Block " + (i + 1));
			blocks[i] = new Block("filename", i);
		}

		System.out.println("Initial State of Buffer Pool:");
		printBufferPool(basicBufferMgr);

		System.out.println("Pinning the Blocks");
		Buffer[] buffers = new Buffer[8];
		for (int i = 0; i < 8; i++) {
			Block blk = blocks[i];
			System.out.println("\tPinning Block " + blk);
			Buffer buf = basicBufferMgr.pin(blk);
			System.out.println("\tBlock Pinned to Buffer " + buf);
			buffers[i] = buf;
		}

		System.out.println("Buffer Pool after setting 8 blocks:");
		printBufferPool(basicBufferMgr);

		System.out.println("Unpining Blocks");
		System.out.println("\tUnpining Block 2");
		basicBufferMgr.unpin(buffers[2]);
		System.out.println("\tUnpining Block 1");
		basicBufferMgr.unpin(buffers[1]);

		System.out.println("Buffer Pool after unpinning blocks 2 and 1:");
		printBufferPool(basicBufferMgr);

		System.out.println("Pinning new Block 8");
		basicBufferMgr.pin(blocks[8]);
		System.out.println(
				"Now we have 2 unpinned buffers available. As per FIFO Policy, the earlier unpinned buffer will be replaced.");

		System.out.println("Buffer Pool after pinning new block 8:");
		printBufferPool(basicBufferMgr);

		Assert.assertFalse(basicBufferMgr.getBufferPoolMap().containsKey(blocks[1]));
		Assert.assertTrue(basicBufferMgr.getBufferPoolMap().containsKey(blocks[8]));
		basicBufferMgr.getBufferPoolMap().clear();
		//TODO: We also need to reset the numAvailable Flag here.
		System.out.println("----------Buffer Test Scenario 1 Run Complete----------");
	}

	@Test
	public void testScenario2() {
		System.out.println("----------Running Buffer Test Scenario 2----------");
		BufferMgr basicBufferMgr = new SimpleDB().bufferMgr();

		System.out.println("Creating 10 Blocks");
		Block[] blocks = new Block[10];
		for (int i = 0; i < 10; i++) {
			System.out.println("\tCreating Block " + (i + 1));
			blocks[i] = new Block("filename", i);
		}

		System.out.println("Initial State of Buffer Pool:");
		printBufferPool(basicBufferMgr);

		System.out.println("Pinning the Blocks");
		Buffer[] buffers = new Buffer[8];
		for (int i = 0; i < 8; i++) {
			Block blk = blocks[i];
			System.out.println("\tPinning Block " + blk);
			Buffer buf = basicBufferMgr.pin(blk);
			System.out.println("\tBlock Pinned to Buffer " + buf);
			buffers[i] = buf;
		}

		System.out.println("Buffer Pool after setting 8 blocks:");
		printBufferPool(basicBufferMgr);

		try {
			System.out.println("Pinning new Block 8");
			basicBufferMgr.pin(blocks[8]);
			Assert.assertTrue(false); // If pin does not throw an exception then
										// test case is failed.
		} catch (BufferAbortException e) {
			System.out.println("Exception Thrown: " + e.getClass());
			Assert.assertTrue(true); // If pin throws an exception then test
										// case is passed.
		}

		System.out.println("Buffer Pool after trying to pin new block 8:");
		printBufferPool(basicBufferMgr);
		basicBufferMgr.getBufferPoolMap().clear();
		//TODO: We also need to reset the numAvailable Flag here.
		System.out.println("----------Buffer Test Scenario 2 Run Complete----------");
	}

	@Test
	public void testScenario3() {
		System.out.println("----------Running Buffer Test Scenario 3----------");
		BufferMgr basicBufferMgr = new SimpleDB().bufferMgr();

		System.out.println("Creating 10 Blocks");
		Block[] blocks = new Block[10];
		for (int i = 0; i < 10; i++) {
			System.out.println("\tCreating Block " + (i + 1));
			blocks[i] = new Block("filename", i);
		}

		System.out.println("Initial State of Buffer Pool:");
		printBufferPool(basicBufferMgr);

		System.out.println("Pinning the Blocks");
		Buffer[] buffers = new Buffer[8];
		for (int i = 0; i < 6; i++) {
			Block blk = blocks[i];
			System.out.println("\tPinning Block " + blk);
			Buffer buf = basicBufferMgr.pin(blk);
			System.out.println("\tBlock Pinned to Buffer " + buf);
			buffers[i] = buf;
		}

		System.out.println("Buffer Pool after setting 8 blocks:");
		printBufferPool(basicBufferMgr);

		System.out.println("Unpining Blocks");
		System.out.println("\tUnpining Block 2");
		basicBufferMgr.unpin(buffers[2]);
		System.out.println("\tUnpining Block 1");
		basicBufferMgr.unpin(buffers[1]);

		System.out.println("Buffer Pool after unpinning blocks 2 and 1:");
		printBufferPool(basicBufferMgr);

		System.out.println("Pinning new Block 8");
		basicBufferMgr.pin(blocks[8]);
		System.out.println(
				"Now we have 2 unpinned buffers available. As per FIFO Policy, the earlier unpinned buffer will be replaced.");

		System.out.println("Buffer Pool after pinning new block 8:");
		printBufferPool(basicBufferMgr);

		Assert.assertTrue(basicBufferMgr.getBufferPoolMap().containsKey(blocks[1]));
		Assert.assertTrue(basicBufferMgr.getBufferPoolMap().containsKey(blocks[8]));
		basicBufferMgr.getBufferPoolMap().clear();
		//TODO: We also need to reset the numAvailable Flag here.
		System.out.println("----------Buffer Test Scenario 3 Run Complete----------");
	}
	
	@Test
	public void testScenario4() {
		System.out.println("----------Running Buffer Test Scenario 4----------");
		BufferMgr basicBufferMgr = new SimpleDB().bufferMgr();

		System.out.println("Creating 10 Blocks");
		Block[] blocks = new Block[10];
		for (int i = 0; i < 10; i++) {
			System.out.println("\tCreating Block " + (i + 1));
			blocks[i] = new Block("filename", i);
		}

		System.out.println("Initial State of Buffer Pool:");
		printBufferPool(basicBufferMgr);

		System.out.println("Pinning the Blocks");
		Buffer[] buffers = new Buffer[8];
		for (int i = 0; i < 8; i++) {
			Block blk = blocks[i];
			System.out.println("\tPinning Block " + blk);
			Buffer buf = basicBufferMgr.pin(blk);
			System.out.println("\tBlock Pinned to Buffer " + buf);
			buffers[i] = buf;
		}
		
		System.out.println("Pinning Block 1 Again");
		basicBufferMgr.pin(blocks[1]);

		System.out.println("Buffer Pool after setting 8 blocks:");
		printBufferPool(basicBufferMgr);

		System.out.println("Unpining Blocks");
		System.out.println("\tUnpining Block 2");
		basicBufferMgr.unpin(buffers[2]);
		System.out.println("\tUnpining Block 1");
		basicBufferMgr.unpin(buffers[1]);

		System.out.println("Buffer Pool after unpinning blocks 2 and 1:");
		printBufferPool(basicBufferMgr);

		System.out.println("Pinning new Block 8");
		basicBufferMgr.pin(blocks[8]);
		System.out.println(
				"Now we have 1 unpinned buffers available. Since lock 1 was pinned twice and unpinned only once.\nSo, Block 2 will be replaced.");

		System.out.println("Buffer Pool after pinning new block 8:");
		printBufferPool(basicBufferMgr);

		Assert.assertTrue(basicBufferMgr.getBufferPoolMap().containsKey(blocks[1]));
		Assert.assertTrue(basicBufferMgr.getBufferPoolMap().containsKey(blocks[8]));
		basicBufferMgr.getBufferPoolMap().clear();
		//TODO: We also need to reset the numAvailable Flag here.
		System.out.println("----------Buffer Test Scenario 4 Run Complete----------");
	}

	private void printBufferPool(BufferMgr basicBufferMgr) {
		int i = 0;
		for (Map.Entry<Block, Buffer> e : basicBufferMgr.getBufferPoolMap().entrySet()) {
			System.out.println("\t" + ++i + "): " + e.getKey().toString() + " = [" + e.getValue().toString() + "]\t");
		}
	}

}
