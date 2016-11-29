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
	public static void main(String[] args) throws RemoteException {
		// Init a simpleDB Client.
		SimpleDB.init("simpleDB");
		Registry reg = LocateRegistry.createRegistry(1099);

		// and post the server entry in it
		RemoteDriver d = new RemoteDriverImpl();
		reg.rebind("simpledb", d);

		System.out.println("database server ready");

		// givenTest();

		nikhilTest();

	}

	@SuppressWarnings({ "static-access", "unused" })
	private static void nikhilTest() {
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
		// lsn = rm.setInt(buff2, 5, 1234);
		// buff.setInt(9, 2222, txid, lsn);
		// Flushing all transactions
		BufferMgr bm = new BufferMgr(8);
		// bm.flushAll(txid);
		// Using Log Record Iterator to print records .

//		rm.commit();
		rm2.commit();
		rm3.commit();
		rm2.recover();
		newValue = buff.getInt(offset);
		newValue2 = buff2.getInt(offset2);
		System.out.println("New Value after recovering: " + newValue);
		System.out.println("New Value2 after recovering: " + newValue2);

	}

	@SuppressWarnings({ "static-access", "unused" })
	private static void givenTest() {
		// Creating a Block.
		Block blk1 = new Block("filename", 1);
		// Create a RecoveryManager with id = 123.
		int txid = 123;
		RecoveryMgr rm = new RecoveryMgr(txid);
		// Commit a transaction.
		rm.commit();
		// Recover a transaction.
		rm.recover();
		// Sample setInt
		BufferMgr basicBufferMgr = new SimpleDB().bufferMgr();
		Buffer buff = basicBufferMgr.pin(blk1);

		int lsn = rm.setInt(buff, 4, 1234);
		buff.setInt(4, 1234, txid, lsn);
		// Flushing all transactions
		BufferMgr bm = new BufferMgr(8);
		bm.flushAll(txid);
		// Using Log Record Iterator to print records .
		LogRecordIterator it = new LogRecordIterator();
		System.out.println(it.next());

	}

}
