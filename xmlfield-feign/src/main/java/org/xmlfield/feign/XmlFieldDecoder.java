package org.xmlfield.feign;

import java.io.IOException;
import java.lang.reflect.Type;

import org.apache.commons.io.IOUtils;
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
  private static final String DEFAULT_ENCODING = "UTF-8";

  private String encoding;

  /**
   * Instanciate the decoder with the specified encoding.
   * 
   * @param encoding
   */
  public XmlFieldDecoder(String encoding) {
    this.encoding = encoding;
  }

  /**
   * Instanciate the decoder with the default encoding (UTF-8).
   */
  public XmlFieldDecoder() {
    this(DEFAULT_ENCODING);
  }

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
      return XmlFieldFactory.getInstance().xmlToObject(
          IOUtils.toString(response.body().asInputStream(), encoding), (Class<?>) type);
    } catch (XmlFieldParsingException e) {
      throw new DecodeException(e.toString(), e);
    } finally {
      if (response.body() != null) {
        response.body().close();
      }
    }
  }
}
