package de.jcup.egradle.codeassist.dsl;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.jcup.egradle.core.TestUtil;

public class XMLDSLPluginsImporterTest {

	private XMLPluginsImporter importerToTest;
	private File dslFolder;
	private File pluginsXMLFile;

	@Before
	public void before(){
		importerToTest = new XMLPluginsImporter();
		dslFolder = new File(TestUtil.SRC_TEST_RES_FOLDER,"dsl/3.0");
		pluginsXMLFile = new File(dslFolder,"plugins.xml");
	}
	
	@Test
	public void java_plugin_found_test_res__and_contains_mixin_and_extension_classes() throws Exception{
		
		 XMLPlugins xmlPlugins = importerToTest.importPlugins(new FileInputStream(pluginsXMLFile));
		 Set<Plugin> plugins = xmlPlugins.getPlugins();
		 
		 assertNotNull(plugins);
		 for (Plugin plugin: plugins){
			 if ("java".equals(plugin.getId())){
				 assertEquals("Java Plugin", plugin.getDescription());
				 Set<TypeExtension> extensions = plugin.getExtensions();
				 assertNotNull(extensions);
				 assertEquals(3,extensions.size());
				 List<String>mixinsFound=new ArrayList<>();
				 for (TypeExtension extension: extensions){
					 assertTrue(extension instanceof XMLTypeExtension);
					 XMLTypeExtension xmlTe= (XMLTypeExtension) extension;
					 assertEquals("org.gradle.api.Project", xmlTe.getTargetTypeAsString());
					 String extType = xmlTe.getExtensionTypeAsString();
					 String mixinType = xmlTe.getMixinTypeAsString();
					 if (mixinType!=null){
						 mixinsFound.add(mixinType);
					 }
					 if (extType!=null){
						 assertEquals("reporting",extension.getId());
					 }
					 
				 }
				 assertTrue(mixinsFound.contains("org.gradle.api.plugins.BasePluginConvention"));
				 assertTrue(mixinsFound.contains("org.gradle.api.plugins.JavaPluginConvention"));
				 assertEquals(2,mixinsFound.size());
				 /* OK, Test done */
				 return;
			 }
		 }
		 fail("Java plugin not found in test resource plugins.xml");
	}

	
}