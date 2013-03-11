package sia.ch14;

import java.util.HashMap;
import java.util.Map;

import org.apache.solr.common.SolrException;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.TextField;

import sia.ch14.MultiTextFieldSettings.AnalyzerModes;


/**
 * 
 * This field exist purely to expose the schema with defined
 * field types from the schema to the MultiTextAnalyzer.
 *
 */
public class MultiTextField extends TextField  {

	private final static String FIELD_MAPPINGS = "fieldMappings";
	private final static String DEFAULT_FIELDTYPE = "defaultFieldType";
	private final static String IGNORE_INVALID_MAPPINGS = "ignoreMissingMappings";
	private final static String KEY_FROM_TEXT_DELIMITER = "keyFromTextDelimiter";
	private final static String MULTI_KEY_DELIMITER = "multiKeyDelimiter";
	
	@Override
	protected void init(IndexSchema schema, Map<String,String> args) {
		super.init(schema, args);
		
		MultiTextFieldSettings indexSettings = new MultiTextFieldSettings();
		indexSettings.analyzerMode = AnalyzerModes.index;
		MultiTextFieldSettings querySettings = new MultiTextFieldSettings();
		querySettings.analyzerMode = AnalyzerModes.query;
		MultiTextFieldSettings multiTermSettings = new MultiTextFieldSettings();
		multiTermSettings.analyzerMode = AnalyzerModes.multiTerm;
		
		if (args.containsKey(KEY_FROM_TEXT_DELIMITER)){
			String keyFromTextDelimiter = args.get(KEY_FROM_TEXT_DELIMITER);
			if (keyFromTextDelimiter.length() == 1){			
				indexSettings.keyFromTextDelimiter = keyFromTextDelimiter.charAt(0);
				querySettings.keyFromTextDelimiter = indexSettings.keyFromTextDelimiter;
				indexSettings.keyFromTextDelimiter = indexSettings.keyFromTextDelimiter;
			}
			else{
				if (keyFromTextDelimiter.length() > 1){
					throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Schema configuration"
							+ " error for " + this.getClass().getSimpleName() + "."
							+ "Attribute 'keyFromTextDelimiter' must be a single character.");
				}
			}
			args.remove(KEY_FROM_TEXT_DELIMITER);
		}
		
		if (args.containsKey(MULTI_KEY_DELIMITER)){
			String multiKeyDelimiter = args.get(MULTI_KEY_DELIMITER);
			if (multiKeyDelimiter.length() == 1){			
				indexSettings.multiKeyDelimiter = multiKeyDelimiter.charAt(0);
				querySettings.multiKeyDelimiter = indexSettings.multiKeyDelimiter;
				indexSettings.multiKeyDelimiter = indexSettings.multiKeyDelimiter;
			}
			else{
				if (multiKeyDelimiter.length() > 1){
					throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Schema configuration"
							+ " error for " + this.getClass().getSimpleName() + "."
							+ "Attribute 'multiKeyDelimiter' must be a single character.");
				}
			}
			args.remove(MULTI_KEY_DELIMITER);
		}
		

		
		if (args.containsKey(DEFAULT_FIELDTYPE)){
			if (schema.getFieldTypes().containsKey(args.get(DEFAULT_FIELDTYPE))){
				indexSettings.defaultFieldTypeName = args.get(DEFAULT_FIELDTYPE);
				querySettings.defaultFieldTypeName = indexSettings.defaultFieldTypeName;
				multiTermSettings.defaultFieldTypeName = indexSettings.defaultFieldTypeName;
			}
			else {
				throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Invalid defaultFieldType defined in " 
							+ this.getClass().getSimpleName() + ". FieldType '" + args.get(DEFAULT_FIELDTYPE)
							+ " does not exist in the schema.");
				
			}
			args.remove(DEFAULT_FIELDTYPE);
		}

		
		if (args.containsKey(FIELD_MAPPINGS)){
			HashMap<String, String> possibleFieldMappings = new HashMap<String, String>();
			if (args.get(FIELD_MAPPINGS).length() > 0){
				String[] mappingPairs = args.get(FIELD_MAPPINGS).split(",");
				for (int i = 0; i< mappingPairs.length; i++){
					if (mappingPairs[i].trim().length() > 0 ){
						String[] mapping = mappingPairs[i].split(":");
						for (int j = 0; j < mapping.length; j++){
							String key = "";
							String fieldType = "";
							if (mapping.length == 2){
								key = mapping[0].trim();
								fieldType = mapping[1].trim();
							}
							else if (mapping.length == 1){
								fieldType = mapping[1].trim();
								key = fieldType;								
							}
							if (mapping.length == 0 || mapping.length > 2){
								throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
									"Schema configuration error for " + this.getClass().getSimpleName()
									+ ". Field Mapping '" + mappingPairs [i] + "' is syntactically incorrect.");
							}
							else{
								if (!schema.getFieldTypes().containsKey(fieldType)){
									throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
										"Schema configuration error for " + this.getClass().getSimpleName()
										+ ".  FieldType '" + fieldType + "' is not defined.");
								}
							}
							possibleFieldMappings.put(key, fieldType);
						}
					}
						
				}
				
				indexSettings.fieldMappings = possibleFieldMappings;
				querySettings.fieldMappings = indexSettings.fieldMappings;
				multiTermSettings.fieldMappings = indexSettings.fieldMappings;
			}
			args.remove(FIELD_MAPPINGS);
		}
		
		if (args.containsKey(IGNORE_INVALID_MAPPINGS)){
			boolean ignoreMissingMappings = Boolean.parseBoolean(args.get(IGNORE_INVALID_MAPPINGS));
			indexSettings.ignoreMissingMappings = ignoreMissingMappings;
			querySettings.ignoreMissingMappings = indexSettings.ignoreMissingMappings;
			multiTermSettings.ignoreMissingMappings = indexSettings.ignoreMissingMappings;	
			
			args.remove(IGNORE_INVALID_MAPPINGS);
		}

		MultiTextFieldAnalyzer indexAnalyzer = new MultiTextFieldAnalyzer(schema, indexSettings);
		MultiTextFieldAnalyzer queryAnalyzer = new MultiTextFieldAnalyzer(schema, querySettings);
		MultiTextFieldAnalyzer multiTermAnalyzer = new MultiTextFieldAnalyzer(schema, multiTermSettings);
				
		
		this.setAnalyzer(indexAnalyzer);
		this.setQueryAnalyzer(queryAnalyzer);
		this.setMultiTermAnalyzer(multiTermAnalyzer);
	}
	
	
}