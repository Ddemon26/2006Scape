import static org.junit.Assert.*;
import java.util.Locale;
import org.junit.Test;
public class GameQuantityFormatTest {
 @Test public void groupedStringsCrossBothSuffixBoundariesAndRetainFullCount() {
  int[] values={0,1,99,100,999,1000,1001,9999,10000,99999,100000,999999,1000000,1000001,Integer.MAX_VALUE};
  String[] expected={" 0"," 1"," 99"," 100"," 999"," @cya@1K @whi@(1,000)"," @cya@1K @whi@(1,001)"," @cya@9K @whi@(9,999)"," @cya@10K @whi@(10,000)"," @cya@99K @whi@(99,999)"," @cya@100K @whi@(100,000)"," @cya@999K @whi@(999,999)"," @gre@1 million @whi@(1,000,000)"," @gre@1 million @whi@(1,000,001)"," @gre@2,147 million @whi@(2,147,483,647)"};
  for(int n=0;n<values.length;n++)assertEquals("input "+values[n],expected[n],Game.intToKOrMilLongName(values[n]));
 }
 @Test public void negativeGroupingPreservesSignCountingQuirk() {
  int[] values={-1,-99,-100,-999,-1000,-99999,-100000,Integer.MIN_VALUE};
  String[] expected={" -1"," -99"," @cya@-K @wh@(-,100)"," @cya@-K @wh@(-,999)"," @cya@-1K @wh@(-1,000)"," @cya@-99K @wh@(-99,999)"," @gre@- million @wh@(-,100,000)"," @gre@-2,147 million @wh@(-2,147,483,648)"};
  for(int n=0;n<values.length;n++)assertEquals(expected[n].replace("@wh@","@whi@"),Game.intToKOrMilLongName(values[n]));
 }
 @Test public void billionPrefixRetainsItsInternalGroupingComma() {
  assertEquals(" @gre@1,000 million @whi@(1,000,000,000)",Game.intToKOrMilLongName(1000000000));
  assertEquals(" @gre@1,234 million @whi@(1,234,567,890)",Game.intToKOrMilLongName(1234567890));
  assertEquals("1000M",Game.intToKOrMil(1000000000));
 }
 @Test public void compactCountUsesHundredThousandAndTenMillionThresholds() {
  int[] values={Integer.MIN_VALUE,-10000000,-1,0,99999,100000,100999,101000,999999,1000000,9999999,10000000,10999999,11000000,Integer.MAX_VALUE};
  String[] expected={"-2147483648","-10000000","-1","0","99999","100K","100K","101K","999K","1000K","9999K","10M","10M","11M","2147M"};
  for(int n=0;n<values.length;n++)assertEquals(expected[n],Game.intToKOrMil(values[n]));
 }
 @Test public void decimalValuesDistinguishRoundingAndTruncationAtEveryThreshold() {
  Locale previous=Locale.getDefault();try{Locale.setDefault(Locale.US);
   long[] values={Long.MIN_VALUE,-1000000000L,-1,0,999,1000,1250,1350,9999,10000,10999,999999,1000000,1250000,1350000,9999999,10000000,10999999,999999999,1000000000,1250000000,1350000000,9007199254740993L,Long.MAX_VALUE};
   String[] expected={"-9223372036854775808","-1000000000","-1","0","999","1.0K","1.2K","1.4K","10.0K","10K","10K","999K","1.0M","1.2M","1.4M","10.0M","10M","10M","999M","1.0B","1.2B","1.4B","9007199.3B","9223372036.9B"};
   for(int n=0;n<values.length;n++)assertEquals("input "+values[n],expected[n],Game.intToShortLetter(values[n]));
  }finally{Locale.setDefault(previous);}
 }
 @Test public void decimalSeparatorFollowsLocaleButOtherFormatsDoNot() {
  Locale previous=Locale.getDefault();try{Locale.setDefault(Locale.GERMANY);
   assertEquals("1,2K",Game.intToShortLetter(1250));assertEquals("1,4M",Game.intToShortLetter(1350000));assertEquals("10K",Game.intToShortLetter(10000));
   assertEquals(" @gre@1 million @whi@(1,000,000)",Game.intToKOrMilLongName(1000000));assertEquals("100K",Game.intToKOrMil(100000));
  }finally{Locale.setDefault(previous);}
 }
}
