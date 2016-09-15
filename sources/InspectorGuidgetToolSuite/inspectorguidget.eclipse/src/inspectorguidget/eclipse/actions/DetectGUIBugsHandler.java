package inspectorguidget.eclipse.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class DetectGUIBugsHandler extends AbstractHandler {
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
//		new ClearMarkersAction().run(null);
		new DetectGUIBugsAction().run(null);
		return null;
	}
}
