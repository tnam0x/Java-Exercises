package junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class DivTest {
	@Test
	public void testDivIntPass() {
		assertEquals("error in divInt()", 3, Calculator.divInt(9, 3));
		assertEquals("error in divInt()", 0, Calculator.divInt(1, 9));
	}

	@Test
	public void testDivIntFail() {
		assertNotEquals("error in divInt()", 1, Calculator.divInt(9, 3));
	}

	@Test//(expected = IllegalArgumentException.class)
	public void testDivIntByZero() {
		Calculator.divInt(9, 0); // expect an IllegalArgumentException
	}

	@Test
	public void testDivRealPass() {
		assertEquals("error in divInt()", 0.333333, Calculator.divReal(1, 3), 1e-6);
		assertEquals("error in divInt()", 0.111111, Calculator.divReal(1, 9), 1e-6);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDivRealByZero() {
		Calculator.divReal(9, 0); // expect an IllegalArgumentException
	}
	
	public static void main(String[] args) {
		Result result = JUnitCore.runClasses(DivTest.class);
		for (Failure failure : result.getFailures()) {
			System.out.println(failure.toString());
		}
		System.out.println(result.wasSuccessful());
	}
}
