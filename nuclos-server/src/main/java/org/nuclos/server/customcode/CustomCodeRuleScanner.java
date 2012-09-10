package org.nuclos.server.customcode;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.nuclos.server.customcode.codegenerator.RuleClassLoader;
import org.nuclos.server.eventsupport.valueobject.EventSupportSourceVO;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

//Copyright (C) 2012  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.
public class CustomCodeRuleScanner 
{
	private static final Logger log = Logger.getLogger(CustomCodeRuleScanner.class);

	private RuleClassLoader cl;

	private ServletContext servletContext;
	
	public CustomCodeRuleScanner (RuleClassLoader rcl, ServletContext ctx) 
	{
		this.cl = rcl;
		this.servletContext = ctx;
	}

	/**
	 * This method returns all loadable classes that implement one of the RuleInterfaces
	 * 
	 * @param listOfAllowedInterfaces
	 * @return
	 * @throws IOException 
	 */
	public Map<String, EventSupportSourceVO> getExecutableRulesFromClasspath(Class<?>... listOfAllowedInterfaces) throws IOException
	{
		Map<String, EventSupportSourceVO> execRuleClasses = new HashMap<String, EventSupportSourceVO>();
	
		Environment env = new StandardEnvironment();
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(this.cl);				
		MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
	
		// Directories and JARs stored in the Classpath as URLs (e.g. Nuclet.jar)
		List<File> urls = new ArrayList<File>();
		for (URL u :this.cl.getURLs() )
		{
			urls.add(new File(u.getFile()));
		}
		
		// With extensions additional jars might be added to the /WEB-INF/lib/ directory
		// and so to the classpath
		Iterator iterator = servletContext.getResourcePaths("/WEB-INF/lib/").iterator();

		while (iterator.hasNext())
		{
			String foundJarFile = (String) iterator.next();
			// Only Nuclos-Extensions scanned
			if (foundJarFile != null && foundJarFile.contains("-server-"))
			{
				// Create real path to found jar file for class scanning			
				urls.add(new File (servletContext.getRealPath("/") + foundJarFile));				
			}
		}
		
		for (File u : urls)
		{
			// Search for classes within the current ClassLoader-URLs for loading classes and resources
			ArrayList<File> searchFile = searchClassFilesInDirectory(u, ".class");
			
			for (File curFile :searchFile)
			{
				// Extract filename and filepath to retrieve Resourceinformation 
				String pfad = curFile.getPath().substring(0, curFile.getPath().indexOf(curFile.getName()));
				String converterdResourcePath = 
						ClassUtils.convertClassNameToResourcePath(env.resolveRequiredPlaceholders(pfad));
				
				// get the resource
				Resource resource = resourcePatternResolver.getResource(converterdResourcePath + curFile.getName());
				
				try {
					// Get the metainformation of the resource
					MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
					if (metadataReader.getClassMetadata() != null)
					{
						for (String curIF : metadataReader.getClassMetadata().getInterfaceNames())
						{
							for (Class inter : listOfAllowedInterfaces)
							{
								// If the resource implements one of the ruleInterfaces the resource will
								// be listed as an executable eventsupport
								if (curIF != null && curIF.contains(inter.getName()))
								{
									ClassMetadata classMetadata = metadataReader.getClassMetadata();
									AnnotationMetadata anmeta = metadataReader.getAnnotationMetadata();
									
									String ruleName = resource.getFilename();
									String ruleDescription = classMetadata.getClassName();
									String ruleClassName = classMetadata.getClassName();
									Date ruleClassCompilationDate = new Date(resource.lastModified());

									String rulePackagePath = ClassUtils.convertResourcePathToClassName(env.resolveRequiredPlaceholders(env.resolveRequiredPlaceholders(pfad)));
									if (rulePackagePath != null && rulePackagePath.trim().length() > 0 && 
											rulePackagePath.lastIndexOf(".") == rulePackagePath.length() - 1)
										rulePackagePath = rulePackagePath.substring(0, rulePackagePath.length()-1);
									
									if (anmeta.hasAnnotation("org.nuclos.api.annotation.NuclosEvent"))
									{
										Map<String, Object> annotationAttributes = anmeta.getAnnotationAttributes("org.nuclos.api.annotation.NuclosEvent");
										if (annotationAttributes.containsKey("name"))
										{
											ruleName = (String) annotationAttributes.get("name");
										}
										if (annotationAttributes.containsKey("description"))
										{
											ruleDescription = (String) annotationAttributes.get("description");
										}										
									}

									if (!execRuleClasses.containsKey(ruleName)) {
										List<String> newListOfInterfaces = new ArrayList<String>();
										newListOfInterfaces.add(inter.getName());
										EventSupportSourceVO newRule = 
												new EventSupportSourceVO(ruleName, ruleDescription, ruleClassName, newListOfInterfaces, rulePackagePath, ruleClassCompilationDate);
										execRuleClasses.put(ruleName, newRule);
									}
									else {
										EventSupportSourceVO eventSupportSourceVO = execRuleClasses.get(ruleName);
										eventSupportSourceVO.getInterface().add(inter.getName());
									}
								}	
							}
						}	
					}
				} catch (IOException e) {
					log.warn("Could not read MetadataInformation of Resource " + resource.getFilename());
				}
			}	
		}
	
		return execRuleClasses;
	}
    
	
	/**
	 * This methods scans the given directoy and all subdirectories and returns a list of
	 * all files that correspond with the given filePattern
	 * 
	 * @param dir - Directory oder File to search in
	 * @param filenamePattern - value to search for
	 * @return List of all files that correspond with the filenamePattern within the given directory
	 */
	private ArrayList<File> searchClassFilesInDirectory(File root, String filenamePattern) {

		ArrayList<File> matches = new ArrayList<File> ();
		
		if (root.isFile())
		{
			// Does the found file correpsond with the filepattern
			if (root.getName().endsWith(filenamePattern)) { 
				matches.add(root);
			}
			else if (root.getName().endsWith(".jar"))
			{
				try {
		        	JarFile jarFile = new JarFile(root);
		        	Enumeration<JarEntry> jarEntries = jarFile.entries();
					while(jarEntries.hasMoreElements())
					{
						JarEntry foundElement = jarEntries.nextElement();
						if (foundElement.getName().contains(filenamePattern))
						{
							File myFile = new File(foundElement.getName());
							matches.add(myFile);
						}
					}
				} catch (IOException e) {
					log.warn("jar-File '" + root.getName() + "' could not be opened for scanning " + e);
				}
			}
		}
		else if (root.isDirectory())
		{
			// Recursive call for subdirectories
			matches.addAll(searchClassFilesInDirectory(root, filenamePattern)); 
		}
		
		return matches;
	}
	
}
