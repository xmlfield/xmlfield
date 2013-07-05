package org.xmlfield.core.internal;

import static org.apache.commons.lang.StringUtils.substringAfter;
import static org.apache.commons.lang.StringUtils.substringBetween;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.xmlfield.annotations.Namespaces;

/**
 * Namespaces container class.
 * 
 * @author PGMY03781
 * @author Nicolas Richeton
 * 
 */
public class NamespaceMap implements
		Iterable<Map.Entry<String, String>> {

	private final Map<String, String> prefixesURIs = new HashMap<String, String>();
	private String stringValue = "";

	NamespaceMap(final Namespaces namespaces) {
		this(namespaces == null ? new String[0] : namespaces.value());
	}

	public NamespaceMap(final String... namespaces) {
		if (namespaces != null) {
			for (final String n : namespaces) {
				final String prefix = substringBetween(n, ":", "=");

				if (prefix == null) {

					throw new IllegalArgumentException(
							"Illegal namespace prefix for XPath expressions: "
									+ n);
				}

				final String uri = substringAfter(n, "=").replace("\"", "");
				prefixesURIs.put(prefix, uri);
			}
			updateToString();
		}
	}

	void addNamespaces(final NamespaceMap nMap) {
		if (nMap != null) {
			prefixesURIs.putAll(nMap.prefixesURIs);
			updateToString();
		}
	}

	public String get(String prefix) {
		return prefixesURIs.get(prefix);
	}

	public Map<String, String> getPrefixesURIs() {
		return prefixesURIs;
	}

	public boolean isEmpty() {
		return prefixesURIs.isEmpty();
	}

	@Override
	public Iterator<Entry<String, String>> iterator() {
		return prefixesURIs.entrySet().iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return stringValue;

	}

	/**
	 * Updates toString representation. The representation is stored
	 * internally to speed up toString, as the value will not change in most
	 * cases after creation.
	 */
	private void updateToString() {
		if (!prefixesURIs.isEmpty()) {
			StringBuilder builder = new StringBuilder();
			for (Entry<String, String> entry : this) {
				builder.append(entry.getKey());
				builder.append(":");
				builder.append(entry.getValue());
				builder.append(",");
			}
			stringValue = builder.toString();
		}
	}

}