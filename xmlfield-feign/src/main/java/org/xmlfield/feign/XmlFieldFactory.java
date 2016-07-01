package org.xmlfield.feign;

import org.xmlfield.core.XmlField;

/**
 * A factory for a XmlField instance.
 * 
 * @author Idriss Neumann <neumann.idriss@gmail.com>
 *
 */
public class XmlFieldFactory {
  private static XmlField xf;

  /**
   * Return a single instance of XmlField.
   * 
   * @return XmlField
   */
  public static XmlField getInstance() {
    if (null == xf) {
      xf = new XmlField();
    }

    return xf;
  }

  /**
   * Private constructor.
   */
  private XmlFieldFactory() {

  }
}
