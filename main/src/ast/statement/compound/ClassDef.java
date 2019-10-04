package ast.statement.compound;

import java.util.Collections;
import java.util.List;

import ast.Decorator;
import ast.Suite;
import ast.Visitor;
import ast.argument.Argument;
import ast.expression.nocond.atom.Identifier;
import ast.statement.Statement;

/**
 * Created by Nik on 19-05-2015
 */
public class ClassDef extends Statement {

	private final Identifier name;
	private final Suite body;
	private final List<Argument> inheritance;
	private List<Decorator> decorators;

	public ClassDef(Integer locInfo, Identifier name, Suite body, List<Argument> inheritance) {
		super(locInfo);
		this.name = name;
		this.body = body;
		this.inheritance = inheritance;
		this.decorators = Collections.emptyList();
	}

	public Identifier getName() {
		return this.name;
	}

	public Suite getBody() {
		return this.body;
	}

	public List<Argument> getInheritance() {
		return this.inheritance;
	}

	public List<Decorator> getDecorators() {
		return this.decorators;
	}

	public void setDecorators(List<Decorator> decorators) {
		this.decorators = decorators;
	}

	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visit(this);
	}
}
