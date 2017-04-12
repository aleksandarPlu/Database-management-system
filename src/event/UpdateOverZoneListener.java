package event;

import java.util.EventListener;

public interface UpdateOverZoneListener extends EventListener{
	public void updateOverZonePerformed(UpdateOverZoneEvent e);
}
