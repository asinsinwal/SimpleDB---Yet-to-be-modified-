# SimpleDB

Team:

1. Animesh Sinsinwal
2. Sumit Srivastava
3. Nikhil Sharma
4. Bhaskar Sinha

Background:

PART A:

The SimpleDB buffer manager is grossly inefficient in two ways:

• When looking for a buffer to replace, it uses the first unpinned buffer it finds, instead of using some
intelligent replacement policy.

• When checking to see if a block is already in a buffer, it does a sequential scan of the buffers,
instead of keeping a data structure (such as a map) to more quickly locate the buffer.

PART B:

The SimpleDB recovery manager is implemented via the package simpledb.tx.recovery and in particular the class RecoveryMgr. Each transaction creates its own recovery manager which has methods to write the appropriate log records for that transaction. For example, the constructor writes a start log record, the commit and rollback methods write corresponding log records, and setInt and setString extract the old value from the specified buffer and write and update record to the log.

The code for the SimpleDB recovery manager can be divided into three major areas: (i) code to implement the different kinds of log records (implemented by different classes that implement the LogRecord interface), (ii) code to iterate to the log file (LogIterator) and (iii) code to implement the rollback and recovery algorithms. In this part of the project, you will be working primarily with the simpledb.tx.Recovery. The current implementation of SimpleDB only implements the Undo recovery algorithm. 


Tasks:

Task 1: Use a data structure to keep track of the buffer pool for more efficient searching. 
This structure will track allocated buffers, keyed on the block they contain. (A buffer is allocated when its contents is not null, and may be pinned or unpinned. A buffer starts out unallocated; it becomes allocated when it is first assigned to a block, and stays allocated forever after.) Use this map to determine if a block is currently in a buffer. When a buffer is replaced, you must update the data structure -- The mapping for the old block must be removed, and the mapping for the new block must be added. For our convenience, we will be using “bufferPoolMap” as the name of the structure.

Task 2: First In First Out (FIFO) Buffer Replacement Policy
This suggests a page replacement strategy that chooses the page that was least recently replaced i.e. the page that has been sitting in the buffer pool the longest. This differs a little from the least recently used in that FIFO considers when the page was added to the pool while LRU considers when the page was last accessed.

Task 3: Modify recovery implementation to do the Undo-Redo algorithm.
