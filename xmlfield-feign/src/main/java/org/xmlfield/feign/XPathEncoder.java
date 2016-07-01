package org.xmlfield.feign;

import java.lang.reflect.Type;

import org.xmlfield.core.XmlField;
import org.xmlfield.core.exception.XmlFieldParsingException;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;

/**
 * Encoder which can be used to marshall with XMLField. Inspired by the Feign JaxbEncoder :
 * https://github.com/Netflix/feign/blob/master/jaxb/src/main/java/feign/jaxb/JAXBEncoder.java
 * 
 * @author Idriss Neumann <neumann.idriss@gmail.com>
 *
 */
public class XPathEncoder implements Encoder {
  private XmlField xf = new XmlField();

  /**
   * {@inheritDoc}
   */
  @Override
  public void encode(Object object, Type bodyType, RequestTemplate template)
      throws EncodeException {

    try {
      template.body(xf.objectToXml(object));
    } catch (XmlFieldParsingException e) {
      throw new EncodeException(e.toString(), e);
    }
  }
}
