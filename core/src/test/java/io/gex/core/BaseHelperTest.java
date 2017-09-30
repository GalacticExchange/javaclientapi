package io.gex.core;


import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class BaseHelperTest {

    @Test
    public void checkCompareVersions() {
        assertTrue(BaseHelper.compareAppVersions("5.0.24_Ubuntur108355", "5.0.0") > 0);

        assertTrue(BaseHelper.compareAppVersions("5.0.24r108355", "5.0.0") > 0);

        assertTrue(BaseHelper.compareAppVersions("5.0.24r108355", "5.0.25") < 0);

        assertTrue(BaseHelper.compareAppVersions("5.0.24r108355", "5.0") > 0);
    }
}
