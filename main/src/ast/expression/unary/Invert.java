package ast.expression.unary;

import ast.LocInfo;
import ast.expression.Expr;
import org.antlr.v4.runtime.misc.NotNull;
import ast.Visitor;

/**
 * Created by Nik on 10-06-2015
 */
public class Invert extends Unary {
	public Invert(@NotNull LocInfo locInfo, @NotNull Expr value) {
		super(locInfo, value);
	}

	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visit(this);
	}
}
