package fr.inria.diverse.torgen.inspectorguidget.processor;

import fr.inria.diverse.torgen.inspectorguidget.helper.SpoonHelper;
import fr.inria.diverse.torgen.inspectorguidget.helper.WidgetHelper;
import fr.inria.diverse.torgen.inspectorguidget.listener.WidgetListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.InvocationFilter;
import spoon.reflect.visitor.filter.VariableAccessFilter;

import java.util.*;
import java.util.logging.Level;

/**
 * Detects declaration of widgets.
 */
public class WidgetProcessor extends InspectorGuidgetProcessor<CtTypeReference<?>> {
	private Collection<CtTypeReference<?>> controlType;
	private final @NotNull Map<CtField<?>, List<CtVariableAccess<?>>> fields;
	/** The widgets created and directly added in a container. */
	private final @NotNull Map<CtTypeReference<?>, CtTypeReference<?>> references;
	private CtTypeReference<?> collectionType;
	private final @NotNull Set<WidgetListener> widgetObs;
	private final boolean withConfigStat;

	public WidgetProcessor() {
		this(false);
	}

	public WidgetProcessor(final boolean withConfigurationStatmts) {
		super();
		widgetObs = new HashSet<>();
		fields = new IdentityHashMap<>();
		references = new IdentityHashMap<>();
		withConfigStat = withConfigurationStatmts;
	}

	public void addWidgetObserver(final @NotNull WidgetListener obs) {
		widgetObs.add(obs);
	}

	@Override
	public void init() {
		LOG.log(Level.INFO, "init processor " + getClass().getSimpleName());

		collectionType = getFactory().Type().createReference(Collection.class);
		controlType = WidgetHelper.INSTANCE.getWidgetTypes(getFactory());
	}

	@Override
	public boolean isToBeProcessed(CtTypeReference<?> type) {
		return isASubTypeOf(type, controlType);
	}

	@Override
	public void process(final @NotNull CtTypeReference<?> element) {
		final CtElement parent = element.getParent();

		LOG.log(Level.INFO, () -> "PROCESSING " + element + " " + parent.getClass() + " " + parent.getParent().getClass());

		if(parent instanceof CtField<?>) {
			addNotifyObserversOnField((CtField<?>) parent, element);
			return;
		}
		if(parent instanceof CtExecutableReference<?> && parent.getParent() instanceof CtConstructorCall<?>) {
			analyseWidgetConstructorCall((CtConstructorCall<?>) parent.getParent(), element);
			return;
		}
		if(parent instanceof CtAssignment<?,?>) {
			analyseWidgetAssignment((CtAssignment<?, ?>) parent, element);
			return;
		}
		if(parent instanceof CtFieldReference<?>) {
			addNotifyObserversOnField(((CtFieldReference<?>) parent).getDeclaration(), element);
			return;
		}
		if(parent instanceof CtMethod<?>) {
			analyseMethodUse((CtMethod<?>) parent, element);
			return;
		}
		if(parent instanceof CtTypeReference<?>) {
			process((CtTypeReference<?>) parent);
			return;
		}
		if(parent instanceof CtExecutableReference<?>) {
			// A method is called on a widget, so ignored.
			return;
		}

		LOG.log(Level.WARNING, "CTypeReference parent not supported or ignored: " + parent.getClass() + " " + parent);
	}


	private void analyseMethodUse(final @NotNull CtMethod<?> meth, final CtTypeReference<?> element) {
		final ModifierKind visib = meth.getVisibility();
		if(visib == ModifierKind.PRIVATE) {
			meth.getParent(CtClass.class).getElements(new InvocationFilter(meth)).forEach(invok -> analyseWidgetInvocation(invok, element));
		}else if(visib == ModifierKind.PUBLIC) {
			meth.getFactory().Package().getRootPackage().getElements(new InvocationFilter(meth)).forEach(invok -> analyseWidgetInvocation(invok, element));
		}else if(visib == null || visib == ModifierKind.PROTECTED) {
			meth.getParent(CtPackage.class).getElements(new InvocationFilter(meth)).forEach(invok -> analyseWidgetInvocation(invok, element));
		}
	}


	private void analyseWidgetConstructorCall(final @NotNull CtConstructorCall<?> call, final CtTypeReference<?> element) {
		analyseWidgetUse(call.getParent(), element);
	}


	private void analyseWidgetUse(final CtElement elt, final CtTypeReference<?> refType) {
		if(elt instanceof CtAssignment<?, ?>) {
			analyseWidgetAssignment((CtAssignment<?, ?>) elt, refType);
			return;
		}
		if(elt instanceof CtInvocation<?>) {
			analyseWidgetInvocation((CtInvocation<?>) elt, refType);
			return;
		}

		if(elt instanceof CtLocalVariable<?>) {
			analyseUseOfLocalVariable(((CtLocalVariable<?>)elt).getReference(), elt.getParent(CtBlock.class), refType);
			return;
		}

		LOG.log(Level.WARNING, "Widget use not supported or ignored (" + SpoonHelper.INSTANCE.formatPosition(elt.getPosition()) +
				"): " + elt.getClass());
	}


	private void analyseUseOfLocalVariable(final @NotNull CtLocalVariableReference<?> var, final @Nullable CtBlock<?> block, final CtTypeReference<?> refType) {
		if(block==null) {
			LOG.log(Level.SEVERE, "No block ("+SpoonHelper.INSTANCE.formatPosition(var.getPosition())+"): " + var);
			return;
		}

		block.getElements(new VariableAccessFilter<>(var)).forEach(access -> analyseWidgetUse(access.getParent(), refType));
	}


	/**
	 * Object foo;
	 * foo = new JButton();
	 */
	private void analyseWidgetAssignment(final @NotNull CtAssignment<?,?> assign, final CtTypeReference<?> element) {
		final CtExpression<?> exp = assign.getAssigned();

		if(exp instanceof CtFieldWrite<?>) {
			addNotifyObserversOnField(((CtFieldWrite<?>) exp).getVariable().getDeclaration(), element);
		}else {
			LOG.log(Level.WARNING, "Widget Assignment not supported or ignored: " + exp.getClass() + " " + exp);
		}
	}


	/**
	 * JPanel panel = new JPanel();
	 * panel.add(new JWindow());
	 * ****
	 * List<Object> foo = new ArrayList<>();
	 * foo.add(new JMenuItem());
	 */
	private void analyseWidgetInvocation(final @NotNull CtInvocation<?> invok, final CtTypeReference<?> element) {
		final CtExpression<?> exp = invok.getTarget();

		if(exp==null) {
			LOG.log(Level.WARNING, "Cannot treat the widget invocation because of a null type: " + invok);
			return;
		}

		final CtTypeReference<?> type = exp.getType();

		if(type.isSubtypeOf(collectionType) && type.getParent() instanceof CtFieldReference<?>) {
			addNotifyObserversOnField(((CtFieldReference<?>) type.getParent()).getDeclaration(), element);
			return;
		}

		if(isASubTypeOf(type, controlType)) {
			addNotifyObserversOnContained(invok, element);
			return;
		}
		if(invok.getParent() instanceof CtAssignment<?,?>) {
			analyseWidgetAssignment((CtAssignment<?, ?>) invok.getParent(), element);
			return;
		}
		if(invok.getParent() instanceof CtInvocation<?> && invok.getParent() instanceof CtInvocation<?>) {
			// Calling a method on a collection.
			addNotifyObserversOnContained(invok, element);
			return;
		}

		LOG.log(Level.WARNING, "Widget invocation not supported or ignored: " + type.getSimpleName() + " " + invok);
	}


	private void addNotifyObserversOnContained(final CtInvocation<?> invok, final @Nullable CtTypeReference<?> element) {
		if(element!=null && references.putIfAbsent(element, element)==null) {
			widgetObs.forEach(o -> o.onWidgetCreatedForContainer(invok, element));
		}
	}

	private void addNotifyObserversOnField(final @Nullable CtField<?> field, final CtTypeReference<?> element) {
		if(field!=null && !fields.containsKey(field)) {
			final List<CtVariableAccess<?>> usages = extractUsagesOfWidgetField(field);
			fields.put(field, usages);
			widgetObs.forEach(o -> o.onWidgetAttribute(field, usages, element));
		}
	}

	private List<CtVariableAccess<?>> extractUsagesOfWidgetField(final CtField<?> field) {
		if(withConfigStat) {
			return SpoonHelper.INSTANCE.extractUsagesOfField(field);
		}
		return Collections.emptyList();
	}

	public @NotNull Map<CtField<?>, List<CtVariableAccess<?>>> getFields() {
		return Collections.unmodifiableMap(fields);
	}
}
