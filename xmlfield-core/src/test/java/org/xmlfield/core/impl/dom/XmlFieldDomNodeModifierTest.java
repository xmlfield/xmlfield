package org.xmlfield.core.impl.dom;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xmlfield.core.api.XmlFieldNode;
import org.xmlfield.core.api.XmlFieldNodeList;
import org.xmlfield.core.api.XmlFieldNodeModifier;
import org.xmlfield.core.api.XmlFieldNodeParser;
import org.xmlfield.core.api.XmlFieldSelector;
import org.xmlfield.core.internal.XmlFieldUtils.NamespaceMap;

public class XmlFieldDomNodeModifierTest {

	private final XmlFieldNodeModifier modifier = new DomNodeModifier();

	private XmlFieldNode<Node> node;

	private XmlFieldNode<Node> nodeNs;

	private final XmlFieldNodeParser<Node> parser;

	private final XmlFieldSelector selector = new DomJaxenSelector();

	private final String xml = "<Catalog><Cd><Title>Empire Burlesque</Title><Artist>Bob Dylan</Artist><Country>USA</Country><Company>Columbia</Company><Price>10.90</Price><Year>1985</Year></Cd><Cd><Title>Hide your heart</Title><Artist>Bonnie Tyler</Artist><Country>UK</Country><Company>CBS Records</Company><Price>9.90</Price><Year>1988</Year></Cd><Cd><Title>Greatest Hits</Title><Artist>Dolly Parton</Artist><Country>USA</Country><Company>RCA</Company><Price>9.90</Price><Year>1982</Year></Cd></Catalog>";

	private final String xmlNs = "<Catalog xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><xsi:Cd><xsi:Title>Empire Burlesque</xsi:Title><xsi:Artist>Bob Dylan</xsi:Artist><xsi:Country>USA</xsi:Country><xsi:Company>Columbia</xsi:Company><xsi:Price>10.90</xsi:Price><xsi:Year>1985</xsi:Year></xsi:Cd><xsi:Cd><xsi:Title>Hide your heart</xsi:Title><xsi:Artist>Bonnie Tyler</xsi:Artist><xsi:Country>UK</xsi:Country><xsi:Company>CBS Records</xsi:Company><xsi:Price>9.90</xsi:Price><xsi:Year>1988</xsi:Year></xsi:Cd><xsi:Cd><xsi:Title>Greatest Hits</xsi:Title><xsi:Artist>Dolly Parton</xsi:Artist><xsi:Country>USA</xsi:Country><xsi:Company>RCA</xsi:Company><xsi:Price>9.90</xsi:Price><xsi:Year>1982</xsi:Year></xsi:Cd></Catalog>";

	public XmlFieldDomNodeModifierTest()
			throws TransformerConfigurationException,
			TransformerFactoryConfigurationError {
		parser = new DomNodeParser();
	}

	@Before
	public void setUp() throws Exception {
		node = parser.xmlToNode(xml);
		nodeNs = parser.xmlToNode(xmlNs);
	}

	@Test
	public void testCreateAttribute() throws Exception {
		// test create attribute with all the parameters null
		try {
			modifier.createAttribute(null, null, null);
			fail("No exception thrown");
		} catch (NullPointerException e) {
			assertTrue(true);
		}

		// test create attribute with all the parameters null, except the node
		try {
			modifier.createAttribute(node, null, null);
			fail("No exception thrown");
		} catch (NullPointerException e) {
			assertTrue(true);
		}

		// test create attribute with a null attribute name
		try {
			modifier.createAttribute(node, null, "Test");
			fail("No exception thrown");
		} catch (NullPointerException e) {
			assertTrue(true);
		}

		// test create attribute
		assertThat(node.hasAttributes(), is(false));
		modifier.createAttribute(node, "name", "MyCatalog");
		assertThat(node.hasAttributes(), is(true));
		assertThat(
				parser.nodeToXml(node),
				is("<Catalog name=\"MyCatalog\"><Cd><Title>Empire Burlesque</Title><Artist>Bob Dylan</Artist><Country>USA</Country><Company>Columbia</Company><Price>10.90</Price><Year>1985</Year></Cd><Cd><Title>Hide your heart</Title><Artist>Bonnie Tyler</Artist><Country>UK</Country><Company>CBS Records</Company><Price>9.90</Price><Year>1988</Year></Cd><Cd><Title>Greatest Hits</Title><Artist>Dolly Parton</Artist><Country>USA</Country><Company>RCA</Company><Price>9.90</Price><Year>1982</Year></Cd></Catalog>"));

		modifier.createAttribute(node, "othername", null);
		assertThat(
				parser.nodeToXml(node),
				is("<Catalog name=\"MyCatalog\" othername=\"\"><Cd><Title>Empire Burlesque</Title><Artist>Bob Dylan</Artist><Country>USA</Country><Company>Columbia</Company><Price>10.90</Price><Year>1985</Year></Cd><Cd><Title>Hide your heart</Title><Artist>Bonnie Tyler</Artist><Country>UK</Country><Company>CBS Records</Company><Price>9.90</Price><Year>1988</Year></Cd><Cd><Title>Greatest Hits</Title><Artist>Dolly Parton</Artist><Country>USA</Country><Company>RCA</Company><Price>9.90</Price><Year>1982</Year></Cd></Catalog>"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateElement() throws Exception {
		// test create element with all parameters null
		try {
			modifier.createElement(null, null, null, null);
			fail("No exception thrown");
		} catch (NullPointerException e) {
			assertTrue(true);
		}

		// test create element with a null node
		try {
			modifier.createElement(null, null, "Cd", null);
			fail("No exception thrown");
		} catch (NullPointerException e) {
			assertTrue(true);
		}

		// test create element with an empty or null element name
		assertThat(
				(XmlFieldNode<Node>) modifier.createElement(null, node, null),
				sameInstance(node));
		assertThat((XmlFieldNode<Node>) modifier.createElement(null, node, ""),
				sameInstance(node));

		// test create element without namespaces
		XmlFieldNode<Node> newNode = (XmlFieldNode<Node>) modifier
				.createElement(null, node, "Cd");
		assertThat(parser.nodeToXml(newNode), is("<Cd/>"));
		assertThat(
				parser.nodeToXml(node),
				is("<Catalog><Cd><Title>Empire Burlesque</Title><Artist>Bob Dylan</Artist><Country>USA</Country><Company>Columbia</Company><Price>10.90</Price><Year>1985</Year></Cd><Cd><Title>Hide your heart</Title><Artist>Bonnie Tyler</Artist><Country>UK</Country><Company>CBS Records</Company><Price>9.90</Price><Year>1988</Year></Cd><Cd><Title>Greatest Hits</Title><Artist>Dolly Parton</Artist><Country>USA</Country><Company>RCA</Company><Price>9.90</Price><Year>1982</Year></Cd><Cd/></Catalog>"));

		// test create element with namespaces
		NamespaceMap namespaces = new NamespaceMap(
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
		assertThat((XmlFieldNode<Node>) modifier.createElement(namespaces,
				nodeNs, null), sameInstance(nodeNs));
		assertThat((XmlFieldNode<Node>) modifier.createElement(namespaces,
				nodeNs, ""), sameInstance(nodeNs));
		newNode = (XmlFieldNode<Node>) modifier.createElement(namespaces,
				nodeNs, "xsi:Cd");
		assertThat(parser.nodeToXml(newNode),
				is("<Cd xmlns=\"http://www.w3.org/2001/XMLSchema-instance\"/>"));
		assertThat(
				parser.nodeToXml(nodeNs),
				is("<Catalog xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><xsi:Cd><xsi:Title>Empire Burlesque</xsi:Title><xsi:Artist>Bob Dylan</xsi:Artist><xsi:Country>USA</xsi:Country><xsi:Company>Columbia</xsi:Company><xsi:Price>10.90</xsi:Price><xsi:Year>1985</xsi:Year></xsi:Cd><xsi:Cd><xsi:Title>Hide your heart</xsi:Title><xsi:Artist>Bonnie Tyler</xsi:Artist><xsi:Country>UK</xsi:Country><xsi:Company>CBS Records</xsi:Company><xsi:Price>9.90</xsi:Price><xsi:Year>1988</xsi:Year></xsi:Cd><xsi:Cd><xsi:Title>Greatest Hits</xsi:Title><xsi:Artist>Dolly Parton</xsi:Artist><xsi:Country>USA</xsi:Country><xsi:Company>RCA</xsi:Company><xsi:Price>9.90</xsi:Price><xsi:Year>1982</xsi:Year></xsi:Cd><Cd xmlns=\"http://www.w3.org/2001/XMLSchema-instance\"/></Catalog>"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateElementWithTextContent() throws Exception {
		// test create element with all parameters null
		try {
			modifier.createElement(null, null, null, null);
			fail("No exception thrown");
		} catch (NullPointerException e) {
			assertTrue(true);
		}

		// test create element with a null node
		try {
			modifier.createElement(null, null, "Cd", null);
			fail("No exception thrown");
		} catch (NullPointerException e) {
			assertTrue(true);
		}

		// test create element with an empty or null element name
		assertThat((XmlFieldNode<Node>) modifier.createElement(null, node,
				null, "Some text"), sameInstance(node));
		assertThat((XmlFieldNode<Node>) modifier.createElement(null, node, "",
				"Some text"), sameInstance(node));

		// test create element without namespaces
		XmlFieldNode<Node> newNode = (XmlFieldNode<Node>) modifier
				.createElement(null, node, "Cd", "Some text");
		assertThat(parser.nodeToXml(newNode), is("<Cd>Some text</Cd>"));
		assertThat(
				parser.nodeToXml(node),
				is("<Catalog><Cd><Title>Empire Burlesque</Title><Artist>Bob Dylan</Artist><Country>USA</Country><Company>Columbia</Company><Price>10.90</Price><Year>1985</Year></Cd><Cd><Title>Hide your heart</Title><Artist>Bonnie Tyler</Artist><Country>UK</Country><Company>CBS Records</Company><Price>9.90</Price><Year>1988</Year></Cd><Cd><Title>Greatest Hits</Title><Artist>Dolly Parton</Artist><Country>USA</Country><Company>RCA</Company><Price>9.90</Price><Year>1982</Year></Cd><Cd>Some text</Cd></Catalog>"));

		newNode = (XmlFieldNode<Node>) modifier.createElement(null, node, "Cd",
				null);
		assertThat(parser.nodeToXml(newNode), is("<Cd/>"));
		assertThat(
				parser.nodeToXml(node),
				is("<Catalog><Cd><Title>Empire Burlesque</Title><Artist>Bob Dylan</Artist><Country>USA</Country><Company>Columbia</Company><Price>10.90</Price><Year>1985</Year></Cd><Cd><Title>Hide your heart</Title><Artist>Bonnie Tyler</Artist><Country>UK</Country><Company>CBS Records</Company><Price>9.90</Price><Year>1988</Year></Cd><Cd><Title>Greatest Hits</Title><Artist>Dolly Parton</Artist><Country>USA</Country><Company>RCA</Company><Price>9.90</Price><Year>1982</Year></Cd><Cd>Some text</Cd><Cd/></Catalog>"));

		// test create element with namespaces
		NamespaceMap namespaces = new NamespaceMap(
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
		assertThat((XmlFieldNode<Node>) modifier.createElement(namespaces,
				nodeNs, null, "Some text"), sameInstance(nodeNs));
		assertThat((XmlFieldNode<Node>) modifier.createElement(namespaces,
				nodeNs, "", "Some text"), sameInstance(nodeNs));
		newNode = (XmlFieldNode<Node>) modifier.createElement(namespaces,
				nodeNs, "xsi:Cd", "Some text");
		assertThat(
				parser.nodeToXml(newNode),
				is("<Cd xmlns=\"http://www.w3.org/2001/XMLSchema-instance\">Some text</Cd>"));
		assertThat(
				parser.nodeToXml(nodeNs),
				is("<Catalog xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><xsi:Cd><xsi:Title>Empire Burlesque</xsi:Title><xsi:Artist>Bob Dylan</xsi:Artist><xsi:Country>USA</xsi:Country><xsi:Company>Columbia</xsi:Company><xsi:Price>10.90</xsi:Price><xsi:Year>1985</xsi:Year></xsi:Cd><xsi:Cd><xsi:Title>Hide your heart</xsi:Title><xsi:Artist>Bonnie Tyler</xsi:Artist><xsi:Country>UK</xsi:Country><xsi:Company>CBS Records</xsi:Company><xsi:Price>9.90</xsi:Price><xsi:Year>1988</xsi:Year></xsi:Cd><xsi:Cd><xsi:Title>Greatest Hits</xsi:Title><xsi:Artist>Dolly Parton</xsi:Artist><xsi:Country>USA</xsi:Country><xsi:Company>RCA</xsi:Company><xsi:Price>9.90</xsi:Price><xsi:Year>1982</xsi:Year></xsi:Cd><Cd xmlns=\"http://www.w3.org/2001/XMLSchema-instance\">Some text</Cd></Catalog>"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testInsertBefore() throws Exception {
		// test insert before with all parameters null
		try {
			modifier.insertBefore(null, null, null);
			fail("No exception thrown");
		} catch (NullPointerException e) {
			assertTrue(true);
		}

		// test insert before with all parameters null except the context node
		try {
			modifier.insertBefore(node, null, null);
			fail("No exception thrown");
		} catch (NullPointerException e) {
			assertTrue(true);
		}

		XmlFieldNode<Node> newChild = (XmlFieldNode<Node>) modifier
				.createElement(null, node, "Cd", "Some text");
		// test insert before with a null for the reference node parameter
		try {
			modifier.insertBefore(node, newChild, null);
			fail("No exception thrown");
		} catch (NullPointerException e) {
			assertTrue(true);
		}

		// test insert before with all the parameters
		XmlFieldNode<Node> refChild = (XmlFieldNode<Node>) selector
				.selectXPathToNode(null, "/Catalog/Cd[2]", node);
		modifier.insertBefore(node, newChild, refChild);
		assertThat(
				parser.nodeToXml(node),
				is("<Catalog><Cd><Title>Empire Burlesque</Title><Artist>Bob Dylan</Artist><Country>USA</Country><Company>Columbia</Company><Price>10.90</Price><Year>1985</Year></Cd><Cd>Some text</Cd><Cd><Title>Hide your heart</Title><Artist>Bonnie Tyler</Artist><Country>UK</Country><Company>CBS Records</Company><Price>9.90</Price><Year>1988</Year></Cd><Cd><Title>Greatest Hits</Title><Artist>Dolly Parton</Artist><Country>USA</Country><Company>RCA</Company><Price>9.90</Price><Year>1982</Year></Cd></Catalog>"));

		// test insert before with all the parameters and namespace
		// NamespaceMap namespaces = new
		// NamespaceMap("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
		// refChild = (XmlFieldNode<Node>)
		// selector.selectXPathToNode(namespaces, "/Catalog/xsi:Cd[2]", nodeNs);
		// newChild = (XmlFieldNode<Node>) modifier.createElement(namespaces,
		// nodeNs, "xsi:Cd", "Some text");
		// modifier.insertBefore(nodeNs, newChild, refChild);
		// assertThat(
		// parser.nodeToXml(nodeNs),
		// is("<Catalog xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><xsi:Cd><xsi:Title>Empire Burlesque</xsi:Title><xsi:Artist>Bob Dylan</xsi:Artist><xsi:Country>USA</xsi:Country><xsi:Company>Columbia</xsi:Company><xsi:Price>10.90</xsi:Price><xsi:Year>1985</xsi:Year></xsi:Cd><Cd xmlns=\"http://www.w3.org/2001/XMLSchema-instance\">Some text</Cd><xsi:Cd><xsi:Title>Hide your heart</xsi:Title><xsi:Artist>Bonnie Tyler</xsi:Artist><xsi:Country>UK</xsi:Country><xsi:Company>CBS Records</xsi:Company><xsi:Price>9.90</xsi:Price><xsi:Year>1988</xsi:Year></xsi:Cd><xsi:Cd><xsi:Title>Greatest Hits</xsi:Title><xsi:Artist>Dolly Parton</xsi:Artist><xsi:Country>USA</xsi:Country><xsi:Company>RCA</xsi:Company><xsi:Price>9.90</xsi:Price><xsi:Year>1982</xsi:Year></xsi:Cd></Catalog>"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRemoveAttribute() throws Exception {
		// test if the node is null
		try {
			modifier.removeAttribute(null, "test");
			fail("No exception thrown");
		} catch (NullPointerException e) {
			assertTrue(true);
		}

		// test if the attribute name is null
		try {
			modifier.removeAttribute(node, null);
			fail("No exception thrown");
		} catch (NullPointerException e) {
			assertTrue(true);
		}

		// test if all the parameters are null
		try {
			modifier.removeAttribute(null, null);
			fail("No exception thrown");
		} catch (NullPointerException e) {
			assertTrue(true);
		}

		// test to remove an attibute who doesn't exist
		node = parser
				.xmlToNode("<Catalog name=\"MyCatalog\"><Cd><Title>Empire Burlesque</Title><Artist>Bob Dylan</Artist><Country>USA</Country><Company>Columbia</Company><Price>10.90</Price><Year>1985</Year></Cd><Cd><Title>Hide your heart</Title><Artist>Bonnie Tyler</Artist><Country>UK</Country><Company>CBS Records</Company><Price>9.90</Price><Year>1988</Year></Cd><Cd><Title>Greatest Hits</Title><Artist>Dolly Parton</Artist><Country>USA</Country><Company>RCA</Company><Price>9.90</Price><Year>1982</Year></Cd></Catalog>");
		XmlFieldNode<Node> removedNode = (XmlFieldNode<Node>) modifier
				.removeAttribute(node, "lastname");
		assertThat(removedNode, nullValue());

		// test to remove an attibute
		removedNode = (XmlFieldNode<Node>) modifier.removeAttribute(node,
				"name");
		assertThat(parser.nodeToXml(node), is(xml));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRemoveChild() throws Exception {
		// test to remove a child node with all the parameters null
		try {
			modifier.removeChild(null, null);
			fail("No exception thrown");
		} catch (NullPointerException e) {
			assertTrue(true);
		}

		// test to remove a child node with a null child node
		try {
			modifier.removeChild(node, null);
			fail("No exception thrown");
		} catch (NullPointerException e) {
			assertTrue(true);
		}

		// test to remove a child node with a null context node
		XmlFieldNode<Node> oldChild = (XmlFieldNode<Node>) selector
				.selectXPathToNode(null, "/Catalog/Cd[2]", node);
		try {
			modifier.removeChild(null, oldChild);
			fail("No exception thrown");
		} catch (NullPointerException e) {
			assertTrue(true);
		}

		// test to remove a child node which doesn't exist in the context node
		XmlFieldNode<Node> removedNode = (XmlFieldNode<Node>) modifier
				.removeChild(node, oldChild);
		assertThat(removedNode, sameInstance(oldChild));
		assertThat(
				parser.nodeToXml(node),
				is("<Catalog><Cd><Title>Empire Burlesque</Title><Artist>Bob Dylan</Artist><Country>USA</Country><Company>Columbia</Company><Price>10.90</Price><Year>1985</Year></Cd><Cd><Title>Greatest Hits</Title><Artist>Dolly Parton</Artist><Country>USA</Country><Company>RCA</Company><Price>9.90</Price><Year>1982</Year></Cd></Catalog>"));

		// test to remove a chid node with a real oldchild
		node = parser.xmlToNode(xml);
		oldChild = (XmlFieldNode<Node>) modifier.createElement(null, node,
				"Cd", "Some text");
		removedNode = (XmlFieldNode<Node>) modifier.removeChild(node, oldChild);
		assertThat(removedNode, sameInstance(oldChild));
		assertThat(parser.nodeToXml(node), is(xml));
	}

	@Test
	public void testRemoveChildren() throws Exception {
		// test to remove nodes with a null list
		try {
			modifier.removeChildren(null);
			fail("No exception thrown");
		} catch (NullPointerException e) {
			assertTrue(true);
		}

		// test to remove nodes with an empty list
		modifier.removeChildren(new DomNodeList(
				new ArrayList<XmlFieldNode<?>>()));
		assertThat(parser.nodeToXml(node), is(xml));

		// test to remove nodes with a list
		XmlFieldNodeList list = selector.selectXPathToNodeList(null,
				"/Catalog/Cd[Year < 1986]", node);
		modifier.removeChildren(list);
		assertThat(
				parser.nodeToXml(node),
				is("<Catalog><Cd><Title>Hide your heart</Title><Artist>Bonnie Tyler</Artist><Country>UK</Country><Company>CBS Records</Company><Price>9.90</Price><Year>1988</Year></Cd></Catalog>"));
	}
}
