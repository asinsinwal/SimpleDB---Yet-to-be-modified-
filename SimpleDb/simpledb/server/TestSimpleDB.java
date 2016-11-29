package simpledb.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

import simpledb.buffer.Buffer;
import simpledb.buffer.BufferAbortException;
import simpledb.buffer.BufferMgr;
import simpledb.file.Block;
import simpledb.remote.RemoteDriver;
import simpledb.remote.RemoteDriverImpl;

/**
 * @author Sumit
 */
public class TestSimpleDB {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		testSimpleDB();
	}

	@SuppressWarnings({ "static-access", "unused" })
	private static void testSimpleDB() {
		try {
			// Init a simpleDB Client
			SimpleDB.init("simpleDB");
			Registry reg = LocateRegistry.createRegistry(1099);

			// and post the server entry in it
			RemoteDriver d = new RemoteDriverImpl();
			reg.rebind("simpledb", d);

			System.out.println("database server ready");

			// Creating a Block -
			Block blk1 = new Block("filename", 1);
			Block blk2 = new Block("filename", 2);
			Block blk3 = new Block("filename", 3);
			Block blk4 = new Block("filename", 4);
			Block blk5 = new Block("filename", 5);
			Block blk6 = new Block("filename", 6);
			Block blk7 = new Block("filename", 7);
			Block blk8 = new Block("filename", 8);
			Block blk9 = new Block("filename", 9);
			Block blk10 = new Block("filename", 10);

			// Creating a basicBufferMgr
			BufferMgr basicBufferMgr = new SimpleDB().bufferMgr();

			// Existing pool
			System.out.println("Existing buffer pool: ");
//			printBufferPool(basicBufferMgr);
			// Pin a Block
			System.out.println("pinning blocks:");
			System.out.println("\tpinning 1");
			Buffer buff1 = basicBufferMgr.pin(blk1);
			System.out.println("\tpinning 2");
			Buffer buff2 = basicBufferMgr.pin(blk2);
			System.out.println("\tpinning 3");
			Buffer buff3 = basicBufferMgr.pin(blk3);
			System.out.println("\tpinning 4");
			Buffer buff4 = basicBufferMgr.pin(blk4);
			System.out.println("\tpinning 5");
			Buffer buff5 = basicBufferMgr.pin(blk5);
			System.out.println("\tpinning 6");
			Buffer buff6 = basicBufferMgr.pin(blk6);
			System.out.println("\tpinning 7");
			Buffer buff7 = basicBufferMgr.pin(blk7);
			System.out.println("\tpinning 8");
			Buffer buff8 = basicBufferMgr.pin(blk8);
			System.out.println("After setting 8 blocks in buffer pool:");
//			printBufferPool(basicBufferMgr);

			System.out.println("unpinning blocks");
			// Unpin a Block
			basicBufferMgr.unpin(buff3);
			basicBufferMgr.unpin(buff2);     
			System.out.println("After unpinning in buffer pool:");
			printBufferPool(basicBufferMgr);

			System.out.println("pinning block 9 now 2 should be replaced.");
			// Catching Buffer Exception
			basicBufferMgr.pin(blk9);
			System.out.println("After Block replacement in buffer pool:");
//			printBufferPool(basicBufferMgr);

		} catch (BufferAbortException e) {
			System.out.println("BufferAbortException: ");
			e.printStackTrace();
		} catch (RemoteException e) {
			System.out.println("RemoteException: ");
			e.printStackTrace();
		}
	}

	private static void printBufferPool(BufferMgr basicBufferMgr) {
		int i = 0;
		for (Map.Entry<Block, Buffer> e : basicBufferMgr.getBufferPoolMap().entrySet()) {
			System.out.println("\t" + i++ + "): " + e.getKey().toString() + " = [" + e.getValue().toString() + "]\t");
		}
	}

}
