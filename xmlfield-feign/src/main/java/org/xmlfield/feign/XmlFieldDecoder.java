package org.xmlfield.feign;

import java.io.IOException;
import java.lang.reflect.Type;

import org.xmlfield.core.XmlField;
import org.xmlfield.core.exception.XmlFieldParsingException;

import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.Decoder;

/**
 * Decoder which can be used to unmarshall with XMLField.<br />
 * Inspired by the JaxbDecoder from feign :
 * https://github.com/Netflix/feign/blob/master/jaxb/src/main/java/feign/jaxb/JAXBDecoder.java
 * 
 * @author Idriss Neumann<neumann.idriss@gmail.com>
 *
 */
public class XmlFieldDecoder implements Decoder {
  private XmlField xf = new XmlField();

  /**
   * {@inheritDoc}
   */
  @Override
  public Object decode(Response response, Type type)
      throws IOException, DecodeException, FeignException {

    if (response.status() == 404) {
      return Util.emptyValueOf(type);
    }

    if (response.body() == null) {
      return null;
    }

    try {
      return xf.xmlToObject(response.body().toString(), (Class<?>) type);
    } catch (XmlFieldParsingException e) {
      throw new DecodeException(e.toString(), e);
    } finally {
      if (response.body() != null) {
        response.body().close();
      }
    }
  }
}
