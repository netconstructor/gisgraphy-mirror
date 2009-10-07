package com.gisgraphy.helper;

import junit.framework.Assert;

import org.junit.Test;

public class StringHelperTest {

    @Test
    public void testTransformStringForFulltextIndexation() {
	Assert.assertEquals("letter without accent should not be modified"," e e e e e je me souviens de ce zouave qui jouait du xylophone en buvant du whisky c est ok c est super",
		StringHelper.TransformStringForFulltextIndexation("-é \u00E8 \u00E9 \u00EA \u00EB JE ME SOUVIENS de ce zouave qui jouait du-xylophone en buvant.du whisky c'est ok c\"est super"));
    }
    
    @Test
    public void testTransformStringForFulltextIndexationForNullString() {
	Assert.assertNull(StringHelper.TransformStringForFulltextIndexation(null));
    }

}
