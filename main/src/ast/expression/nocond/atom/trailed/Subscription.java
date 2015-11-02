package ast.expression.nocond.atom.trailed;

import ast.Visitor;
import ast.expression.nocond.atom.Atom;
import ast.expression.nocond.trailer.SubscriptSliceList;
import org.antlr.v4.runtime.misc.NotNull;

/**
 * Created by Nik on 07-07-2015
 */
public class Subscription extends TrailedAtom {

	private final SubscriptSliceList subscript;

	public Subscription(@NotNull Integer locInfo, @NotNull Atom base, @NotNull SubscriptSliceList subscript) {
		super(locInfo, base);
		this.subscript = subscript;
	}

	public SubscriptSliceList getSubscript() {
		return this.subscript;
	}

	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visit(this);
	}
}