/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.isis.extensions.xml.objectstore;

import java.util.Collections;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.alternatives.objectstore.xml.XmlObjectStore;
import org.apache.isis.alternatives.objectstore.xml.internal.clock.DefaultClock;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.SerialNumberVersion;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.testspec.TestProxySpecification;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.DestroyObjectCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.ObjectStoreTransaction;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommand;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.PersistenceCommandContext;
import org.apache.isis.core.runtime.persistence.objectstore.transaction.SaveObjectCommand;
import org.apache.isis.core.runtime.persistence.oidgenerator.simple.SerialOid;
import org.apache.isis.core.runtime.testsystem.ProxyJunit4TestCase;
import org.apache.isis.core.runtime.testsystem.TestProxyAdapter;
import org.apache.isis.core.runtime.testsystem.TestProxyConfiguration;
import org.apache.isis.extensions.xml.objectstore.internal.data.MockDataManager;
import org.apache.isis.extensions.xml.objectstore.internal.services.DummyServiceManager;

import static org.junit.Assert.*;

public class XmlObjectStoreTest extends ProxyJunit4TestCase {

	private Mockery context = new JUnit4Mockery();
	private XmlObjectStore objectStore;
    private MockDataManager dataManager;
    private TestProxyAdapter adapter;
    private TestProxySpecification spec;
    private TestProxyConfiguration configuration;
    private ObjectStorePersistenceHelper persistenceHelper;
	private PersistenceCommandContext transaction;
	private ObjectAdapter mockAdapter;

    @Before
    public void setUp() throws Exception {

		mockAdapter = context.mock(ObjectAdapter.class);
        // system
        dataManager = new MockDataManager();
        objectStore = new XmlObjectStore(dataManager, new DummyServiceManager());
        objectStore.setClock(new DefaultClock());

        // objects
        configuration = new TestProxyConfiguration();
        spec = new TestProxySpecification(this.getClass());
        spec.fields = new ObjectAssociation[0];
        adapter = new TestProxyAdapter();
        adapter.setupSpecification(spec);
        adapter.setOptimisticLock(new SerialNumberVersion(23, null, null));
    	adapter.setupOid(SerialOid.createPersistent(1));
        persistenceHelper = new ObjectStorePersistenceHelper(spec);
        transaction = null;
    }

	@Test
	public void validatesSaveObjectCommandWithOptimisticLock() throws Exception {
		allowingGetOidAndGetObjectAndTitleStringFromAdapter();
		context.checking(new Expectations() {
			{
				one(mockAdapter).setOptimisticLock(with(any(Version.class)));
			}
		});
		objectStore.createSaveObjectCommand(mockAdapter);
	}

    @Test
    public void ValidatesCreateObjectCommand() throws Exception {
		CreateObjectCommand command = objectStore.createCreateObjectCommand(adapter);
		command.execute(transaction);
		assertFalse(objectStore.hasInstances(spec));
    }

    @Test
    public void validatesDestroyObjectCommand() throws Exception {
		DestroyObjectCommand command = objectStore.createDestroyObjectCommand(adapter);
		command.execute(transaction);
		assertFalse(objectStore.hasInstances(spec));
    }

    @Test
    public void validatesSaveObjectCommand() throws Exception {
		SaveObjectCommand command = objectStore.createSaveObjectCommand(adapter);
		command.execute(transaction);
		assertTrue(objectStore.hasInstances(spec));
    }
    
    @Test
    public void validatesGettingObjectStoreInstances() throws Exception {
        SaveObjectCommand command = objectStore.createSaveObjectCommand(adapter);
        objectStore.execute(Collections.<PersistenceCommand>singletonList(command));
    	ObjectAdapter[] array = objectStore.getInstances(persistenceHelper);
    	assertTrue(array.length == 1);
    }
    
	@Test 
	public void validatesObjectStoreName()throws Exception {
		assertTrue(objectStore.name().equals("XML"));
	}
	
	@Test 
	public void validatesObjectStoreHasInstances()throws Exception {
        SaveObjectCommand command = objectStore.createSaveObjectCommand(adapter);
        objectStore.execute(Collections.<PersistenceCommand>singletonList(command));
		assertTrue(objectStore.hasInstances(spec));
	}
	
	@Test 
	public void validatesObjectStoreIfFixtureIsInstalled()throws Exception {
        SaveObjectCommand command = objectStore.createSaveObjectCommand(adapter);
        objectStore.execute(Collections.<PersistenceCommand>singletonList(command));
        objectStore.open();        
		assertTrue(objectStore.isFixturesInstalled());
	}
	
	@Test 
	public void validatesObjectStoreGetObject()throws Exception {
        SaveObjectCommand command = objectStore.createSaveObjectCommand(adapter);
        objectStore.execute(Collections.<PersistenceCommand>singletonList(command));
		assertTrue(objectStore.getObject(adapter.getOid(), adapter.getSpecification()).getOid().equals(adapter.getOid()));
	}
	
	@Test
	public void validateObjectStoreCreationWithProxyConfiguration() throws Exception {
		XmlObjectStore objectStore = new XmlObjectStore(configuration);
		assertFalse(objectStore.isFixturesInstalled());
	}
	private void allowingGetOidAndGetObjectAndTitleStringFromAdapter() {
		context.checking(new Expectations() {
			{
				allowing(mockAdapter).getOid();
				allowing(mockAdapter).getObject();
				allowing(mockAdapter).titleString();
			}
		});
	}
	

}
