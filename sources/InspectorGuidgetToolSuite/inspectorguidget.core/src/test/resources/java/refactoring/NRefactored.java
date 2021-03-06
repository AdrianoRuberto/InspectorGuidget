
package java.refactoring;
import javax.swing.JFileChooser;
class N {
	protected JFileChooser fileChooser;
	javax.swing.JButton but1;
	javax.swing.JButton but2;
	N() {
		but1 = new javax.swing.JButton("foo1");
		fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setMultiSelectionEnabled(false);
		but1.addActionListener((java.awt.event.ActionEvent e) -> {
			int id = fileChooser.showDialog(null, "foo");
			if (id == (JFileChooser.APPROVE_OPTION)) {
				java.lang.System.out.println("foo");
			}
		});
		but2 = new javax.swing.JButton("foo2");
		but2.addActionListener((java.awt.event.ActionEvent e) -> {
			int id = fileChooser.showDialog(null, "bar");
			if (id == (JFileChooser.APPROVE_OPTION)) {
				java.lang.System.out.println("bar");
			}
		});
	}
}
class NListener {
	protected JFileChooser fileChooser;
	public NListener() {
		fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setMultiSelectionEnabled(false);
	}
}

