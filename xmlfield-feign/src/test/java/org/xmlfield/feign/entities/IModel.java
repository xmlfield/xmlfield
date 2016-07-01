package org.xmlfield.feign.entities;

import org.xmlfield.annotations.FieldXPath;
import org.xmlfield.annotations.Namespaces;
import org.xmlfield.annotations.ResourceXPath;

/**
 * Model class test for XML resources atom_test.xml.
 * 
 * @author Idriss Neumann <neumann.idriss@gmail.com>
 *
 */
@Namespaces({"xmlns:a=http://www.w3.org/2005/Atom", "xmlns:x=http://www.w3.org/1999/xhtml"})
@ResourceXPath("/a:entry")
public interface IModel {
  @FieldXPath("a:title/x:div/x:span[@class='name']")
  String getName();

  @FieldXPath("a:title/x:div/x:span[@class='format']")
  String getFormat();
}
