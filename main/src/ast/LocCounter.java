package ast;

import util.FileOpener;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Nik on 23-07-2015
 */
public class LocCounter {

	private final static String emptyOrCommentRegex = "([ \t]*|[ \t]*#.*)";

	private final FileOpener fileOpener;

	public LocCounter(String module) {
		this.fileOpener = new FileOpener(module);
	}

	public Integer count() {
		List<String> lines = this.fileOpener.getLines();
		return this.count(lines);
	}

	public Integer count(Integer startLine, Integer endLine) {
		List<String> lines = this.fileOpener.getLines(startLine - 1, endLine);
		return this.count(lines);
	}

	private Integer count(List<String> lines) {
		Long nr = lines.stream().filter(this::countsTowardsLoc).count();
		return nr.intValue();
	}

	public boolean countsTowardsLoc(String line) {
		return !this.isEmptyOrComment(line);
	}

	private boolean isEmptyOrComment(String line) {
		Pattern emptyOrComment = Pattern.compile(emptyOrCommentRegex);
		Matcher matcher = emptyOrComment.matcher(line);
		return matcher.matches();
	}
}
