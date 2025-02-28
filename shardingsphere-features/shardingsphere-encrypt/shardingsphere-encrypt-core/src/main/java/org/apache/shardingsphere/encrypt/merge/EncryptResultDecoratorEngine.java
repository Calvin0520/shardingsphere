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

package org.apache.shardingsphere.encrypt.merge;

import org.apache.shardingsphere.encrypt.constant.EncryptOrder;
import org.apache.shardingsphere.encrypt.merge.dal.EncryptDALResultDecorator;
import org.apache.shardingsphere.encrypt.merge.dql.EncryptAlgorithmMetaData;
import org.apache.shardingsphere.encrypt.merge.dql.EncryptDQLResultDecorator;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.merge.engine.decorator.ResultDecorator;
import org.apache.shardingsphere.infra.merge.engine.decorator.ResultDecoratorEngine;
import org.apache.shardingsphere.infra.merge.engine.decorator.impl.TransparentResultDecorator;
import org.apache.shardingsphere.infra.metadata.ShardingSphereDatabase;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;

/**
 * Result decorator engine for encrypt.
 */
public final class EncryptResultDecoratorEngine implements ResultDecoratorEngine<EncryptRule> {
    
    @Override
    public ResultDecorator<?> newInstance(final DatabaseType databaseType, final String databaseName, final ShardingSphereDatabase database,
                                          final EncryptRule encryptRule, final ConfigurationProperties props, final SQLStatementContext<?> sqlStatementContext) {
        if (sqlStatementContext instanceof SelectStatementContext) {
            EncryptAlgorithmMetaData algorithmMetaData = new EncryptAlgorithmMetaData(databaseName, database, encryptRule, (SelectStatementContext) sqlStatementContext);
            return new EncryptDQLResultDecorator(algorithmMetaData);
        }
        if (sqlStatementContext.getSqlStatement() instanceof DALStatement) {
            return new EncryptDALResultDecorator();
        }
        return new TransparentResultDecorator();
    }
    
    @Override
    public int getOrder() {
        return EncryptOrder.ORDER;
    }
    
    @Override
    public Class<EncryptRule> getTypeClass() {
        return EncryptRule.class;
    }
}
