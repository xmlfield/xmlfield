/*
 * Copyright 2010 Capgemini
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 */
package org.xmlfield.utils;

import static org.junit.Assert.assertTrue;
import static org.xmlfield.utils.XmlUtils.xmlToXmlFieldNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.xpath.XPath;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.xmlfield.core.XmlFieldNode;

/**
 * Test class of the JaxpUtils tool in a multithreaded environment.
 * 
 * @author Guillaume Mary <guillaume.mary@capgemini.com>
 */
public class JaxpUtilsPerformanceTest {

    private static final int TASK_COUNT = 100;

    private static final int THREAD_POOL_SIZE = 2;

    private static final int XPATH_REQUEST_COUNT = 100;

    /**
     * Test that the static getXPath method works fine in a multithreaded environment.
     * 
     * @throws Exception
     *             errors.
     */
    @Test
    public void testGetXPath() throws Exception {
        // prepare tasks to be executed
        Callable<Result> task = new Callable<Result>() {

            @Override
            public Result call() throws Exception {
                Result testResult = new Result();
                RunListener testListener = testResult.createListener();
                Description description = Description.createTestDescription(getClass(), "testGetXPath");
                String xml = "<Catalog><Cd> <Title>Empire Burlesque</Title> <Artist>Bob Dylan</Artist>  <Country>USA</Country>  <Company>Columbia</Company> <Price>10.90</Price>    <Year>1985</Year></Cd><Cd>  <Title>Hide your heart</Title>  <Artist>Bonnie Tyler</Artist>   <Country>UK</Country>   <Company>CBS Records</Company>  <Price>9.90</Price> <Year>1988</Year></Cd><Cd>  <Title>Greatest Hits</Title>    <Artist>Dolly Parton</Artist>   <Country>USA</Country>  <Company>RCA</Company>  <Price>9.90</Price> <Year>1982</Year></Cd><Cd>  <Title>Still got the blues</Title>  <Artist>Gary Moore</Artist> <Country>UK</Country>   <Company>Virgin records</Company>   <Price>10.20</Price>    <Year>1990</Year></Cd><Cd>  <Title>Eros</Title> <Artist>Eros Ramazzotti</Artist>    <Country>EU</Country>   <Company>BMG</Company>  <Price>9.90</Price> <Year>1997</Year></Cd><Cd>  <Title>One night only</Title>   <Artist>Bee Gees</Artist>   <Country>UK</Country>   <Company>Polydor</Company>  <Price>10.90</Price>    <Year>1998</Year></Cd><Cd>  <Title>Sylvias Mother</Title>   <Artist>Dr.Hook</Artist>    <Country>UK</Country>   <Company>CBS</Company>  <Price>8.10</Price> <Year>1973</Year></Cd><Cd>  <Title>Maggie May</Title>   <Artist>Rod Stewart</Artist>    <Country>UK</Country>   <Company>Pickwick</Company> <Price>8.50</Price> <Year>1990</Year></Cd><Cd>  <Title>Romanza</Title>  <Artist>Andrea Bocelli</Artist> <Country>EU</Country>   <Company>Polydor</Company>  <Price>10.80</Price>    <Year>1996</Year></Cd><Cd>  <Title>When a man loves a woman</Title> <Artist>Percy Sledge</Artist>   <Country>USA</Country>  <Company>Atlantic</Company> <Price>8.70</Price> <Year>1987</Year></Cd><Cd>  <Title>Black angel</Title>  <Artist>Savage Rose</Artist>    <Country>EU</Country>   <Company>Mega</Company> <Price>10.90</Price>    <Year>1995</Year></Cd><Cd>  <Title>1999 Grammy Nominees</Title> <Artist>Many</Artist>   <Country>USA</Country>  <Company>Grammy</Company>   <Price>10.20</Price>    <Year>1999</Year></Cd><Cd>  <Title>For the good times</Title>   <Artist>Kenny Rogers</Artist>   <Country>UK</Country>   <Company>Mucik Master</Company> <Price>8.70</Price> <Year>1995</Year></Cd><Cd>  <Title>Big Willie style</Title> <Artist>Will Smith</Artist> <Country>USA</Country>  <Company>Columbia</Company> <Price>9.90</Price> <Year>1997</Year></Cd><Cd>  <Title>Tupelo Honey</Title> <Artist>Van Morrison</Artist>   <Country>UK</Country>   <Company>Polydor</Company>  <Price>8.20</Price> <Year>1971</Year></Cd><Cd>  <Title>Soulsville</Title>   <Artist>Jorn Hoel</Artist>  <Country>Norway</Country>   <Company>WEA</Company>  <Price>7.90</Price> <Year>1996</Year></Cd><Cd>  <Title>The very best of</Title> <Artist>Cat Stevens</Artist>    <Country>UK</Country>   <Company>Island</Company>   <Price>8.90</Price> <Year>1990</Year></Cd><Cd>  <Title>Stop</Title> <Artist>Sam Brown</Artist>  <Country>UK</Country>   <Company>A and M</Company>  <Price>8.90</Price> <Year>1988</Year></Cd><Cd>  <Title>Bridge of Spies</Title>  <Artist>T'Pau</Artist>  <Country>UK</Country>   <Company>Siren</Company>    <Price>7.90</Price> <Year>1987</Year></Cd><Cd>  <Title>Private Dancer</Title>   <Artist>Tina Turner</Artist>    <Country>UK</Country>   <Company>Capitol</Company>  <Price>8.90</Price> <Year>1983</Year></Cd><Cd>  <Title>Midt om natten</Title>   <Artist>Kim Larsen</Artist> <Country>EU</Country>   <Company>Medley</Company>   <Price>7.80</Price> <Year>1983</Year></Cd><Cd>  <Title>Pavarotti Gala Concert</Title>   <Artist>Luciano Pavarotti</Artist>  <Country>UK</Country>   <Company>DECCA</Company>    <Price>9.90</Price> <Year>1991</Year></Cd><Cd>  <Title>The dock of the bay</Title>  <Artist>Otis Redding</Artist>   <Country>USA</Country>  <Company>Atlantic</Company> <Price>7.90</Price> <Year>1987</Year></Cd><Cd>  <Title>Picture book</Title> <Artist>Simply Red</Artist> <Country>EU</Country>   <Company>Elektra</Company>  <Price>7.20</Price> <Year>1985</Year></Cd><Cd>  <Title>Red</Title>  <Artist>The Communards</Artist> <Country>UK</Country>   <Company>London</Company>   <Price>7.80</Price> <Year>1987</Year></Cd><Cd>  <Title>Unchain my heart</Title> <Artist>Joe Cocker</Artist> <Country>USA</Country>  <Company>EMI</Company>  <Price>8.20</Price> <Year>1987</Year></Cd></Catalog>";
                XmlFieldNode<?> node = xmlToXmlFieldNode(xml);
                testListener.testRunStarted(description);
                try {
                    for (int i = 0; i < XPATH_REQUEST_COUNT; i++) {
                        XPath xPath = JaxpUtils.getXPath(null);
                        String result = xPath.evaluate("/Catalog/Cd[Artist=\"Percy Sledge\"]/Title/text()",
                                node.getNode());
                        if (!"When a man loves a woman".equals(result)) {
                            testListener.testFailure(new Failure(description, null));
                        }
                    }
                } catch (Exception e) {
                    testListener.testFailure(new Failure(description, e));
                }
                testListener.testRunFinished(testResult);
                return testResult;
            }
        };
        List<Callable<Result>> tasks = new ArrayList<Callable<Result>>();
        for (int i = 0; i < TASK_COUNT; i++) {
            tasks.add(task);
        }

        // run tasks
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<Future<Result>> results = executor.invokeAll(tasks);

        // test results
        long totalTime = 0;
        for (Future<Result> result : results) {
            Result returnedResult = result.get();
            totalTime += returnedResult.getRunTime();
            assertTrue(returnedResult.wasSuccessful());
        }
    }
}
