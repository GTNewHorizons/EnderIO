package crazypants.util;

import com.gtnewhorizon.gtnhlib.color.ColorResource;

public class ColorUtils {

    private static final ColorResource.Factory color = new ColorResource.Factory("enderio");

    public static final ColorResource
    // spotless:off
        priorityString  = color.rgb("priorityString",   "0x000000"),
        suctionString   = color.rgb("suctionString",    "0x000000");
    // spotless:on
}
