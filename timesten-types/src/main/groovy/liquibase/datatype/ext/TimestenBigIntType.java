/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package liquibase.datatype.ext;

import liquibase.database.Database;
import liquibase.database.ext.TimestenDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.datatype.core.BigIntType;

/**
 * @author Michal Bocek
 * @since 1.0.0
 */
@DataTypeInfo(name="bigint", aliases = {"java.sql.Types.BIGINT", "java.math.BigInteger", "java.lang.Long", "integer8", "bigserial", "serial8", "int8"}, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DATABASE)
public class TimestenBigIntType extends BigIntType {
    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof TimestenDatabase){
            return new DatabaseDataType("TT_BIGINT", getParameters());
        }
        
        return super.toDatabaseDataType(database);
    }
    
	/* (non-Javadoc)
	 * @see liquibase.datatype.LiquibaseDataType#supports(liquibase.database.Database)
	 */
	@Override
	public boolean supports(Database database) {
		return database instanceof TimestenDatabase;
	}
}
