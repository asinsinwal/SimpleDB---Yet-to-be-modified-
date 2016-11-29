package simpledb.buffer;

import java.util.LinkedHashMap;
import java.util.Map;

import simpledb.file.Block;
import simpledb.file.FileMgr;

/**
 * Manages the pinning and unpinning of buffers to blocks.
 * 
 * @author Edward Sciore
 *
 */
class BasicBufferMgr {
//	 private Buffer[] bufferpool;
	private int numAvailable;
	private int bufferPoolSize;
	/*
	 * author Animesh creating a HashMap to track Buffer Pool
	 */
	private static final Map<Block, Buffer> bufferPoolMap = new LinkedHashMap<Block, Buffer>();

	/**
	 * Creates a buffer manager having the specified number of buffer slots.
	 * This constructor depends on both the {@link FileMgr} and
	 * {@link simpledb.log.LogMgr LogMgr} objects that it gets from the class
	 * {@link simpledb.server.SimpleDB}. Those objects are created during system
	 * initialization. Thus this constructor cannot be called until
	 * {@link simpledb.server.SimpleDB#initFileAndLogMgr(String)} or is called
	 * first.
	 * 
	 * @param numbuffs
	 *            the number of buffer slots to allocate
	 */
	BasicBufferMgr(int numbuffs) {
		// bufferpool = new Buffer[numbuffs];
		numAvailable = numbuffs;
		bufferPoolSize = numbuffs;
		// Initialing the bufferPoolMap
		// bufferPoolMap = new LinkedHashMap<Block, Buffer>(numbuffs);
		// for (int i=0; i<numbuffs; i++)
		// bufferpool[i] = new Buffer();
	}

	/**
	 * Flushes the dirty buffers modified by the specified transaction.
	 * 
	 * @param txnum
	 *            the transaction's id number
	 */
	synchronized void flushAll(int txnum) {
		/*
		 * for (Buffer buff : bufferpool) if (buff.isModifiedBy(txnum))
		 * buff.flush();
		 */
		/*
		 * author Animesh Changing the model for clearing the buffer
		 */
		for (Buffer buffer : bufferPoolMap.values()) {
			if (buffer.isModifiedBy(txnum)) {
				buffer.flush();
			}
		}
	}

	/**
	 * Pins a buffer to the specified block. If there is already a buffer
	 * assigned to that block then that buffer is used; otherwise, an unpinned
	 * buffer from the pool is chosen. Returns a null value if there are no
	 * available buffers.
	 * 
	 * @param blk
	 *            a reference to a disk block
	 * @return the pinned buffer
	 */
	synchronized Buffer pin(Block blk) throws BufferAbortException {
		Buffer buff = findExistingBuffer(blk);
		if (buff == null) {
			buff = chooseUnpinnedBuffer();
			// if (buff == null)
			// return null;
			bufferPoolMap.remove(buff.block());
			buff.assignToBlock(blk);
		}

		if (!buff.isPinned())
			numAvailable--;
		buff.pin();
		// System.out.println("\t\tPutting this in poolMap: " + blk.toString() +
		// " = " + buff.toString());
		bufferPoolMap.put(blk, buff);
		return buff;
	}

	/**
	 * Allocates a new block in the specified file, and pins a buffer to it.
	 * Returns null (without allocating the block) if there are no available
	 * buffers.
	 * 
	 * @param filename
	 *            the name of the file
	 * @param fmtr
	 *            a pageformatter object, used to format the new block
	 * @return the pinned buffer
	 */
	synchronized Buffer pinNew(String filename, PageFormatter fmtr) throws BufferAbortException {
		Buffer buff = chooseUnpinnedBuffer();
		bufferPoolMap.remove(buff.block());
		// if (buff == null)
		// return null;
		buff.assignToNew(filename, fmtr);
		numAvailable--;
		buff.pin();
		// System.out.println("\t\tPutting this in poolMap: " +
		// buff.block().toString() + " = " + buff.toString());
		bufferPoolMap.put(buff.block(), buff);
		return buff;
	}

	/**
	 * Unpins the specified buffer.
	 * 
	 * @param buff
	 *            the buffer to be unpinned
	 */
	synchronized void unpin(Buffer buff) {
		buff.unpin();
		if (!buff.isPinned())
			numAvailable++;
	}

	/**
	 * Returns the number of available (i.e. unpinned) buffers.
	 * 
	 * @return the number of available buffers
	 */
	int available() {
		return numAvailable;
	}

	private Buffer findExistingBuffer(Block blk) {
		/**
		 * Changes to implement the buffer pool using LinkedHashMap
		 */
		if (bufferPoolMap.containsKey(blk)) {
			return bufferPoolMap.get(blk);
		} else {
			return null;
		}
		// for (Buffer buff : bufferpool) {
		// Block b = buff.block();
		// if (b != null && b.equals(blk))
		// return buff;
		// }
		// return null;
	}

	/**
	 * IMPL: FIFO Replacement Policy
	 * 
	 * @return
	 */
	private Buffer chooseUnpinnedBuffer() throws BufferAbortException {
		/**
		 * Changes to implement the buffer pool using LinkedHashMap
		 */
		// System.out.println("\t\tsize of bufferPoolMap while choosing unpinned
		// buffer: " + bufferPoolMap.size());
		if (numAvailable > 0) {
			if (bufferPoolMap.size() < bufferPoolSize) {
				Buffer buff = new Buffer();
				// bufferPoolMap.put(null, buff);
				// System.out.println("\t\t\tcreating new buffer as size is
				// within limit");
				return buff;
			} else {
				// System.out.println("\t\t\treturning existing buffer.");
				for (Map.Entry<Block, Buffer> e : bufferPoolMap.entrySet()) {
					if (!e.getValue().isPinned()) {
						// System.out.println("\t\t\treturning pinned Buffer: "
						// + e.getKey());
						return e.getValue();
					}
				}
			}
		}
		throw new BufferAbortException();
		// for (Buffer buff : bufferpool)
		// if (!buff.isPinned())
		// return buff;
		// return null;
	}

	/**
	 * @return the bufferPoolMap
	 */
	public Map<Block, Buffer> getBufferPoolMap() {
		return bufferPoolMap;
	}
}