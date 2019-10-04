package ast.path;

import java.util.List;

import ast.Visitor;
import util.StringHelper;

/**
 * Created by Nik on 23-06-2015
 */
public class DottedPath extends Path {

	private final static String DELIMITER = ".";
	private List<String> path;

	public DottedPath( Integer locInfo,  List<String> path) {
		super(locInfo);
		this.path = path;
	}

	public void addPrefixes(List<String> prefixes) {
		prefixes.addAll(this.path);
		this.path = prefixes;
	}

	@Override
	public String getPath() {
		return StringHelper.implode(this.path, DELIMITER);
	}

	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visit(this);
	}
}
