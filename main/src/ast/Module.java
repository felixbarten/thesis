package ast;

import ast.statement.Statement;
import org.antlr.v4.runtime.misc.NotNull;
import thesis.StringHelper;

import java.util.List;

/**
 * Created by Nik on 25-05-15
 */
public class Module extends AstNode {

    private final List<Statement> body;

    public Module(@NotNull LocInfo locInfo, @NotNull List<Statement> body) {
        super(locInfo);
        this.body = body;
    }

    public List<Statement> getBody() {
        return this.body;
    }

    public String getFilePath() {
        return this.locInfo.getFilePath();
    }

	public String getName() {
		List<String> filePathParts = StringHelper.explode(this.locInfo.getFilePath(), "\\");
		String s = filePathParts.get(filePathParts.size() - 1);
		return s.substring(0, s.length() - 3);
	}

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
