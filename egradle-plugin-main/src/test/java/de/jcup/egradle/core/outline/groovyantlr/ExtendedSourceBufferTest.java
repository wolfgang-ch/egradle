package de.jcup.egradle.core.outline.groovyantlr;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import de.jcup.egradle.core.text.OffsetCalculator;

public class ExtendedSourceBufferTest {

	private ExtendedSourceBuffer bufferToTest;
	private OffsetCalculator mockedCalculator;

	@Before
	public void before() {
		bufferToTest = new ExtendedSourceBuffer();
		mockedCalculator = mock(OffsetCalculator.class);
		bufferToTest.calculator = mockedCalculator;
	}

	@Test
	public void getOffset_for_text_EMPTY_line_10_column_20__calculator_is_called_with_frozenLinesAsArray__and_line_10_column_20() {
		/* prepare */
		write("", bufferToTest);

		/* execute */
		bufferToTest.getOffset(10, 20);

		/* test */

		// remark: StringBuilder and StringBuffer do not override equals of
		// Object. So a mockito verify(mockedCalculator).getOffset(aryEq(new
		// StringBuilder[]{new StringBuilder()}...) DOES NOT WORK!
		// so test frozen array content before and then check mocked called with
		// the frozen array
		assertEquals(1, bufferToTest.frozenLinesAsArray.length);
		assertEquals("", bufferToTest.frozenLinesAsArray[0].toString());

		verify(mockedCalculator).calculatetOffset(bufferToTest.frozenLinesAsArray, 10, 20);
	}

	@Test
	public void writing_text_HELLOWORLD_cr_NEWLINE_results_in_frozenLinesArray_length2() {
		/* prepare */
		write("HELLOWORLD\nNEWLINE", bufferToTest);

		/* execute */
		bufferToTest.ensureFrozen();
		
		/* test */
		assertEquals(2, bufferToTest.frozenLinesAsArray.length);
	}

	@Test
	public void unix_writing_text_12345_lf_NEWLINE_results_in_frozenLinesArray_first_line_has_length_of_6() {
		/* prepare */
		write("12345\nNEWLINE", bufferToTest);
		
		/* execute */
		bufferToTest.ensureFrozen();
		
		/* test */
		assertEquals(6, bufferToTest.frozenLinesAsArray[0].length());
	}

	@Test
	public void mac_writing_text_12345_cr_NEWLINE_results_in_frozenLinesArray_first_line_has_length_of_6() {
		/* prepare */
		write("12345\rNEWLINE", bufferToTest);

		/* execute */
		bufferToTest.ensureFrozen();

		/* test */
		assertEquals(6, bufferToTest.frozenLinesAsArray[0].length());
	}

	@Test
	public void windows_writing_text_12345_cr_lf_NEWLINE_results_in_frozenLinesArray_first_line_has_length_of_7() {
		/* prepare */
		write("12345\r\nNEWLINE", bufferToTest);

		/* execute */
		bufferToTest.ensureFrozen();

		/* test */
		assertEquals(7, bufferToTest.frozenLinesAsArray[0].length());
	}
	
	@Test
	public void write4LinesSeparatedByLf_results_in_4_frozen_lines(){
		/* prepare */
		String text = "def variable1='value1'\n\n\ndef variable2='value2'";

		/* execute */
		write(text,bufferToTest);
		bufferToTest.ensureFrozen();
		
		/* test */
		assertEquals(4, bufferToTest.frozenLinesAsArray.length);
		
	}
	
	private void write(String string, ExtendedSourceBuffer bufferToTest) {
		for (char c : string.toCharArray()) {
			bufferToTest.write(c);
		}

	}

}
