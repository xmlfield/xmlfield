package org.xmlfield.tests.date;

import org.joda.time.DateTime;
import org.xmlfield.annotations.FieldXPath;
import org.xmlfield.annotations.ResourceXPath;

@ResourceXPath("/dateStorage")
public interface DateStorage {

	@FieldXPath(value = "date", format = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    DateTime getDate();
	
	void setDate( DateTime time );
	
	
	@FieldXPath(value = "dateDefault")
    DateTime getDateDefault();
	
	void setDateDefault( DateTime time );
}
