/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.UnknownDatabaseException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUseStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class UseDatabaseExecutorTest extends ProxyContextRestorer {
    
    private static final String DATABASE_PATTERN = "db_%s";
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        Field contextManagerField = ProxyContext.getInstance().getClass().getDeclaredField("contextManager");
        contextManagerField.setAccessible(true);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(
                mock(MetaDataPersistService.class), getDatabaseMap(), mock(ShardingSphereRuleMetaData.class), mock(OptimizerContext.class), new ConfigurationProperties(new Properties()));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        contextManagerField.set(ProxyContext.getInstance(), contextManager);
    }
    
    private Map<String, ShardingSphereDatabase> getDatabaseMap() {
        Map<String, ShardingSphereDatabase> result = new HashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
            when(database.getResource().getDatabaseType()).thenReturn(new H2DatabaseType());
            result.put(String.format(DATABASE_PATTERN, i), database);
        }
        return result;
    }
    
    @Test
    public void assertExecuteUseStatementBackendHandler() {
        MySQLUseStatement useStatement = mock(MySQLUseStatement.class);
        when(useStatement.getSchema()).thenReturn(String.format(DATABASE_PATTERN, 0));
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getAllDatabaseNames().contains(anyString())).thenReturn(true);
        when(contextManager.getMetaDataContexts().getGlobalRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData().getRules()).thenReturn(new LinkedList<>());
        when(contextManager.getMetaDataContexts().getDatabaseMetaData(any())).thenReturn(database);
        ProxyContext.init(contextManager);
        UseDatabaseExecutor useSchemaBackendHandler = new UseDatabaseExecutor(useStatement);
        useSchemaBackendHandler.execute(connectionSession);
        verify(connectionSession).setCurrentDatabase(anyString());
    }
    
    @Test(expected = UnknownDatabaseException.class)
    public void assertExecuteUseStatementBackendHandlerWhenSchemaNotExist() {
        MySQLUseStatement useStatement = mock(MySQLUseStatement.class);
        when(useStatement.getSchema()).thenReturn(String.format(DATABASE_PATTERN, 10));
        UseDatabaseExecutor useSchemaBackendHandler = new UseDatabaseExecutor(useStatement);
        useSchemaBackendHandler.execute(connectionSession);
    }
}
