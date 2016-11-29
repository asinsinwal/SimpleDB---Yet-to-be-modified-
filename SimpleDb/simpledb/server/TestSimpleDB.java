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

			// Creating a basicBufferMgr
			BufferMgr basicBufferMgr = new SimpleDB().bufferMgr();

			// Existing pool
			System.out.println("Existing buffer pool: ");
			printBufferPool(basicBufferMgr);

			System.out.println("Creating 10 Blocks");
			Block[] blocks = new Block[10];
			for (int i = 0; i < 10; i++) {
				System.out.println("\tCreating Block " + (i));
				blocks[i] = new Block("filename", i);
			}

			System.out.println("Pinning the Blocks");
			Buffer[] buffers = new Buffer[8];
			for (int i = 0; i < 8; i++) {
				Block blk = blocks[i];
				System.out.println("\tPinning Block " + blk);
				Buffer buf = basicBufferMgr.pin(blk);
				System.out.println("\tBlock Pinned to Buffer " + buf);
				buffers[i] = buf;
			}

			System.out.println("After setting 8 blocks in buffer pool:");
			printBufferPool(basicBufferMgr);

			System.out.println("Unpining Blocks");
			System.out.println("\tUnpining Block 2");
			basicBufferMgr.unpin(buffers[2]);
			System.out.println("\tUnpining Block 1");
			basicBufferMgr.unpin(buffers[1]);
			System.out.println("After unpinning in buffer pool:");
			printBufferPool(basicBufferMgr);

			System.out.println("pinning block 8 now 1 should be replaced.");
			basicBufferMgr.pin(blocks[8]);
			System.out.println("After Block replacement in buffer pool:");
			printBufferPool(basicBufferMgr);

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
			System.out.println("\t" + ++i + "): " + e.getKey().toString() + " = [" + e.getValue().toString() + "]\t");
		}
	}

}
