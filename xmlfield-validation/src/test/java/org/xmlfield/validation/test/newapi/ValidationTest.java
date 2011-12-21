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
package org.xmlfield.validation.test.newapi;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlfield.core.XmlField;
import org.xmlfield.validation.XmlFieldValidator;
import org.xmlfield.validation.XmlFieldValidationException;

/**
 * @author Nicolas Richeton <nicolas.richeton@capgemini.com>
 * @author Mabrouk Belhout
 */
public class ValidationTest {

    Logger log = LoggerFactory.getLogger(ValidationTest.class);

    @Test(expected = XmlFieldValidationException.class)
    public void testValidationErrorOnEmptyDocument() throws Exception {
        XmlField xf = new XmlField();
        Catalog c = xf.newObject(Catalog.class);
        assertNotNull(c);
        c.addToItems();

        XmlFieldValidator xfv = new XmlFieldValidator();
        xfv.ensureValidation(c);

    }

    @Test(expected = XmlFieldValidationException.class)
    public void testValidationErrorOnEmptyName() throws Exception {
        XmlField xf = new XmlField();
        Catalog c = xf.newObject(Catalog.class);
        c.setType(Catalog.CD_CONST1);
        c.addToItems();

        XmlFieldValidator xfv = new XmlFieldValidator();
        xfv.ensureValidation(c);

    }

    @Test(expected = XmlFieldValidationException.class)
    public void testValidationErrorOnEmptyName2() throws Exception {
        XmlField xf = new XmlField();
        Catalog c = xf.newObject(Catalog.class);
        c.setType(Catalog.CD_CONST1);
        c.setName("");
        c.addToItems();

        XmlFieldValidator xfv = new XmlFieldValidator();
        xfv.ensureValidation(c);

    }
    
    @Test(expected = XmlFieldValidationException.class)
    public void testValidationErrorOnSizeName() throws Exception {
        XmlField xf = new XmlField();
        Catalog c = xf.newObject(Catalog.class);
        c.setType(Catalog.CD_CONST1);
        c.addToItems();
        c.setName("a");

        XmlFieldValidator xfv = new XmlFieldValidator();
        xfv.ensureValidation(c);

    }

    @Test(expected = XmlFieldValidationException.class)
    public void testValidationErrorOnInvalidValue() throws Exception {
        XmlField xf = new XmlField();
        Catalog c = xf.newObject(Catalog.class);
        c.setType("invalid value");
        c.setName("Test");
        c.addToItems();

        XmlFieldValidator xfv = new XmlFieldValidator();
        xfv.ensureValidation(c);

    }

    @Test(expected = XmlFieldValidationException.class)
    public void testValidationErrorOnInvalidValue2() throws Exception {
        XmlField xf = new XmlField();
        Catalog c = xf.newObject(Catalog.class);
        c.setType("invalid value");
        c.setName("Test");
        c.setNumber(3);
        c.addToItems();
        
        XmlFieldValidator xfv = new XmlFieldValidator();
        xfv.ensureValidation(c);

    }

    @Test(expected = XmlFieldValidationException.class)
    public void testValidationErrorOnEmptyType() throws Exception {
        XmlField xf = new XmlField();
        Catalog c = xf.newObject(Catalog.class);

        c.setName("Test");
        c.addToItems();

        XmlFieldValidator xfv = new XmlFieldValidator();
        xfv.ensureValidation(c);

    }

    @Test
    public void testValidationSuccess() throws Exception {
        XmlField xf = new XmlField();
        Catalog c = xf.newObject(Catalog.class);
        c.setType(Catalog.CD_CONST2);
        c.setName("Test");
        Item i = c.addToItems();
        i.setName("testName");

        XmlFieldValidator xfv = new XmlFieldValidator();
        xfv.ensureValidation(c);

    }
    
    @Test(expected = XmlFieldValidationException.class)
    public void testValidationErrorOnFailGroup() throws Exception {
        XmlField xf = new XmlField();
        Catalog c = xf.newObject(Catalog.class);
        c.setType(Catalog.CD_CONST2);
        c.setName("Test");
        Item i = c.addToItems();
        i.setName("testName");

        XmlFieldValidator xfv = new XmlFieldValidator();
        xfv.ensureValidation(c, FailGroup.class);

    }

}
