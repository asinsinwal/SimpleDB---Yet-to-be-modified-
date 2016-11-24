package simpledb.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

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

			// Creating a basicBufferMgr
			BufferMgr basicBufferMgr = new SimpleDB().bufferMgr();

			// Pin a Block
			Buffer buff1 = basicBufferMgr.pin(blk1);
			Buffer buff2 = basicBufferMgr.pin(blk2);
			Buffer buff3 = basicBufferMgr.pin(blk3);
			Buffer buff4 = basicBufferMgr.pin(blk4);
			Buffer buff5 = basicBufferMgr.pin(blk5);
			Buffer buff6 = basicBufferMgr.pin(blk6);
			Buffer buff7 = basicBufferMgr.pin(blk7);
			Buffer buff8 = basicBufferMgr.pin(blk8);

			// Unpin a Block
			basicBufferMgr.unpin(buff3);
			basicBufferMgr.unpin(buff2);

			// Catching Buffer Exception
			basicBufferMgr.pin(blk1);

		} catch (BufferAbortException e) {
			System.out.println("BufferAbortException: " + e.getStackTrace());
		} catch (RemoteException e) {
			System.out.println("RemoteException: " + e.getStackTrace());
		}
	}

}
