package it.unitn.disi.logcompliance.prediction.tools.objects;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;

public class QueryConnection {
	static final public short WALK = 1;
	static final public short nWALK = 2;
	static final public short FLOW = 3;
	static final public short nFLOW = 4;
	
	private int connection;
	private QueryTask a,b;
	
	public QueryConnection(int connection, QueryTask a, QueryTask b) {
		this.a = a;
		this.b = b;
		this.connection = connection;
	}
	public QueryConnection(int connection) {}
	
	public int getConnection() {
		return connection;
	}

	public void setConnection(int connection) {
		if(connection<1 || connection > 4)
			connection = 3;
		this.connection = connection;
	}

	public QueryTask getA() {
		return a;
	}

	public void setA(QueryTask a) {
		this.a = a;
	}

	public QueryTask getB() {
		return b;
	}

	public void setB(QueryTask b) {
		this.b = b;
	}
	@Override
	public String toString() {
		String res = "";
		switch(connection) {
			case WALK:
				res = a.getName() + " walk to " + b.getName();
			   break;
		   	case nWALK:
		   		res = a.getName() + " negative walk to " + b.getName();
			   break;
		   	case FLOW:
		   		res = a.getName() + " flow to " + b.getName();
			   break;
		   	case nFLOW:
		   		res = a.getName() + " sequence to " + b.getName();
		}
		return res;
	}
}
