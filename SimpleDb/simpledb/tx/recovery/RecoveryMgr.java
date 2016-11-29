package simpledb.tx.recovery;

import static simpledb.tx.recovery.LogRecord.CHECKPOINT;
import static simpledb.tx.recovery.LogRecord.COMMIT;
import static simpledb.tx.recovery.LogRecord.ROLLBACK;
import static simpledb.tx.recovery.LogRecord.START;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import simpledb.buffer.Buffer;
import simpledb.file.Block;
import simpledb.server.SimpleDB;

/**
 * The recovery manager. Each transaction has its own recovery manager.
 * 
 * @author Edward Sciore
 */
public class RecoveryMgr {
	private int txnum;

	/**
	 * Creates a recovery manager for the specified transaction.
	 * 
	 * @param txnum
	 *            the ID of the specified transaction
	 */
	public RecoveryMgr(int txnum) {
		this.txnum = txnum;
		new StartRecord(txnum).writeToLog();
	}

	/**
	 * Writes a commit record to the log, and flushes it to disk.
	 */
	public void commit() {
		SimpleDB.bufferMgr().flushAll(txnum);
		int lsn = new CommitRecord(txnum).writeToLog();
		SimpleDB.logMgr().flush(lsn);
	}

	/**
	 * Writes a rollback record to the log, and flushes it to disk.
	 */
	public void rollback() {
		doRollback();
		SimpleDB.bufferMgr().flushAll(txnum);
		int lsn = new RollbackRecord(txnum).writeToLog();
		SimpleDB.logMgr().flush(lsn);
	}

	/**
	 * Recovers uncompleted transactions from the log, then writes a quiescent
	 * checkpoint record to the log and flushes it.
	 */
	public void recover() {
		doRecover();
		SimpleDB.bufferMgr().flushAll(txnum);
		int lsn = new CheckpointRecord().writeToLog();
		SimpleDB.logMgr().flush(lsn);

	}

	/**
	 * Writes a setint record to the log, and returns its lsn. Updates to
	 * temporary files are not logged; instead, a "dummy" negative lsn is
	 * returned.
	 * 
	 * @param buff
	 *            the buffer containing the page
	 * @param offset
	 *            the offset of the value in the page
	 * @param newval
	 *            the value to be written
	 */
	public int setInt(Buffer buff, int offset, int newval) {
		int oldval = buff.getInt(offset);
		Block blk = buff.block();
		if (isTempBlock(blk))
			return -1;
		else
			return new SetIntRecord(txnum, blk, offset, oldval).writeToLog();
	}

	/**
	 * Writes a setstring record to the log, and returns its lsn. Updates to
	 * temporary files are not logged; instead, a "dummy" negative lsn is
	 * returned.
	 * 
	 * @param buff
	 *            the buffer containing the page
	 * @param offset
	 *            the offset of the value in the page
	 * @param newval
	 *            the value to be written
	 */
	public int setString(Buffer buff, int offset, String newval) {
		String oldval = buff.getString(offset);
		Block blk = buff.block();
		if (isTempBlock(blk))
			return -1;
		else
			return new SetStringRecord(txnum, blk, offset, oldval).writeToLog();
	}

	/**
	 * Rolls back the transaction. The method iterates through the log records,
	 * calling undo() for each log record it finds for the transaction, until it
	 * finds the transaction's START record.
	 */
	private void doRollback() {
		Iterator<LogRecord> iter = new LogRecordIterator();
		while (iter.hasNext()) {
			LogRecord rec = iter.next();
			if (rec.txNumber() == txnum) {
				if (rec.op() == START)
					return;
				rec.undo(txnum);
			}
		}
	}

	/**
	 * Does a complete database recovery. The method iterates through the log
	 * records. Whenever it finds a log record for an unfinished transaction, it
	 * calls undo() on that record. The method stops when it encounters a
	 * CHECKPOINT record or the end of the log.
	 */
	private void doRecover() {
		Collection<Integer> rollbackTxns = new ArrayList<Integer>();
		Collection<Integer> commitTxns = new ArrayList<Integer>();
		Iterator<LogRecord> iter = new LogRecordIterator();
		List<LogRecord> listRecs = new ArrayList<LogRecord>();
		while (iter.hasNext()) {
			if (iter.next() != null){
			LogRecord rec = iter.next();
//			System.out.println("rec: " + iter.next());
			if (rec.op() == CHECKPOINT)
				// return;
				break;
			else
				listRecs.add(rec);
			if (rec.op() == COMMIT)
				commitTxns.add(rec.txNumber());
			if (rec.op() == ROLLBACK)
				rollbackTxns.add(rec.txNumber());
			else if (!rollbackTxns.contains(rec.txNumber()) && !commitTxns.contains(rec.txNumber()))
				rec.undo(txnum);
		}
		// TODO: do a new while loop which reads the log file from START in
		// forward order from checkpoint.
		Collections.reverse(listRecs);
//		System.out.println("The entire log from checkpoint (or starting) is: ");
//		for (LogRecord rec : listRecs) {
//			System.out.println("\t\t" + rec.toString());
//		}
		//System.out.println("REdo-ing now");
		for (LogRecord rec : listRecs) {
			boolean nonUpdateRecord = ((rec.op() != COMMIT) && (rec.op() != CHECKPOINT) && (rec.op() != ROLLBACK)
					&& rec.op() != START);
//			System.out.println("For txn: " + txnum + "currently reading this record: " + rec.toString() + ", is it on commit list? - " + commitTxns.contains(rec.txNumber()));
			if (nonUpdateRecord && commitTxns.contains(rec.txNumber())) {
				//System.out.println("This transaction was redo-ed: " + txnum);
				rec.redo(txnum);
			}
		}
	}
	}

	/**
	 * Determines whether a block comes from a temporary file or not.
	 */
	private boolean isTempBlock(Block blk) {
		return blk.fileName().startsWith("temp");
	}
}
