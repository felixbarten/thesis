package db;

import db.pojo.*;
import model.Class;
import model.Method;
import model.Module;
import model.Project;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Nik on 28-07-2015
 */
public class DataHandler {

	private final String projectName;
	private final RunInfoEntity runInfoEntity;

	public DataHandler(String projectName) {
		this.projectName = projectName;
		this.runInfoEntity = Converter.createRunInfo(projectName);
		insert(runInfoEntity);
	}

	public void save(Project project) {
		VersionEntity versionEntity = Converter.createVersion(runInfoEntity, project.getVersion());
		insert(versionEntity);

		Map<Module, ModuleEntity> moduleMap = Converter.convertModules(versionEntity, project.getModules());
		insert(moduleMap.values());

		Map<Class, ClassEntity> classMap = Converter.convertClasses(moduleMap, project.getClasses());
		insert(classMap.values());

		List<DependenciesEntity> dependencies = Converter.createDependencies(classMap);
		insert(dependencies);

		Map<Method, MethodEntity> methodMap = Converter.convertMethods(classMap, project.getMethods());
		insert(methodMap.values());
	}

	private void insert(Object o) {
		ConnectionManager cm = ConnectionManager.getInstance();
		Session session = cm.openSession();
		Transaction tx = session.beginTransaction();
		session.save(o);
		tx.commit();
		session.close();
	}

	private <T> void insert(Collection<T> objects) {
		insertObjects(objects.stream().map(m -> (Object) m).collect(Collectors.toList()));
	}

	private void insertObjects(List<Object> objects) {
		ConnectionManager cm = ConnectionManager.getInstance();
		StatelessSession session = cm.openStatelessSession();
		Transaction tx = session.beginTransaction();
		objects.forEach(o -> session.insert(o));
		tx.commit();
		session.close();
	}
}
