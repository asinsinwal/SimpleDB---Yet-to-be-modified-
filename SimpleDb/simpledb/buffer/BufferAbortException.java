package simpledb.buffer;

/**
 * A runtime exception indicating that the transaction
 * needs to abort because a buffer request could not be satisfied.
 * @author Edward Sciore
 */
public class BufferAbortException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8411322878343217445L;
	public BufferAbortException() {
	   }	
}
