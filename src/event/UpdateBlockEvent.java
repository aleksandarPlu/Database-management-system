package event;

import java.util.EventObject;

public class UpdateBlockEvent extends EventObject{

	private static final long serialVersionUID = 1L;

	public UpdateBlockEvent(Object source) {
		super(source);
	}

}
