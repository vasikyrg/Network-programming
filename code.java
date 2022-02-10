import ithakimodem.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.*;
import java.util.stream.Stream;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.imageio.ImageIO;
public class virtualModem {
public static void main(String[] param) throws Exception {
(new virtualModem()).demo();
}
public void demo() throws Exception {
int k;
Modem modem;
modem=new Modem();
modem.setSpeed(80000);
modem.setTimeout(2000);
modem.open("ithaki");
for (;;) {
try {
k=modem.read();
if (k==-1) break;
System.out.print((char)k);
} catch (Exception x) {break;}
}
//initialize
String Echo = "E3176"; // Θα χρειάζονται αλλαγή κάφε φορά
String imageEF = "M6123";
String imageWE = "G4699";
String GPS = "P6181";
String ACK = "Q9924";
String NACK = "R0534";
//echoRequestCode
byte[] echoData = new byte[Echo.length()+1];
for(int i=0; i<Echo.length(); i++)
echoData[i] = (byte)Echo.charAt(i);
echoData[Echo.length()] = (byte)'\r';
ArrayList<Long> echopacks = new ArrayList<>();
FileWriter Echopackets= new FileWriter("Echopackets.txt");
BufferedWriter out = new BufferedWriter(Echopackets);
long startTime=System.currentTimeMillis();
while(System.currentTimeMillis()-startTime<=300000) {
long t1=System.currentTimeMillis();
modem.write(echoData);
for (;;) {
try {
k = modem.read();
if (k==-1) break;
long t2=System.currentTimeMillis();
echopacks.add(t2-t1);
} catch (Exception x) {break;}
}
long lastIndexTimeEcho = echopacks.get(echopacks.size()-1);
out.write(" \n "+lastIndexTimeEcho);
}
echopacks.clear();
out.close();
//ACK NACK
byte[] ACKData = new byte[ACK.length()+1];
for(int i=0; i<ACK.length(); i++)
ACKData[i] = (byte)ACK.charAt(i);
ACKData[ACK.length()] = (byte)'\r';
ArrayList<Long> ACKTimeData = new ArrayList<>();
FileWriter CorrectPackets = new FileWriter("CorrectPackets.txt");
BufferedWriter out2 = new BufferedWriter(CorrectPackets);
FileWriter ACKpackets = new FileWriter("ACKpackets.txt");
BufferedWriter out3 = new BufferedWriter(ACKpackets);
FileWriter NACKpackets2 = new FileWriter("NACKpackets.txt");
BufferedWriter out4 = new BufferedWriter(NACKpackets2);
int fullCounter = 0;
long startTime2=System.currentTimeMillis();
while(System.currentTimeMillis()-startTime2<=300000) {
fullCounter++;
ArrayList<Character> ACKArray = new ArrayList<Character>();
long ACKt1=System.currentTimeMillis();
modem.write(ACKData);
for (;;) {
try {
k = modem.read();
if (k==-1) break;
long ACKt2=System.currentTimeMillis();
ACKTimeData.add(ACKt2 - ACKt1);
ACKArray.add((char)k);
} catch (Exception x) {break;}
}
char[] Crypto16 = new char[16];
String FCString = "" ;
for(int i=31; i<47; i++)
Crypto16[i-31] = ACKArray.get(i);
for(int i=49; i<52; i++)
FCString += ACKArray.get(i);
int FCS = Integer.parseInt(FCString);
long lastIndexTime = ACKTimeData.get(ACKTimeData.size()-1);
int XOR = (int)(Crypto16[0]) ^ (int)(Crypto16[1]);
for(int i=2; i<Crypto16.length; i++)
XOR = XOR ^ (int)(Crypto16[i]);
if(FCS == XOR) {
out2.write("\n "+lastIndexTime+"");
out3.write("\n "+lastIndexTime+"");
ACKTimeData.clear();
}
else {
byte[] NACKData = new byte[NACK.length()+1];
ArrayList<Long> NACKTimeData = new ArrayList<>();
for(int i=0; i<NACK.length(); i++)
NACKData[i] = (byte)NACK.charAt(i);
NACKData[NACK.length()] = (byte)'\r';
int XOR2 = 0;
int counter = 0;
while(XOR2 != FCS) {
counter++;
modem.write(NACKData);
ArrayList<Character> NACKArray = new ArrayList<Character>();
for (;;) {
try {
k = modem.read();
if (k==-1) break;
long NACKt2=System.currentTimeMillis();
NACKTimeData.add(NACKt2 - ACKt1);
NACKArray.add((char)k);
}
catch (Exception x) {break;}
}
char[] Crypto16s = new char[16];
for(int i=31; i<47; i++)
Crypto16s[i-31] = NACKArray.get(i);
XOR2 = (int)(Crypto16s[0]) ^ (int)(Crypto16s[1]);
for(int i=2; i<Crypto16s.length; i++)
XOR2 = XOR2 ^ (int)(Crypto16s[i]);
}
long lastIndexTimeN = NACKTimeData.get(NACKTimeData.size()-1);
NACKTimeData.clear();
out2.write(" \n "+lastIndexTimeN);
out4.write("\nACKArray["+fullCounter+"] needs "+counter+" time/s");
}
}
out2.close();
out3.close();
out4.close();
//imageRequestCode Error FREE
byte[] imageEFData = new byte[imageEF.length()+1];
for(int i=0; i<imageEF.length(); i++)
imageEFData[i] = (byte)imageEF.charAt(i);
imageEFData[imageEF.length()] = (byte)'\r';
modem.write(imageEFData);
ArrayList<Byte> ImageEFData = new ArrayList<Byte>();
for (;;) {
try {
k = modem.read();
ImageEFData.add((byte)k);
if (k==-1) break;
} catch (Exception x) {break;}
}
byte[] imageEFDataArray = new byte[ImageEFData.size()];
for(int i=0; i<imageEFDataArray.length; i++) {
imageEFDataArray[i] = ImageEFData.get(i);
}
try {
ByteArrayInputStream imageErrorFREE= new ByteArrayInputStream(imageEFDataArray);
BufferedImage finalimageEF = ImageIO.read(imageErrorFREE);
ImageIO.write(finalimageEF , "jpg", new File("imageRequestCode Error FREE.jpg") );
System.out.println("\nConverted Successfully!");
} catch(Exception x) {}
ImageEFData.clear();
//imageRequestCode With Error
byte[] imageWEData = new byte[imageWE.length()+1];
for(int i=0; i<imageWE.length(); i++)
imageWEData[i] = (byte)imageWE.charAt(i);
imageWEData[imageWE.length()] = (byte)'\r';
modem.write(imageWEData);
ArrayList<Byte> ImageWEData = new ArrayList<Byte>();
for (;;) {
try {
k = modem.read();
ImageWEData.add((byte)k);
if (k==-1) break;
} catch (Exception x) {
break;
}
}
byte[] imageWEDataArray = new byte[ImageWEData.size()];
for(int i=0; i<imageWEDataArray.length; i++) {
imageWEDataArray[i] = ImageWEData.get(i);
}
try {
ByteArrayInputStream imageWithError= new ByteArrayInputStream(imageWEDataArray);
BufferedImage finalimageWE = ImageIO.read(imageWithError);
ImageIO.write(finalimageWE , "jpg", new File("imageRequestCode With Error.jpg") );
System.out.println("\nConverted Successfully!");
} catch(Exception x) {}
ImageWEData.clear();
//GPS
String R = "R=10030";
String ixni = "50\r";
String GPSFull = GPS + R+ ixni;
byte[] GPSData = new byte[GPSFull.length()];
for(int i=0; i<GPSFull.length(); i++)
GPSData[i] = (byte)GPSFull.charAt(i);
modem.write(GPSData);
ArrayList<Character> GPSArray = new ArrayList<>();
for (;;) {
try {
k = modem.read();
GPSArray.add((char)k);
if (k==-1) break;
} catch (Exception x) {break;}
}
String[] latitude = new String[4];
for(int i=0; i<4; i++) {
latitude[i] = "";
}
for(int i=45; i<54; i++){
latitude[0] += GPSArray.get(i);
}
for(int i=(45+5*76); i<(54+5*76); i++){
latitude[1] += GPSArray.get(i);
}
for(int i=(45+15*76); i<(54+15*76); i++){
latitude[2] += GPSArray.get(i);
}
for(int i=(45+35*76); i<(54+35*76); i++){
latitude[3] += GPSArray.get(i);
}
String[] temp1 = new String[4];
String[] temp3 = new String[4];
String[] latitudeDec = new String[4];
String[] latitudeInt = new String[4];
for(int i=0; i<4; i++) {
int index1=0;
temp1[i] = latitude[i];
index1 = temp1[i].lastIndexOf(".");
latitudeDec[i] = temp1[i].substring(index1+1);
temp3[i] = latitude[i];
int index3 = temp3[i].indexOf(".");
latitudeInt[i] = latitude[i].substring(0, index3);
}
for(int i=0; i<4; i++)
latitudeDec[i] = String.valueOf((Integer.parseInt(latitudeDec[i]))*0.006);
for(int i=0; i<latitudeDec.length; i++)
latitudeDec[i] = String.valueOf(Math.floor(Double.valueOf(latitudeDec[i])));
String[] finalLatitude = new String[4];
for(int i=0; i<finalLatitude.length; i++) {
finalLatitude[i] = latitudeInt[i] + latitudeDec[i];
finalLatitude[i] = finalLatitude[i].replaceFirst(".$", "");
finalLatitude[i] = finalLatitude[i].replaceFirst(".$", "");
}
String[] longitude = new String[4];
for(int i=0; i<4; i++)
longitude[i] = "";
for(int i=58; i<67; i++)
longitude[0] += GPSArray.get(i);
for(int i=(58+5*76); i<(67+5*76); i++)
longitude[1] += GPSArray.get(i);
for(int i=(58+15*76); i<(67+15*76); i++)
longitude[2] += GPSArray.get(i);
for(int i=(58+35*76); i<(67+35*76); i++)
longitude[3] += GPSArray.get(i);
String[] temp2 = new String[4];
String[] temp4 = new String[4];
String[] longitudeDec = new String[4];
String[] longitudeInt = new String[4];
for(int i=0; i<4; i++) {
int index2=0;
temp2[i] = longitude[i];
index2 = temp2[i].lastIndexOf(".");
longitudeDec[i] = temp2[i].substring(index2+1);
temp4[i] = longitude[i];
int index4 = temp4[i].indexOf(".");
longitudeInt[i] = longitude[i].substring(0, index4);
}
for(int i=0; i<4; i++)
longitudeDec[i] = String.valueOf((Integer.parseInt(longitudeDec[i]))*0.006);
for(int i=0; i<longitudeDec.length; i++)
longitudeDec[i] = String.valueOf(Math.floor(Double.valueOf(longitudeDec[i])));
String[] finalLongitude = new String[4];
for(int i=0; i<finalLongitude.length; i++) {
finalLongitude[i] = longitudeInt[i] + longitudeDec[i];
finalLongitude[i] = finalLongitude[i].replaceFirst(".$", "");
finalLongitude[i] = finalLongitude[i].replaceFirst(".$", "");
}
String T = "T=";
String[] GPSFull2 = new String[1] ;
for(int i=0; i<GPSFull2.length; i++)
GPSFull2[i] = "";
for(int i=0; i<4; i++)
GPSFull2[0] += GPS + T + finalLongitude[i] + finalLatitude[i];
byte[] GPSData2 = new byte[GPSFull2[0].length()+1];
for(int i=0; i<GPSFull2[0].length(); i++)
GPSData2[i] = (byte)GPSFull2[0].charAt(i);
GPSData2[GPSFull2[0].length()] = (byte)'\r';
modem.write(GPSData2);
ArrayList<Byte> GPSDataArray = new ArrayList<Byte>();
for (;;) {
try {
k = modem.read();
GPSDataArray.add((byte)k);
if (k==-1) {
break;
}
}
catch (Exception x) {
break;
}
}
byte[] GPSArray2 = new byte[GPSDataArray.size()];
for(int i=0; i<GPSArray2.length; i++) {
GPSArray2[i] = GPSDataArray.get(i);
}
try {
ByteArrayInputStream GPSimage = new ByteArrayInputStream(GPSArray2);
BufferedImage finalimageGPS = ImageIO.read(GPSimage);
ImageIO.write(finalimageGPS , "jpg", new File("GPS File.jpg") );
System.out.println("\nConverted Successfully!");
} catch(Exception x) {}
GPSDataArray.clear();
GPSArray.clear();
modem.close();
}
}