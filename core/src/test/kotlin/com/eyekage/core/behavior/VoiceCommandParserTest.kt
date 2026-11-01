package com.eyekage.core.behavior

import com.eyekage.core.model.EyeModeId
import com.eyekage.core.model.VoiceCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VoiceCommandParserTest {

    @Test
    fun parse_nullOrBlank_returnsNull() {
        assertNull(VoiceCommandParser.parse(null))
        assertNull(VoiceCommandParser.parse(""))
        assertNull(VoiceCommandParser.parse("   "))
        assertNull(VoiceCommandParser.parse("!@#$$%"))
    }

    @Test
    fun parse_unrecognizedSpeech_returnsNull() {
        assertNull(VoiceCommandParser.parse("potato salad"))
        assertNull(VoiceCommandParser.parse("what is the weather today"))
        assertNull(VoiceCommandParser.parse("random background noise"))
    }

    @Test
    fun parse_awakenSynonyms_returnsTriggerAwakening() {
        assertEquals(VoiceCommand.TriggerAwakening, VoiceCommandParser.parse("awaken"))
        assertEquals(VoiceCommand.TriggerAwakening, VoiceCommandParser.parse("Awaken!"))
        assertEquals(VoiceCommand.TriggerAwakening, VoiceCommandParser.parse("wake up"))
        assertEquals(VoiceCommand.TriggerAwakening, VoiceCommandParser.parse("Hello EyeKage"))
        assertEquals(VoiceCommand.TriggerAwakening, VoiceCommandParser.parse("eyekage awaken"))
        assertEquals(VoiceCommand.TriggerAwakening, VoiceCommandParser.parse("Hey EyeKage!"))
    }

    @Test
    fun parse_sleepSynonyms_returnsSleep() {
        assertEquals(VoiceCommand.Sleep, VoiceCommandParser.parse("sleep"))
        assertEquals(VoiceCommand.Sleep, VoiceCommandParser.parse("Go to sleep"))
        assertEquals(VoiceCommand.Sleep, VoiceCommandParser.parse("rest"))
        assertEquals(VoiceCommand.Sleep, VoiceCommandParser.parse("goodnight, eyekage"))
    }

    @Test
    fun parse_blinkSynonyms_returnsBlink() {
        assertEquals(VoiceCommand.Blink, VoiceCommandParser.parse("blink"))
        assertEquals(VoiceCommand.Blink, VoiceCommandParser.parse("wink"))
        assertEquals(VoiceCommand.Blink, VoiceCommandParser.parse("Please blink!"))
    }

    @Test
    fun parse_centerSynonyms_returnsCenterGaze() {
        assertEquals(VoiceCommand.CenterGaze, VoiceCommandParser.parse("center"))
        assertEquals(VoiceCommand.CenterGaze, VoiceCommandParser.parse("look forward"))
        assertEquals(VoiceCommand.CenterGaze, VoiceCommandParser.parse("look at me"))
        assertEquals(VoiceCommand.CenterGaze, VoiceCommandParser.parse("center gaze"))
    }

    @Test
    fun parse_crimsonSynonyms_returnsSwitchModeCrimson() {
        val expected = VoiceCommand.SwitchMode(EyeModeId.CRIMSON_TRACKER)
        assertEquals(expected, VoiceCommandParser.parse("crimson"))
        assertEquals(expected, VoiceCommandParser.parse("crimson tracker"))
        assertEquals(expected, VoiceCommandParser.parse("Red eyes!"))
        assertEquals(expected, VoiceCommandParser.parse("EyeKage, sharingan!"))
    }

    @Test
    fun parse_whiteScanSynonyms_returnsSwitchModeWhiteScan() {
        val expected = VoiceCommand.SwitchMode(EyeModeId.WHITE_SCAN)
        assertEquals(expected, VoiceCommandParser.parse("white"))
        assertEquals(expected, VoiceCommandParser.parse("white scan"))
        assertEquals(expected, VoiceCommandParser.parse("scan mode"))
        assertEquals(expected, VoiceCommandParser.parse("byakugan"))
    }

    @Test
    fun parse_purpleOrbitSynonyms_returnsSwitchModePurpleOrbit() {
        val expected = VoiceCommand.SwitchMode(EyeModeId.PURPLE_ORBIT)
        assertEquals(expected, VoiceCommandParser.parse("purple"))
        assertEquals(expected, VoiceCommandParser.parse("purple orbit"))
        assertEquals(expected, VoiceCommandParser.parse("orbit mode"))
        assertEquals(expected, VoiceCommandParser.parse("rinnegan"))
    }

    @Test
    fun parse_normalSynonyms_returnsSwitchModeNormal() {
        val expected = VoiceCommand.SwitchMode(EyeModeId.NORMAL)
        assertEquals(expected, VoiceCommandParser.parse("normal"))
        assertEquals(expected, VoiceCommandParser.parse("normal eyes"))
        assertEquals(expected, VoiceCommandParser.parse("default mode"))
        assertEquals(expected, VoiceCommandParser.parse("reset mode"))
    }
}
