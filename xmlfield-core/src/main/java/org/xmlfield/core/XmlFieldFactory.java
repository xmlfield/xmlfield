package org.xmlfield.core;

import java.util.Map;

/**
 * Factory used to create XmlField instances.
 * <p>
 * This factory is thread safe and can be configured to use ThreadLocal to reuse
 * XmlField instances for performance reasons.
 * <p>
 * When using ThreadLocal, configuration ( parserConfiguration and getter cache)
 * is unique (static) across all XmlFieldFactory instances.
 * 
 * 
 * @see CleanThreadLocalFilter
 * 
 * @author Nicolas Richeton
 * 
 */
public class XmlFieldFactory {

	private static Boolean staticGetterCache = null;
	private static Map<String, String> staticParserConfiguration = null;
	/**
	 * XmlFiled instances associated to threads. Used only if useThreadLocal is
	 * true.
	 */
	private static final ThreadLocal<XmlField> xmlFieldInstances = new ThreadLocal<XmlField>() {
		@Override
		protected XmlField initialValue() {
			XmlField xf = new XmlField(staticParserConfiguration);
			if (staticGetterCache != null) {
				xf.setGetterCache(staticGetterCache);
			}
			return xf;
		}
	};

	private Boolean getterCache = null;
	private Map<String, String> parserConfiguration = null;

	private final boolean useThreadLocal;

	public XmlFieldFactory() {
		this(false);
	}

	/**
	 * @param useThreadLocal
	 *            When enabled, the factory will create only one XmlField object
	 *            per thread and reuse it each time {@link #getXmlField()} is
	 *            called.
	 *            <p>
	 *            This improves performance if {@link #getXmlField()} is called
	 *            several times in the same thread. But it is still better to
	 *            keep the XmlField object and reuse it because it it not
	 *            subject to synchronization.
	 * 
	 *            <p>
	 *            When used in a web application, it is recommended to use a
	 *            servlet filter which clears the context after each call to
	 *            prevent leaks in application servers.
	 * 
	 * @see CleanThreadLocalFilter
	 */
	public XmlFieldFactory(boolean useThreadLocal) {
		this.useThreadLocal = useThreadLocal;
	}

	/**
	 * Clean the XmlField instance of the current thread.
	 */
	public void cleanThreadLocal() {
		if (useThreadLocal) {
			xmlFieldInstances.remove();
		}
	}

	/**
	 * Get a new XmlField instance, or the instance associated with the current
	 * thread if {@link #setUseThreadLocal(boolean)} is enabled.
	 * 
	 * @return
	 */
	public XmlField getXmlField() {
		if (useThreadLocal) {
			return xmlFieldInstances.get();
		}

		XmlField xf = new XmlField(parserConfiguration);
		if (getterCache != null) {
			xf.setGetterCache(getterCache);
		}

		return xf;
	}

	/**
	 * Enable Getter cache.
	 * 
	 * @see XmlField#setGetterCache(boolean)
	 * @param enabled
	 */
	public void setGetterCache(boolean enabled) {
		if (useThreadLocal) {
			staticGetterCache = enabled;
		} else {
			this.getterCache = enabled;
		}
	}

	/**
	 * Set parser configuration. All XmlField instances will use this
	 * configuration.
	 * 
	 * @param configuration
	 */
	public void setParserConfiguration(Map<String, String> configuration) {
		if (useThreadLocal) {
			staticParserConfiguration = configuration;
		} else {
			parserConfiguration = configuration;
		}
	}

}
