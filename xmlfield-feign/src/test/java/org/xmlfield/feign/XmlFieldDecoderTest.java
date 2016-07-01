package org.xmlfield.feign;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.xmlfield.feign.entities.IModel;

import feign.Response;

/**
 * Test of the decoder (unmarshalling xml feed in a java XMLField interface).
 * 
 * @author Idriss Neumann <neumann.idriss@gmail.com>
 *
 */
public class XmlFieldDecoderTest {
  private XmlFieldDecoder decoder;

  @Before
  public void setUp() throws Exception {
    decoder = new XmlFieldDecoder();
  }


  private File getFileInClasspath(String filename) {
    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource(filename).getFile());
    return file;
  }

  @Test
  public void decodeTestNominal() throws Exception {
    File fileInClasspath = getFileInClasspath("atom_test.xml");
    byte[] data = Files.readAllBytes(fileInClasspath.toPath());

    Map<String, Collection<String>> headers = new HashMap<String, Collection<String>>();

    Response response = Response.create(200, "reason", headers, data);

    IModel model = (IModel) decoder.decode(response, IModel.class);

    assertEquals("CD Catalog", model.getName());
    assertEquals("Atom", model.getFormat());
  }
}
