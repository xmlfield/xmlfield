/*
 * Copyright 2010 Capgemini
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 */
package org.xmlfield.tests.performance.shakspeare;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlfield.core.XmlFieldBinder;
import org.xmlfield.core.impl.DefaultXmlFieldSelectorTest;

/**
 * @author Olivier Lafon
 */
@Ignore
public class TestPack9Test {

    private final XmlFieldBinder binder = new XmlFieldBinder();


    String hugeRomeoAndFatPrincess;
    @Before
    public void setUp() throws Exception {
        File fichierTest = createFileFromClasspathResource("/r_and_j.xml");
        hugeRomeoAndFatPrincess = FileUtils.readFileToString(fichierTest, "UTF-8");

    }
    /**
     * Méthode qui uploade un fichier présent dans le classpath
     * 
     * @param resourceUrl
     *            l'url de la ressource
     * @return le fichier uploadé
     * @throws IOException
     */

    public File createFileFromClasspathResource(String resourceUrl) throws IOException {
        File fichierTest = File.createTempFile("xmlFieldTestFile", "");
        FileOutputStream fos = new FileOutputStream(fichierTest);
        InputStream is = DefaultXmlFieldSelectorTest.class.getResourceAsStream(resourceUrl);

        IOUtils.copy(is, fos);
        is.close();
        fos.close();
        return fichierTest;
    }
    
    @Test
    public void testPerfSelectXPathToString() throws Exception {
        StringBuilder sb = new StringBuilder();
        Play fatRomeo = binder.bindReadOnly(hugeRomeoAndFatPrincess, Play.class);
        for (Act act : fatRomeo.getActs()) {
            sb.append(act.getPrologueTitle());
            for (Scene scene : act.getScenes()) {
                sb.append(scene.getTitle());
                sb.append(scene.getStageDir());
                for (Speech speech : scene.getSpeeches()) {
                    sb.append(speech.getSpeaker());
                    for (String line : speech.getLines()) {
                        sb.append(line);
                    }
                }
            }
        }
        assertTrue( StringUtils.isNotEmpty(sb.toString()));
    }
}
