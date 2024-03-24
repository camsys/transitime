package org.transitclock.ipc.data;

import org.transitclock.db.structs.StopInterface;

import java.io.Serializable;

public class IpcStopWithDwellTime extends IpcStop implements Serializable {

    private final Long dwellTime;

    private final int configRev;

    public IpcStopWithDwellTime(StopInterface dbStop, boolean aUiStop, String directionId, Double stopPathLength, Long dwellTime) {
        super(dbStop, aUiStop, directionId, stopPathLength);
        this.dwellTime = dwellTime;
        this.configRev = dbStop.getConfigRev();
    }

    /**
     * Constructs a stop and sets isUiStop to true.
     *
     * @param dbStop
     */
    public IpcStopWithDwellTime(StopInterface dbStop, String directionId, Long dwellTime) {
        super(dbStop,directionId);
        this.dwellTime = dwellTime;
        this.configRev = dbStop.getConfigRev();
    }

    public Long getDwellTime() {
        return dwellTime;
    }

    public int getConfigRev() {
        return configRev;
    }

    @Override
    public String toString() {
        return "IpcStopWithDwellTime{"
                + "id=" + getId()
                + ", name=" + getName()
                + ", code=" + getCode()
                + ", loc=" + getLoc()
                + ", isUiStop=" + isUiStop()
                + ", directionId" + getDirectionId()
                + ", dwellTime=" + dwellTime
                + '}';
    }
}
