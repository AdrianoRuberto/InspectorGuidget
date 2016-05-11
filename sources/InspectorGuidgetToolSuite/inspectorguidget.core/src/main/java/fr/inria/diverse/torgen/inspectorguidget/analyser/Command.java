package fr.inria.diverse.torgen.inspectorguidget.analyser;

import org.jetbrains.annotations.NotNull;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;

import java.util.List;

public class Command {
	private final @NotNull List<CtStatement> statements;

	private final @NotNull List<CtExpression<Boolean>> conditions;

	public Command(final @NotNull List<CtStatement> stats, final @NotNull List<CtExpression<Boolean>> conds) {
		super();
		statements = stats;
		conditions = conds;
	}

	public int getLineStart() {
		return statements.stream().map(s -> s.getPosition()).filter(p -> p!=null).mapToInt(p -> p.getLine()).min().orElse(-1);
	}

	public int getLineEnd() {
		return statements.stream().map(s -> s.getPosition()).filter(p -> p!=null).mapToInt(p -> p.getEndLine()).max().orElse(-1);
	}

	public List<CtExpression<Boolean>> getConditions() {
		return conditions;
	}

	public List<CtStatement> getStatements() {
		return statements;
	}
}