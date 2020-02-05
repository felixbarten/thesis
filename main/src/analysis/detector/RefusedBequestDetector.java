package analysis.detector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import analysis.storage.PrimitiveFloatMap;
import analysis.storage.PrimitiveIntMap;
import analysis.storage.SetStrMap;
import model.Class;
import util.Settings;
import util.DebuggingLogger;

/**
 * Detector for Refused Bequest
 * Using the metrics described in Lanza & Marinescu's Object Oriented Programming Metrics in Practice. 
 * Created by felixb on 9-4-2018
 */
public class RefusedBequestDetector extends Detector {

	private final static String OVERRIDES = "OVERRIDES";
	// aren't CLOC and class_loc the same? 
	private final static String CLOC = "CLOC";
	// Cyclomatic complexity stored per class.
	private final static String CLASS_WMC = "CLASS_WMC";
	private final static String CLASS_AMW = "CLASS_AMW";
	// loc per class
	private final static String CLASS_LOC = "CLASS_LOC";
	
	// total loc stored per project
	private final static String PROJECT_LOC = "PROJECT_LOC";
	// total cyclomatic complexity for project.
	private final static String PROJECT_CC = "PROJECT_CC";
	
	
	// average class LOC stored per project
	// TODO are these two redundant if you have number of classes in project and total LOC you can derive these values?
	private final static String CLASS_AVG_LOC = "CLASS_AVG_LOC";
	// average class cyclomatic complexity (stored per project?)
	private final static String CLASS_AVG_CC  = "CLASS_AVG_CC";
	private final static String PROJECT_AVG_SUBROUTINE_CC = "PROJECT_AVG_SUBROUTINE_CC"; // average cyclomatic per subroutine in project
	private final static String PROJECT_AVG_AMW = "PROJECT_AVG_AMW";
	private final static String PROJECT_AVG_LOC = "PROJECT_AVG_LOC";
	private final static String SUBROUTINE_AVG_CC = "SUBROUTINE_AVG_CC";
	// stores the NOM (number of methods) per project. 
	private final static String GLOBAL_AVG_NOM = "GLOBAL_AVG_NOM";
	private final static String PARENT_PROTECTED_MEMBERS = "PARENT_PROTECTED_MEMBERS";
	private static final String CLASS_PARENTS = "CLASS_PARENTS";
	private static final String CLASS_REF_VAR_COUNT = "CLASS_REF_VAR_COUNT";
	private static final String CLASS_REF_CLS_COUNT = "CLASS_REF_CLS_COUNT";
	
	private static final String CLASS_REF_METHODS = "CLASS_REF_METHODS";	
	
	private static final String CLASS_DEF_METHODS = "CLASS_DEF_METHODS";		
	private static final String CLASS_METHODS = "CLASS_METHODS";	
	private static final String CLASS_PROTECTED_FIELDS = "CLASS_PROTECTED_FIELDS";	
	private static final String CLASS_FIELDNAMES = "CLASS_FIELDNAMES";
	private static final String CLASS_REF_METHOD_NAMES = "CLASS_REF_METHOD_NAMES";
	private static final String CLASS_REF_CLS_NAMES = "CLASS_REF_CLS_NAMES";
	private static final String CLASS_REF_VAR_NAMES = "CLASS_REF_VAR_NAMES";

	private DebuggingLogger debug;
	private DataStore global;
	
	private int memberThreshold = 0;
	private int overrideThreshold = 0;
	private boolean verbose = false;

	public RefusedBequestDetector() throws IOException {
		super();
		this.debug = DebuggingLogger.getInstance();
		global = DataStore.getInstance();
		// get from settings
		try {
			Properties config = Settings.getConfig();
			this.overrideThreshold = Integer.parseInt(config.getProperty("detectors.rb.overridethreshold"));
			this.memberThreshold = Integer.parseInt(config.getProperty("detectors.rb.memberthreshold"));
			if (config.containsKey("detectors.rb.verbose")
					&& config.getProperty("detectors.rb.verbose").equalsIgnoreCase("true")) {
				this.verbose = true;
			}
		} catch(NumberFormatException e) {
			setDefaultThresholds();
		}
		if(memberThreshold == 0 || overrideThreshold == 0) setDefaultThresholds();
		System.out.println("[RB] Initialized Detector with thresholds: " + this.memberThreshold + " and " + this.overrideThreshold);
	}

	@Override
	public void addDataStores() throws IOException {
		//prevents initialization issues
		global = DataStore.getInstance();
		global.addDataStore(PROJECT_AVG_AMW, new PrimitiveFloatMap(this.getDataStoreFilePath(PROJECT_AVG_AMW)));
		global.addDataStore(PROJECT_AVG_LOC, new PrimitiveIntMap(this.getDataStoreFilePath(PROJECT_AVG_LOC)));		
		global.addDataStore(CLASS_AVG_LOC, new PrimitiveIntMap(this.getDataStoreFilePath(CLASS_AVG_LOC)));		
		global.addDataStore(CLASS_DEF_METHODS, new SetStrMap(this.getDataStoreFilePath(CLASS_DEF_METHODS)));
		global.addDataStore(CLASS_REF_METHODS, new SetStrMap(this.getDataStoreFilePath(CLASS_REF_METHODS)));
		global.addDataStore(CLASS_REF_CLS_COUNT, new PrimitiveIntMap(this.getDataStoreFilePath(CLASS_REF_CLS_COUNT)));
		global.addDataStore(CLASS_REF_VAR_COUNT, new PrimitiveIntMap(this.getDataStoreFilePath(CLASS_REF_VAR_COUNT)));
		global.addDataStore(CLASS_PARENTS, new SetStrMap(this.getDataStoreFilePath(CLASS_PARENTS)));
		global.addDataStore(CLASS_PROTECTED_FIELDS, new PrimitiveIntMap(this.getDataStoreFilePath(CLASS_PROTECTED_FIELDS)));
		global.addDataStore(CLASS_AVG_CC, new PrimitiveFloatMap(this.getDataStoreFilePath(CLASS_AVG_CC)));
		global.addDataStore(CLASS_AMW, new PrimitiveFloatMap(this.getDataStoreFilePath(CLASS_AMW)));
		global.addDataStore(CLASS_WMC, new PrimitiveIntMap(this.getDataStoreFilePath(CLASS_WMC)));
		global.addDataStore(CLASS_LOC, new PrimitiveIntMap(this.getDataStoreFilePath(CLASS_LOC)));
		global.addDataStore(CLASS_METHODS, new PrimitiveIntMap(this.getDataStoreFilePath((CLASS_METHODS))));
		
		global.addDataStore(CLASS_FIELDNAMES, new SetStrMap(this.getDataStoreFilePath((CLASS_FIELDNAMES))));
		global.addDataStore(CLASS_REF_VAR_NAMES, new SetStrMap(this.getDataStoreFilePath((CLASS_REF_VAR_NAMES))));
		global.addDataStore(CLASS_REF_METHOD_NAMES, new SetStrMap(this.getDataStoreFilePath((CLASS_REF_METHOD_NAMES))));
		global.addDataStore(CLASS_REF_CLS_NAMES, new SetStrMap(this.getDataStoreFilePath((CLASS_REF_CLS_NAMES))));
	}

	/**
	  	To satisfy precondition of parent class must be direct ancestor and a library/dependency. 
	  	Libraries don't get added to the superclass set so precondition is satisfied as the classes can't be iterated if they're not in the Set. 		
		
	 */
	@Override
	protected Boolean isPreliminarilyDefective(Class cls) {
		boolean hasParent = hasParent(cls);
		if(hasParent && verbose) {
			debug.debug("[RB] Class: " +  cls.getShortName() + " is preliminary defective: " + hasParent);
		}
		return hasParent; 
	}

	@Override
	protected Boolean confirmDefect(String fullPath, String projectPath) {		
		// debugging 
		boolean bequest = cls_ignores_bequest(fullPath);
		boolean complex = clsComplex(fullPath, projectPath);

		if (bequest && complex && verbose) {
			debug.debug("[RB] Class with path: " + fullPath + " has Refused (Parent) Bequest.");
		}
		
		return complex && bequest;
	}

	@Override
	protected String getName() {
		return "Refused (Parent) Bequest";
	}
	
	private boolean cls_ignores_bequest(String path) {
		boolean prot = fewProtectedMembers(path);
		boolean refused = refusedBequest(path);
		boolean overrides = overridesMethods(path);
		// ^ debugging
		return (prot && refused) || overrides;
	}
	
	
	/**
	 * NProtM > FEW
	 * @param cls
	 * @return
	 */
	private boolean fewProtectedMembers(String path) {
		/* 
		 * this one is weird in Python as a class can have more than one parent. 
		 * So the threshold may need to be higher than in Java. 
		 */
		//Integer memberCount = cls.getProtectedParentVars().getAsSet().size();
		Integer parentMember = 0;
		Set<String> classParents = global.getStrSetMap(CLASS_PARENTS).get(path);
		
		if(classParents != null) {
			for (String parent : classParents) {
				Integer temp = (Integer) global.getPrimitiveIntMapStore(CLASS_PROTECTED_FIELDS).get(parent);	
				if (temp != null) {
					parentMember += temp;	
				}
			}
		}
		
		boolean condition = parentMember > memberThreshold;
		
		return condition;
	}

	
	/**
	 * BUR < A THIRD
	 * @param cls
	 * @return
	 */
	private boolean refusedBequest(String path) {
		// TODO Comment needs to be verified. 
		
		// roadblock. Needs additional information from the AST about method calls. which is not currently available. 
		// collect call data 
		// check if parent data is called. 
		int parentMemberCount = 0;
		
		int count = 0; 
		int methodsCount = 0;
		float ratio = 0.33f;
		
		//Integer classRefs = (Integer) global.getPrimitiveIntMapStore(CLASS_REF_CLS_COUNT).get(path);
		//Integer varRefs = (Integer) global.getPrimitiveIntMapStore(CLASS_REF_VAR_COUNT).get(path);
		
		Set<String> parents = global.getStrSetMap(CLASS_PARENTS).get(path);
		List<String> parentVars = new ArrayList<>();
		Set<String> parentMethods = new HashSet<>();
		if (parents != null) {
			for (String parent : parents) {
				Set<String> fieldnames =  global.getStrSetMap(CLASS_FIELDNAMES).get(parent);
				if (fieldnames != null) {
					parentVars.addAll(fieldnames);
				}
				Set<String> definedMethods = global.getStrSetMap(CLASS_DEF_METHODS).get(parent);
				if (definedMethods != null) {
					parentMethods.addAll(definedMethods);
				}
			}
		}
		
		Set<String> calledMethods = global.getStrSetMap(CLASS_REF_METHOD_NAMES).get(path);
		Set<String> referencedVars = global.getStrSetMap(CLASS_REF_VAR_NAMES).get(path);
		
		// methodsCount is never incremented. In the rascal version it was possible to track method invocations of the parent.
		if (calledMethods != null) {
			for(String method : calledMethods) {
				if (parentMethods.contains(method)) {
					methodsCount++;
				}
			}
		}
		
		if(referencedVars != null) {
			for(String var : referencedVars) {
				if (parentVars.contains(var)) {
					count++;
				}
			}
		}
		parentMemberCount = parentVars.size() + parentMethods.size();
						
		return ((count + methodsCount) / checkIfZero(parentMemberCount)) < ratio;
	}
	
	private int checkIfZero(int n) {
		return n == 0 ? 1 : n;
	}

	
	/**
	 * "Overriding methods are rare in child" 
	 * This implementation checks all parent classes for methods, adds them to a set of method names and then compares the methods in the parent with the methods in the child.
	 * If the sets intersect there are overrides present in the child class. 
	 * BOvR < A THIRD
	 * 
	 * In Python an override is assumed if the deriving class has a method of the same name. 
	 * Therefore if we compare the set of subroutines in the parent with the set of subroutines in the child an intersection would show the overrides. 
	 * @return boolean whether it has more or fewer overriding methods than the threshold. 
	 */
	private boolean overridesMethods(String path) {	
		/*
			Step 1: get parents 
			Step 2: foreach parent lookup set of subroutine names 
			Step 3: add all to a set
			Step 4: compare set to current class set of subroutines. 
		*/
		
		Set<String> parentSubRoutines = new HashSet<>();
		Set<String> superClasses = global.getStrSetMap(CLASS_PARENTS).get(path);
		if(superClasses != null) {
			for(String parent : superClasses) {
				Set<String> subroutines = global.getStrSetMap(CLASS_DEF_METHODS).get(parent);
				if (subroutines != null) 
					parentSubRoutines.addAll(subroutines);
			}
		}
		Set<String> defMethods = global.getStrSetMap(CLASS_DEF_METHODS).get(path);
		Set<String> intersection = new HashSet<String>();	
		// duplicate set
		if(defMethods != null) {
			intersection = new HashSet<>(defMethods);
		}
		intersection.retainAll(parentSubRoutines);
		// Debugging marker.
		boolean condition = intersection.size() > overrideThreshold;
		
		return condition;
	}

	/**
	 * Calculates if the class is not small and not simple.
	 * (AMW > avg) || (WMC > avg) && (NOM > avg)
	 * @param cls
	 * @return
	 */
	private boolean clsComplex(String path, String projectPath) {
		boolean funcComplexityAbvAvg = funcComplexityAboveAvg(path, projectPath);
		boolean clsCCAboveAvg = clsCCAboveAvg(path, projectPath);
		boolean clsSizeAbvAvg = clsSizeAboveAvg(path, projectPath);
		// debugging 
		return (funcComplexityAbvAvg || clsCCAboveAvg ) && clsSizeAbvAvg;
	}
	
	/**
	 * Number of Methods > more than average (of the classes in the project).
	 * @param cls
	 * @return
	 */
	private boolean clsSizeAboveAvg(String path, String projectPath) {
		Integer LOC = (Integer) global.getPrimitiveIntMapStore(CLASS_LOC).get(path);
		Integer clsLOC = (Integer) global.getPrimitiveIntMapStore(CLASS_AVG_LOC).get(projectPath);
		boolean sizeAboveAvg = false;
		if(LOC != null && clsLOC != null) {
			sizeAboveAvg =  LOC >= clsLOC;
		}
		return sizeAboveAvg;
	}

	/**
	 * Calculates the Average Method Weight
	 * AMW > avg
	 * WMC / NOM = AMW 
	 * @param cls
	 * @return
	 */
	private boolean funcComplexityAboveAvg(String path, String projectPath) {
		// get functional CC for project				
		Float projectAvg = (Float) global.getPrimitiveFloatMapStore(PROJECT_AVG_AMW).get(projectPath);
		Float AMW = 0.0f;
		Integer NOM = (Integer) global.getPrimitiveIntMapStore(CLASS_METHODS).get(path);
		Integer WMC = (Integer) global.getPrimitiveIntMapStore(CLASS_WMC).get(path);
		// Check if class contains methods 

		boolean condition = false;
		// Really java you're making me check EVERY null?
		if(WMC != null && NOM != null && projectAvg != null) {
			AMW = (float)WMC / checkIfZero(NOM);
			condition = AMW > projectAvg;
		}
		return condition;
	}


	/**
	 * Calculates Weighted Method Count.
	 * WMC = CYCLOMATIC COMPLEXITY OF CLASS (including all methods) 
	 * WMC > avg
	 * @param cls
	 * @return true if class is more complex than average
	 */
	private boolean clsCCAboveAvg(String path, String projectPath) {
		// get functional CC for project
		//Integer avgWMC = this.getPrimitiveMapStore(CLASS_AVG_CC).get(cls.getProject().getPath());
		Float avgWMC = (Float) global.getPrimitiveFloatMapStore(CLASS_AVG_CC).get(projectPath);
		Integer WMC = (Integer) global.getPrimitiveIntMapStore(CLASS_WMC).get(path);
		//debug.debug(cls.getShortName() + " has CC above avg: " + cls.getWMC() + " > " + avgWMC);
		
		boolean condition = false;
		if (WMC != null && avgWMC != null) {
			condition = WMC > avgWMC;
		}
		return condition;
	}
	
	/**
	 * Precondition for RBP processing. Class must have a parent.
	 * @return
	 */
	private boolean hasParent(Class cls) {
		Map<String, Class> supers = cls.getSuperclasses();
		return supers.size() > 0;
	}
	
	private void setDefaultThresholds() {
		// set to default values if they can't be fetched from settings 
		this.overrideThreshold = 3;
		this.memberThreshold = 3; 
	}
	
}
