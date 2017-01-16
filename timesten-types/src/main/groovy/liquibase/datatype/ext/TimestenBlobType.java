/**
 * 
 */
package liquibase.datatype.ext;

import liquibase.database.Database;
import liquibase.database.ext.TimestenDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.datatype.core.BlobType;
import liquibase.util.StringUtils;

/**
 * @author scott
 *
 */
@DataTypeInfo(name = "blob", aliases = {"java.sql.Types.BLOB","java.sql.Types.VARBINARY", "java.sql.Types.BINARY", "varbinary", "binary"}, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DATABASE)
public class TimestenBlobType extends BlobType {

	/* (non-Javadoc)
	 * @see liquibase.datatype.core.BlobType#toDatabaseDataType(liquibase.database.Database)
	 */
	@Override
	public DatabaseDataType toDatabaseDataType(Database database) {
		 if (database instanceof TimestenDatabase) {
			   String originalDefinition = StringUtils.trimToEmpty(getRawDefinition());
	            if (originalDefinition.toLowerCase().startsWith("blob") || originalDefinition.equals("java.sql.Types.BLOB")) {
	                return new DatabaseDataType("BLOB");
	            } else if (originalDefinition.toLowerCase().startsWith("varbinary") || originalDefinition.equals("java.sql.Types.VARBINARY")) {
	                return new DatabaseDataType("VARBINARY", getParameters());
	            } else if (originalDefinition.toLowerCase().startsWith("binary")) {
	                return new DatabaseDataType("BINARY", getParameters());
	            } 
		}
		return super.toDatabaseDataType(database);
	}

}
