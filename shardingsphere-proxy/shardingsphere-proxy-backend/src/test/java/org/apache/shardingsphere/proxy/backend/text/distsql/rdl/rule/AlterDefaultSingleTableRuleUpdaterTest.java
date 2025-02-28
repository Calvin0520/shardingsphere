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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.rule;

import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterDefaultSingleTableRuleStatement;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereDatabase;
import org.apache.shardingsphere.singletable.config.SingleTableRuleConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AlterDefaultSingleTableRuleUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private SingleTableRuleConfiguration currentConfig;
    
    private final AlterDefaultSingleTableRuleStatementUpdater updater = new AlterDefaultSingleTableRuleStatementUpdater();
    
    @Before
    public void setUp() throws Exception {
        when(database.getName()).thenReturn("sharding_db");
        when(database.getResource().getDataSources()).thenReturn(Collections.singletonMap("ds_0", mock(DataSource.class)));
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckWithNotExistConfiguration() throws Exception {
        AlterDefaultSingleTableRuleStatement statement = new AlterDefaultSingleTableRuleStatement("ds_1");
        updater.checkSQLStatement(database, statement, null);
    }
    
    @Test(expected = RequiredResourceMissedException.class)
    public void assertCheckWithInvalidResource() throws Exception {
        AlterDefaultSingleTableRuleStatement statement = new AlterDefaultSingleTableRuleStatement("ds_1");
        updater.checkSQLStatement(database, statement, currentConfig);
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckWithNotExistResource() throws Exception {
        when(currentConfig.getDefaultDataSource()).thenReturn(Optional.empty());
        AlterDefaultSingleTableRuleStatement statement = new AlterDefaultSingleTableRuleStatement("ds_0");
        updater.checkSQLStatement(database, statement, currentConfig);
    }
    
    @Test
    public void assertBuild() {
        AlterDefaultSingleTableRuleStatement statement = new AlterDefaultSingleTableRuleStatement("ds_0");
        SingleTableRuleConfiguration config = updater.buildToBeAlteredRuleConfiguration(statement);
        assertTrue(config.getDefaultDataSource().isPresent());
        assertThat(config.getDefaultDataSource().get(), is("ds_0"));
    }
    
    @Test
    public void assertUpdate() {
        AlterDefaultSingleTableRuleStatement statement = new AlterDefaultSingleTableRuleStatement("ds_0");
        SingleTableRuleConfiguration config = updater.buildToBeAlteredRuleConfiguration(statement);
        SingleTableRuleConfiguration currentConfig = new SingleTableRuleConfiguration();
        updater.updateCurrentRuleConfiguration(currentConfig, config);
        assertTrue(currentConfig.getDefaultDataSource().isPresent());
        assertThat(currentConfig.getDefaultDataSource().get(), is("ds_0"));
    }
}
