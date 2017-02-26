package de.jcup.egradle.codeassist.dsl.gradle;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

import de.jcup.egradle.codeassist.dsl.DSLFileLoader;
import de.jcup.egradle.codeassist.dsl.ModifiableType;
import de.jcup.egradle.codeassist.dsl.Plugin;
import de.jcup.egradle.codeassist.dsl.PluginMerger;
import de.jcup.egradle.codeassist.dsl.Type;
import de.jcup.egradle.codeassist.dsl.XMLType;

public class GradleDSLTypeProviderTest {

	private DSLFileLoader mockedDSLFileLoader;
	private GradleDSLTypeProvider typeProviderToTest;

	//// C:\dev_custom\projects\JCUP\gradle\subprojects\docs\build\src-egradle\egradle-dsl
	@Before
	public void before(){
		mockedDSLFileLoader = mock(DSLFileLoader.class);
		
		typeProviderToTest = new GradleDSLTypeProvider(mockedDSLFileLoader);
	}
	
	@Test
	public void type_provider_resolves_super_type_for_type_and_set_instance_into_type() throws Exception{
		/* prepare */
		ModifiableType mockedType = mock(ModifiableType.class);
		when(mockedType.getSuperTypeAsString()).thenReturn("superType");
		Type mockedSuperType = mock(Type.class);

		when(mockedDSLFileLoader.loadType("type")).thenReturn(mockedType);
		when(mockedDSLFileLoader.loadType("superType")).thenReturn(mockedSuperType);
		
		/* execute */
		typeProviderToTest.getType("type");

		/* test */
		verify(mockedType).extendFromSuperClass(mockedSuperType);
	}
	
	@Test
	public void type_provider_resolves_super_types_and_its_superSuperType_for_type_and_set_instance_into_type() throws Exception{
		/* prepare */
		ModifiableType mockedType = mock(ModifiableType.class);
		when(mockedType.getSuperTypeAsString()).thenReturn("superType");
		
		ModifiableType mockedSuperType = mock(ModifiableType.class);
		when(mockedSuperType.getSuperTypeAsString()).thenReturn("superSuperType");
		
		Type mockedSuperSuperType = mock(Type.class);

		when(mockedDSLFileLoader.loadType("type")).thenReturn(mockedType);
		when(mockedDSLFileLoader.loadType("superType")).thenReturn(mockedSuperType);
		when(mockedDSLFileLoader.loadType("superSuperType")).thenReturn(mockedSuperSuperType);
		
		/* execute */
		typeProviderToTest.getType("type");

		/* test */
		verify(mockedType).extendFromSuperClass(mockedSuperType);
		verify(mockedSuperType).extendFromSuperClass(mockedSuperSuperType);
	}
	
	@Test
	public void type_provider_calls_plugin_merger_with_loaded_type_and_all_plugins() throws Exception{
		/* prepare */
		Set<Plugin> plugins = new LinkedHashSet<>();
		XMLType type = mock(XMLType.class);
		
		PluginMerger mockedPluginMerger = mock(PluginMerger.class);
		typeProviderToTest.merger=mockedPluginMerger;
		
		when(mockedDSLFileLoader.loadPlugins()).thenReturn(plugins);
		when(mockedDSLFileLoader.loadApiMappings()).thenReturn(Collections.emptyMap());
		when(mockedDSLFileLoader.loadType("typename")).thenReturn(type);
		
		/* execute */
		typeProviderToTest.getType("typename");
		
		/* test */
		verify(mockedPluginMerger).merge(type, plugins);
	}
	
	@Test
	public void type_provider_loads_mappings_by_apimapping_importer_only_one_time_next_type_resolving_for_other_type_does_not_load() throws Exception{
		/* prepare */
		Map<String, String> map = new TreeMap<>();
		map.put("shortName1","long.name1");
		map.put("shortName2","long.name2");
		
		when(mockedDSLFileLoader.loadApiMappings()).thenReturn(map);
		
		Type longName1Type =  mock(Type.class);
		Type longName2Type =  mock(Type.class);
		when(mockedDSLFileLoader.loadType("long.name1")).thenReturn(longName1Type);
		when(mockedDSLFileLoader.loadType("long.name2")).thenReturn(longName2Type);
		
		/* execute */
		Type typeResultForShortName1 = typeProviderToTest.getType("shortName1");
		Type typeResultForShortName2 = typeProviderToTest.getType("shortName2");
		
		/* test */
		verify(mockedDSLFileLoader,times(1)).loadApiMappings();
		assertEquals(typeResultForShortName1,longName1Type);
		assertEquals(typeResultForShortName2,longName2Type);
	}
	
	@Test
	public void when_file_loader_cannot_find_type__null_5_calls_to_type_provider_will_only_call_fileloader_one_time() throws Exception {
		/* execute */
		for (int i=0;i<5;i++){
			typeProviderToTest.getType("something");
		}
		/* test */
		verify(mockedDSLFileLoader,times(1)).loadType("something");
	}
	
	@Test
	public void when_file_loader_can_find_type__null_5_calls_to_type_provider_will_only_call_fileloader_one_time() throws Exception {
		/* prepare */
		Type value = mock(Type.class);
		when(mockedDSLFileLoader.loadType("something")).thenReturn(value);
		
		/* execute */
		for (int i=0;i<5;i++){
			typeProviderToTest.getType("something");
		}
		/* test */
		verify(mockedDSLFileLoader,times(1)).loadType("something");
	}

	@Test
	public void when_file_loader_cannot_find_type__result_is_null() throws Exception  {
		/* prepare */
		when(mockedDSLFileLoader.loadType("something")).thenReturn(null);
		
		/* execute + test */
		assertNull(typeProviderToTest.getType("something"));
	}
	
	@Test
	public void when_file_loader_can_find_type__provider_result_is_file_loader_result() throws Exception {
		/* prepare */
		Type value = mock(Type.class);
		when(mockedDSLFileLoader.loadType("something")).thenReturn(value);
		
		/* execute */
		assertEquals(value, typeProviderToTest.getType("something"));
		
	}
	
}
