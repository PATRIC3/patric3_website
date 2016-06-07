package edu.vt.vbi.patric.portlets;

import java.util.Comparator;

public class GenomeGroupNameComparator implements Comparator<org.patricbrc.Workspace.ObjectMeta> {
	public int compare(org.patricbrc.Workspace.ObjectMeta item1, org.patricbrc.Workspace.ObjectMeta item2) {

		String name1 = item1.e_1;
		String name2 = item2.e_1;

		return name1.compareToIgnoreCase(name2);
	}
}