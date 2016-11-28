/**
 * 
 */
package simpledb.tx.recovery;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import simpledb.buffer.Buffer;
import simpledb.buffer.BufferMgr;
import simpledb.file.Block;
import simpledb.remote.RemoteDriver;
import simpledb.remote.RemoteDriverImpl;
import simpledb.server.SimpleDB;

/**
 * @author Sumit
 *
 */
public class TestLogRecovery {

	/**
	 * @param args
	 * @throws RemoteException
	 */
	@SuppressWarnings("static-access")
	public static void main(String[] args) throws RemoteException {
		// Init a simpleDB Client.
		SimpleDB.init("simpleDB");
		Registry reg = LocateRegistry.createRegistry(1099);

		// and post the server entry in it
		RemoteDriver d = new RemoteDriverImpl();
		reg.rebind("simpledb", d);

		System.out.println("database server ready");

		// Creating a Block.
		Block blk1 = new Block("filename", 1);
		Block blk2 = new Block("filename", 2);
		
		// Create a RecoveryManager with id = 123.
		int txid = 123;
		int txid2 = 143;
		int txid3 = 150;
		System.out.println("Before txid and commit");
		RecoveryMgr rm = new RecoveryMgr(txid);
		System.out.println("After txid and commit");

		RecoveryMgr rm2 = new RecoveryMgr(txid2);
		RecoveryMgr rm3 = new RecoveryMgr(txid3);

		// Commit a transaction.
		// Recover a transaction.
		rm.commit();
		rm2.commit();
		rm3.commit();
		rm.recover();
		// Sample setInt
		BufferMgr basicBufferMgr = new SimpleDB().bufferMgr();
		Buffer buff = basicBufferMgr.pin(blk1);
		Buffer buff2 = basicBufferMgr.pin(blk2);

		int lsn = rm.setInt(buff, 4, 1234);
		buff.setInt(4, 1234, txid, lsn);
		lsn = rm.setInt(buff2, 5, 1234);
		buff.setInt(5, 1234, txid, lsn);
		// Flushing all transactions
		BufferMgr bm = new BufferMgr(8);
		bm.flushAll(txid);
		// Using Log Record Iterator to print records .
		LogRecordIterator it = new LogRecordIterator();
		System.out.println(it.next());
		System.out.println(it.next());
		System.out.println(it.next());
		System.out.println(it.next());
		System.out.println(it.next());
		System.out.println(it.next());
		
	}

}
