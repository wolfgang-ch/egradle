package de.jcup.egradle.eclipse.gradleeditor.document;

import static org.junit.Assert.*;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.junit.Before;
import org.junit.Test;

/**
 * Sorrowly not executable by gradle because of eclipse dependencies. But
 * at least executable in eclipse environment. Tests exact word pattern rule works
 * @author Albert Tregnaghi
 *
 */
public class ExactWordPatternRuleTest {
	
	private IToken token;
	private OneLineSimpleTestCharacterScanner scanner;

	@Before
	public void before(){
		token = new Token("mocked");
	}

	@Test
	public void interface_is_found_scanner_column_is_8() {
		scanner = new OneLineSimpleTestCharacterScanner("interface");
		ExactWordPatternRule rule = new ExactWordPatternRule(new JavaWordDetector(), "interface", token);
		rule.trace=true;
		rule.evaluate(scanner);
		
		assertEquals(8,scanner.column);
		
	}
	
	@Test
	public void interface_is_NOT_found_scanner_column_is_0__something() {
		scanner = new OneLineSimpleTestCharacterScanner("something");
		ExactWordPatternRule rule = new ExactWordPatternRule(new JavaWordDetector(), "interface", token);
		rule.trace=true;
		rule.evaluate(scanner);
		
		assertEquals(0,scanner.column);
		
	}
	
	@Test
	public void interface_is_NOT_found_scanner_column_is_0__xinterface() {
		scanner = new OneLineSimpleTestCharacterScanner("xinterface");
		ExactWordPatternRule rule = new ExactWordPatternRule(new JavaWordDetector(), "interface", token);
		rule.trace=true;
		rule.evaluate(scanner);
		
		assertEquals(0,scanner.column);
		
	}
	
	@Test
	public void interface_is_NOT_found_scanner_column_is_0__int() {
		scanner = new OneLineSimpleTestCharacterScanner("int");
		ExactWordPatternRule rule = new ExactWordPatternRule(new JavaWordDetector(), "interface", token);
		rule.trace=true;
		rule.evaluate(scanner);
		
		assertEquals(0,scanner.column);
		
	}
	
	@Test
	public void interface_is_NOT_found_scanner_column_is_0__in() {
		scanner = new OneLineSimpleTestCharacterScanner("in");
		ExactWordPatternRule rule = new ExactWordPatternRule(new JavaWordDetector(), "interface", token);
		rule.trace=true;
		rule.evaluate(scanner);
		
		assertEquals(0,scanner.column);
		
	}
	
	private class OneLineSimpleTestCharacterScanner implements ICharacterScanner{
		private int column;
		private String text;

		public OneLineSimpleTestCharacterScanner(String text){
			this.text=text;
		}
		

		@Override
		public char[][] getLegalLineDelimiters() {
			char[][] chars = new char[1][];
			chars[0]="\n".toCharArray();
			return chars;
		}

		@Override
		public int getColumn() {
			return column;
		}

		@Override
		public int read() {
			if (column>=text.length()){
				return EOF;
			}
			char c = text.substring(column, column+1).toCharArray()[0];
			column++;
			return c;
		}

		@Override
		public void unread() {
			column--;
			
		}
		
	}

}