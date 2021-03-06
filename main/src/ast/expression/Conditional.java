package ast.expression;

import ast.Visitor;

/**
 * Created by Nik on 14-06-2015
 */
public class Conditional extends Expr {

	private final Expr value;
	private final Expr condition;
	private final Expr falseValue;

	public Conditional( Integer locInfo,  Expr value,  Expr condition,
	                    Expr falseValue) {
		super(locInfo);
		this.value = value;
		this.condition = condition;
		this.falseValue = falseValue;
	}

	public Expr getValue() {
		return this.value;
	}

	public Expr getCondition() {
		return this.condition;
	}

	public Expr getFalseValue() {
		return this.falseValue;
	}

	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visit(this);
	}
}
