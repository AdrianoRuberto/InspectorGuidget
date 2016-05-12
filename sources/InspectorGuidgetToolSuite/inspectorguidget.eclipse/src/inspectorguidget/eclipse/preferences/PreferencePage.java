package inspectorguidget.eclipse.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import inspectorguidget.eclipse.Activator;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public PreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Files where results of InspectorGuidget analyses are stored");
	}

	@Override
	public void init(IWorkbench workbench) {
		// Nothing to do.
	}

	@Override
	protected void createFieldEditors() {
		addField(new StringFieldEditor("pathBlobListeners", "Blob listeners: ", getFieldEditorParent()));
		addField(new StringFieldEditor("pathBlobListeners2", "Blob listeners2: ", getFieldEditorParent()));
		addField(new StringFieldEditor("pathCommand", "Commands: ", getFieldEditorParent()));
		addField(new StringFieldEditor("pathCondListeners", "Cond listeners: ", getFieldEditorParent()));
		addField(new StringFieldEditor("pathListeners", "Listeners: ", getFieldEditorParent()));
	}
}
