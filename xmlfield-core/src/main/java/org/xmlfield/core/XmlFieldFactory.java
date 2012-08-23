package org.xmlfield.core;

/**
 * Factory used to create XmlField instances.
 * <p>
 * This factory is thread safe and can be configured to use ThreadLocal to reuse
 * XmlField instances for performance reasons.
 * 
 * @author Nicolas Richeton
 * 
 */
public class XmlFieldFactory {

	/**
	 * XmlFiled instances associated to threads. Used only if useThreadLocal is
	 * true.
	 */
	private static final ThreadLocal<XmlField> xmlFieldInstances = new ThreadLocal<XmlField>() {
		@Override
		protected XmlField initialValue() {
			return new XmlField();
		}
	};

	boolean useThreadLocal = false;

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

		return new XmlField();
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
	 * @param useThreadLocal
	 */
	public void setUseThreadLocal(boolean useThreadLocal) {
		this.useThreadLocal = useThreadLocal;
	}

}
