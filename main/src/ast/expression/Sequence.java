package ast.expression;

import ast.LocInfo;

/**
 * Created by Nik on 25-5-15.
 */
public abstract class Sequence extends Expr {

    public Sequence(LocInfo locInfo) {
        super(locInfo);
    }

}