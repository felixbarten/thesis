package ast.expression.nocond.atom;

import ast.Visitor;
import ast.expression.ExprList;

/**
 * Created by Nik on 02-09-2015
 */
public class StrConversion extends Atom {

	private final ExprList exprList;

	public StrConversion( Integer locInfo,  ExprList exprList) {
		super(locInfo);
		this.exprList = exprList;
	}

	public ExprList getExprList() {
		return this.exprList;
	}

	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visit(this);
	}
}
