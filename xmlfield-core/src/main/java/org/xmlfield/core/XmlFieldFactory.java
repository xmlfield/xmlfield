package org.xmlfield.core;

import java.util.Map;

/**
 * Factory used to create XmlField instances.
 * <p>
 * This factory is thread safe and can be configured to use ThreadLocal to reuse
 * XmlField instances for performance reasons.
 * 
 * @see CleanThreadLocalFilter
 * 
 * @author Nicolas Richeton
 * 
 */
public class XmlFieldFactory {

	private Boolean getterCache = null;
	private Map<String, String> parserConfiguration = null;
	private boolean useThreadLocal = false;

	/**
	 * XmlFiled instances associated to threads. Used only if useThreadLocal is
	 * true.
	 */
	private final ThreadLocal<XmlField> xmlFieldInstances = new ThreadLocal<XmlField>() {
		@Override
		protected XmlField initialValue() {
			XmlField xf = new XmlField(parserConfiguration);
			if (getterCache != null) {
				xf.setGetterCache(getterCache);
			}
			return xf;
		}
	};

	public XmlFieldFactory() {
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
		this.getterCache = enabled;
	}

	/**
	 * Set parser configuration. All XmlField instances will use this
	 * configuration.
	 * 
	 * @param configuration
	 */
	public void setParserConfiguration(Map<String, String> configuration) {
		parserConfiguration = configuration;
	}

	/**
	 * When enabled, the factory will create only one XmlField object per thread
	 * and reuse it each time {@link #getXmlField()} is called.
	 * <p>
	 * This improves performance if {@link #getXmlField()} is called several
	 * times in the same thread. But it is still better to keep the XmlField
	 * object and reuse it because it it not subject to synchronization.
	 * 
	 * <p>
	 * When used in a web application, it is recommended to use a servlet filter
	 * which clears the context after each call to prevent leaks in application
	 * servers.
	 * 
	 * @see CleanThreadLocalFilter
	 * 
	 * @param useThreadLocal
	 */
	public void setUseThreadLocal(boolean useThreadLocal) {
		this.useThreadLocal = useThreadLocal;
	}

}
