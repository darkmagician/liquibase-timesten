/**
 * 
 */
package liquibase.database.ext;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.snapshot.SnapshotIdService;
import liquibase.snapshot.jvm.JdbcSnapshotGenerator;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Sequence;

/**
 * @author scott
 *
 */
public class TimestenSequenceSnapshotGenerator extends JdbcSnapshotGenerator {

	protected String getSelectSequenceSql(Schema schema, Database database) {
		return "SELECT OBJECT_NAME AS SEQUENCE_NAME FROM SYS.USER_OBJECTS WHERE OBJECT_TYPE='SEQUENCE';";
	}

	public TimestenSequenceSnapshotGenerator() {
		super(Sequence.class, new Class[] { Schema.class });
	}

	@Override
	protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot)
			throws DatabaseException, InvalidExampleException {
		if (!(snapshot.getDatabase() instanceof TimestenDatabase) ||!snapshot.getDatabase().supportsSequences()) {
			return;
		}
		if (foundObject instanceof Schema) {
			Schema schema = (Schema) foundObject;
			Database database = snapshot.getDatabase();
			if (!database.supportsSequences()) {
				updateListeners("Sequences not supported for " + database.toString() + " ...");
			}

			// noinspection unchecked
			List<Map<String, ?>> sequences = ExecutorService.getInstance().getExecutor(database)
					.queryForList(new RawSqlStatement(getSelectSequenceSql(schema, database)));

			if (sequences != null) {
				for (Map<String, ?> sequence : sequences) {
					schema.addDatabaseObject(mapToSequence(sequence, (Schema) foundObject, database));
				}
			}
		}
	}

	@Override
	protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot)
			throws DatabaseException {
		if (example.getSnapshotId() != null) {
			return example;
		}
		if (example.getAttribute("liquibase-complete", false)) { // need to go
																	// through
																	// "snapshotting"
																	// the
																	// object
																	// even if
																	// it was
																	// previously
																	// populated
																	// in addTo.
																	// Use the
																	// "liquibase-complete"
																	// attribute
																	// to track
																	// that it
																	// doesn't
																	// need to
																	// be fully
																	// snapshotted
			example.setSnapshotId(SnapshotIdService.getInstance().generateId());
			example.setAttribute("liquibase-complete", null);
			return example;
		}

		Database database = snapshot.getDatabase();
		if (!(database instanceof TimestenDatabase) || !database.supportsSequences()) {
			return null;
		}

		List<Map<String, ?>> sequences = ExecutorService.getInstance().getExecutor(database)
				.queryForList(new RawSqlStatement(getSelectSequenceSql(example.getSchema(), database)));
		for (Map<String, ?> sequenceRow : sequences) {
			String name = cleanNameFromDatabase((String) sequenceRow.get("SEQUENCE_NAME"), database);
			if ((database.isCaseSensitive() && name.equals(example.getName())
					|| (!database.isCaseSensitive() && name.equalsIgnoreCase(example.getName())))) {
				return mapToSequence(sequenceRow, example.getSchema(), database);
			}
		}

		return null;
	}

	private Sequence mapToSequence(Map<String, ?> sequenceRow, Schema schema, Database database) {
		String name = cleanNameFromDatabase((String) sequenceRow.get("SEQUENCE_NAME"), database);
		Sequence seq = new Sequence();
		seq.setName(name);
		seq.setSchema(schema);
		seq.setStartValue(toBigInteger(sequenceRow.get("START_VALUE"), database));
		seq.setMinValue(toBigInteger(sequenceRow.get("MIN_VALUE"), database));
		seq.setMaxValue(toBigInteger(sequenceRow.get("MAX_VALUE"), database));
		seq.setCacheSize(toBigInteger(sequenceRow.get("CACHE_SIZE"), database));
		seq.setIncrementBy(toBigInteger(sequenceRow.get("INCREMENT_BY"), database));
		seq.setWillCycle(toBoolean(sequenceRow.get("WILL_CYCLE"), database));
		seq.setOrdered(toBoolean(sequenceRow.get("IS_ORDERED"), database));
		seq.setAttribute("liquibase-complete", true);

		return seq;
	}

	protected Boolean toBoolean(Object value, Database database) {
		if (value == null) {
			return null;
		}

		if (value instanceof Boolean) {
			return (Boolean) value;
		}

		String valueAsString = value.toString();
		valueAsString = valueAsString.replace("'", "");
		if (valueAsString.equalsIgnoreCase("true") || valueAsString.equalsIgnoreCase("'true'")
				|| valueAsString.equalsIgnoreCase("y") || valueAsString.equalsIgnoreCase("1")
				|| valueAsString.equalsIgnoreCase("t")) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}

	protected BigInteger toBigInteger(Object value, Database database) {
		if (value == null) {
			return null;
		}

		if (value instanceof BigInteger) {
			return (BigInteger) value;
		}

		return new BigInteger(value.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * liquibase.snapshot.jvm.JdbcSnapshotGenerator#getPriority(java.lang.Class,
	 * liquibase.database.Database)
	 */
	@Override
	public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
		if ( database instanceof TimestenDatabase ) {
			return super.getPriority(objectType, database);
		}
		return PRIORITY_NONE;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see liquibase.snapshot.jvm.JdbcSnapshotGenerator#replaces()
	 */
	@Override
	public Class<? extends SnapshotGenerator>[] replaces() {
		return new Class[] { liquibase.snapshot.jvm.SequenceSnapshotGenerator.class };
	}


}
