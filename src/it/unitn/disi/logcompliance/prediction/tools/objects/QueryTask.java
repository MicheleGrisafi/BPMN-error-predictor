package it.unitn.disi.logcompliance.prediction.tools.objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class QueryTask {
	private String name;
	public QueryTask(String name) {
		setName(name);
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		if(name.startsWith("@"))
			name = "@";
		this.name = name;
	}
	@Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(name).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
       if (!(obj instanceof QueryTask))
            return false;
        if (obj == this)
            return true;
        QueryTask rhs = (QueryTask) obj;
        return new EqualsBuilder().append(name, rhs.name).isEquals();
    }
    public String presenceAnnotation() {
    	return name + " missing";
    }
}
