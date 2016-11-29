package simpledb.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test Suite Class for testing the changes made to BufferManager and
 * RecoveryManager in SimpleDB.
 * 
 * @author Sumit
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ TestBuffer.class, TestRecovery.class })
public class SimpleDBTestSuite {

	@BeforeClass
	public static void beforeClass() {
		System.out.println("Suite.BeforeClass");
	}

	@AfterClass
	public static void afterClass() {
		System.out.println("Suite.AfterClass");
	}

}
