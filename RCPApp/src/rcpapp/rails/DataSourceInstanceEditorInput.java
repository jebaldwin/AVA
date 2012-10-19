package rcpapp.rails;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class DataSourceInstanceEditorInput implements IEditorInput {
	private final Integer inst_id;
	
	public DataSourceInstanceEditorInput(Integer inst_id) {
		this.inst_id = inst_id;
	}
	
	public Integer getId() {
		return this.inst_id;
	}
	
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.inst_id;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DataSourceInstanceEditorInput other = (DataSourceInstanceEditorInput) obj;
        if (inst_id != other.inst_id)
            return false;
        return true;
    }
	
	public Object getAdapter(Class adapter) {
		return null;
	}

	public boolean exists() {
		return true;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return String.valueOf(this.inst_id);
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return String.valueOf(this.inst_id);
	}
}
