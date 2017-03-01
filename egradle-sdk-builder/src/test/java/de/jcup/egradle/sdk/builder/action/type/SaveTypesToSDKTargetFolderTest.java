package de.jcup.egradle.sdk.builder.action.type;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SaveTypesToSDKTargetFolderTest {

	private SaveTypesToSDKTargetFolder actionToTest;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Before
	public void before(){
		actionToTest = new SaveTypesToSDKTargetFolder();
	}
	
	@Test
	public void subpath_is_reduced_without_parent__when_parent_in_subpath() throws Exception{
		/* prepare */
		File parent = new File("parent");
		File file = new File("parent/child1/child2/child3/Test.xml");
		
		/* execute */
		String subPath = actionToTest.extractSubPathFromFile(file, parent);
		
		/* test */
		assertEquals("child1/child2/child3/Test.xml",subPath);
		
	}
	
	@Test
	public void when_parent_is_not_parent_of_file__illegal_argument_exception_is_thrown() throws Exception{
		/* prepare */
		File parent = new File("wrongParent");
		File file = new File("parent/child1/child2/child3/Test.xml");
		
		/* test on next execute*/
		expectedException.expect(IllegalArgumentException.class);
		
		/* execute */
		actionToTest.extractSubPathFromFile(file, parent);
		
	}

}