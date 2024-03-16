package org.transitclock.core.reporting;

import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.transitclock.applications.Core;
import org.transitclock.db.structs.*;
import org.transitclock.reporting.service.runTime.prescriptive.helper.DatedGtfsService;
import org.transitclock.reporting.service.runTime.prescriptive.model.DatedGtfs;
import org.transitclock.utils.Time;

import java.util.List;
import java.util.TimeZone;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


public class PrescriptiveDatedGTFSTest extends AbstractPrescriptiveRunTimesTests{

    private MockedStatic<Core> singletonCore;

    @Test
    public void prescriptiveDatedGtfs() throws Exception {
        String servicePeriod = "169 - 2022-04-18 - 2022-06-12";

        List<FeedInfo> feedInfos = getFeedInfo(servicePeriod);

        // Mock Core
        Time time = new Time(TimeZone.getDefault().getDisplayName());
        Core mockCore = mock(Core.class, Mockito.RETURNS_DEEP_STUBS);

        // Setup static mocks
        singletonCore = mockStatic(Core.class);
        singletonCore.when(() -> Core.getInstance()).thenReturn(mockCore);

        when(mockCore.getTime()).thenReturn(time);
        when(mockCore.getDbConfig().getFeedInfos()).thenReturn(feedInfos);

        List<DatedGtfs> datedGtfsList = DatedGtfsService.getDatedGtfs();

        assertEquals(3, datedGtfsList.size());

        for(DatedGtfs datedGtfs : datedGtfsList){
            System.out.println(datedGtfs.toString());
        }

        singletonCore.close();
    }


}
