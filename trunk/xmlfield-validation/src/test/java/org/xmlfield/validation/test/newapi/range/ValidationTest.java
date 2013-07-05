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
package org.xmlfield.validation.test.newapi.range;

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
    public void testValidationErrorOnInteger0() throws Exception {
        XmlField xf = new XmlField();
        IIntegerModel c = xf.newObject(IIntegerModel.class);
        assertNotNull(c);
        c.setInteger0(12);

        XmlFieldValidator xfv = new XmlFieldValidator();
        xfv.ensureValidation(c);

    }
    
    
    @Test(expected = XmlFieldValidationException.class)
    public void testValidationErrorOnInteger1() throws Exception {
        XmlField xf = new XmlField();
        IIntegerModel c = xf.newObject(IIntegerModel.class);
        assertNotNull(c);
        c.setInteger1(8);
        c.setInteger0(12);
        XmlFieldValidator xfv = new XmlFieldValidator();
        xfv.ensureValidation(c);

    }

    @Test
    public void testValidationSuccessInteger1() throws Exception {
        XmlField xf = new XmlField();
        IIntegerModel c = xf.newObject(IIntegerModel.class);
        c.setInteger0(6);
        c.setInteger1(8);
        XmlFieldValidator xfv = new XmlFieldValidator();
        xfv.ensureValidation(c);

    }

    
    
    @Test(expected = XmlFieldValidationException.class)
    public void testValidationErrorOnLong0() throws Exception {
        XmlField xf = new XmlField();
        ILongModel c = xf.newObject(ILongModel.class);
        assertNotNull(c);
        c.setLong0(12);

        XmlFieldValidator xfv = new XmlFieldValidator();
        xfv.ensureValidation(c);

    }
    
    
    @Test(expected = XmlFieldValidationException.class)
    public void testValidationErrorOnLong1() throws Exception {
        XmlField xf = new XmlField();
        ILongModel c = xf.newObject(ILongModel.class);
        assertNotNull(c);
        c.setLong1(8);
        c.setLong0(12);
        XmlFieldValidator xfv = new XmlFieldValidator();
        xfv.ensureValidation(c);

    }

    @Test
    public void testValidationSuccessLong1() throws Exception {
        XmlField xf = new XmlField();
        ILongModel c = xf.newObject(ILongModel.class);
        c.setLong0(6);
        c.setLong1(8);
        XmlFieldValidator xfv = new XmlFieldValidator();
        xfv.ensureValidation(c);

    }

}
