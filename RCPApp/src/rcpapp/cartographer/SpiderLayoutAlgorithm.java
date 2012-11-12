package rcpapp.cartographer;

import java.util.Comparator;
import java.util.List;

import org.eclipse.zest.layouts.Filter;
import org.eclipse.zest.layouts.InvalidLayoutConfiguration;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.LayoutEntity;
import org.eclipse.zest.layouts.LayoutRelationship;
import org.eclipse.zest.layouts.progress.ProgressListener;

public class SpiderLayoutAlgorithm implements LayoutAlgorithm {

	public void applyLayout(LayoutEntity[] entitiesToLayout,
			LayoutRelationship[] relationshipsToConsider, double x, double y,
			double width, double height, boolean asynchronous,
			boolean continuous) throws InvalidLayoutConfiguration {
	}

	public boolean isRunning() {
		return false;
	}

	public void setComparator(Comparator comparator) {
	}

	public void setFilter(Filter filter) {
	}

	public void setEntityAspectRatio(double ratio) {
	}

	public double getEntityAspectRatio() {
		return 0;
	}

	public void addProgressListener(ProgressListener listener) {
	}

	public void removeProgressListener(ProgressListener listener) {
	}

	public void stop() {
	}

	public void setStyle(int style) {
	}

	public int getStyle() {
		return 0;
	}

	public void addEntity(LayoutEntity entity) {
	}

	public void addRelationship(LayoutRelationship relationship) {
	}

	public void removeEntity(LayoutEntity entity) {
	}

	public void removeRelationship(LayoutRelationship relationship) {
	}

	public void removeRelationships(List relationships) {
	}

}
