#include <reg51.h>


#define LCD_DATA P0






sbit p26=P2^6;
sbit p27=P2^7;

//p3

sbit p32=P3^2;
sbit p33=P3^3;
sbit p34=P3^4;
sbit p35=P3^5;
sbit p36=P3^6;
sbit p37=P3^7;








sbit LCD_RS = P2^0;
sbit LCD_RW = P2^1;
sbit LCD_E = P2^2;


sbit DS1302_CLK = P2^5;
sbit DS1302_DAT = P2^4;
sbit DS1302_RST = P2^3;

sbit p17 = P1^7;
sbit p16 = P1^6;

void Delay(unsigned int ms) {
unsigned int i, j;
for (i = 0; i < ms; i++)
for (j = 0; j < 123; j++);
}


void LCD_WriteCommand(unsigned char command) {
LCD_RS = 0;
LCD_RW = 0;
LCD_DATA = command;
LCD_E = 1;
Delay(1);
LCD_E = 0;
Delay(5);
}

void LCD_WriteData(unsigned char dat) {
LCD_RS = 1;
LCD_RW = 0;
LCD_DATA = dat;
LCD_E = 1;
Delay(1);
LCD_E = 0;
Delay(5);
}


void LCD_Init() {
LCD_WriteCommand(0x38); // 
LCD_WriteCommand(0x0C); // 
LCD_WriteCommand(0x06); // 
LCD_WriteCommand(0x01); // 
Delay(5);
}


void DS1302_Init() {
DS1302_RST = 0;
DS1302_CLK = 0;
DS1302_DAT = 1;
}


unsigned char DS1302_ReadByte(unsigned char reg) {
unsigned char i, dat = 0;
DS1302_RST = 1;
DS1302_CLK = 0;
DS1302_DAT = 1; 


for (i = 0; i < 8; i++) {
DS1302_CLK = 0;
if (reg & 0x01) {
DS1302_DAT = 1;
} else {
DS1302_DAT = 0;
}
reg >>= 1;
DS1302_CLK = 1;
}


DS1302_DAT = 1; 
for (i = 0; i < 8; i++) {
DS1302_CLK = 0;
dat >>= 1;
if (DS1302_DAT) {
dat |= 0x80;
}
DS1302_CLK = 1;
}

DS1302_RST = 0;
return dat;
}


void DS1302_WriteByte(unsigned char reg, unsigned char dat) {
unsigned char i;
DS1302_RST = 1;
DS1302_CLK = 0;
DS1302_DAT = 1; 

 
for (i = 0; i < 8; i++) {
DS1302_CLK = 0;
if (reg & 0x01) {
DS1302_DAT = 1;
} else {
DS1302_DAT = 0;
}
reg >>= 1;
DS1302_CLK = 1;
}

for (i = 0; i < 8; i++) {
DS1302_CLK = 0;
if (dat & 0x01) {
DS1302_DAT = 1;
} else {
DS1302_DAT = 0;
}
dat >>= 1;
DS1302_CLK = 1;
}

DS1302_RST = 0;
}

void DS1302_SetTime() {
DS1302_WriteByte(0x8E, 0x00);
DS1302_WriteByte(0x80, 0x00);
DS1302_WriteByte(0x82, 0x38);
DS1302_WriteByte(0x84, 0x00);
DS1302_WriteByte(0x86, 0x07);
DS1302_WriteByte(0x88, 0x02);
DS1302_WriteByte(0x8A, 0x05);
DS1302_WriteByte(0x8C, 0x25);
DS1302_WriteByte(0x8E, 0x80);
}


void ReadAndConvertTime(unsigned char *second, unsigned char *minute, unsigned char *hour, unsigned char *date, unsigned char *month, unsigned char *year, unsigned char *day) {
*second = DS1302_ReadByte(0x81); 
*minute = DS1302_ReadByte(0x83); 
*hour = DS1302_ReadByte(0x85);
*date = DS1302_ReadByte(0x87);
*month = DS1302_ReadByte(0x89);
*year = DS1302_ReadByte(0x8D);
*day = DS1302_ReadByte(0x8B);

*second = (*second & 0x0F) + ((*second >> 4) * 10);
*minute = (*minute & 0x0F) + ((*minute >> 4) * 10);
*hour = (*hour & 0x0F) + ((*hour >> 4) * 10);
*date = (*date & 0x0F) + ((*date >> 4) * 10);
*month = (*month & 0x0F) + ((*month >> 4) * 10);
*year = (*year & 0x0F) + ((*year >> 4) * 10);
*day = (*day & 0x0F) + ((*day >> 4) * 10);
}

void AddMinute() {
unsigned char second, minute, hour, date, month, year, day;
ReadAndConvertTime(&second, &minute, &hour, &date, &month, &year, &day);

minute++;
if (minute >= 60) { 
minute = 0;
}


DS1302_WriteByte(0x8E, 0x00);
DS1302_WriteByte(0x80, (second / 10) * 16 + (second % 10)); 
DS1302_WriteByte(0x82, (minute / 10) * 16 + (minute % 10)); 
DS1302_WriteByte(0x84, (hour / 10) * 16 + (hour % 10));
DS1302_WriteByte(0x86, (date / 10) * 16 + (date % 10));
DS1302_WriteByte(0x88, (month / 10) * 16 + (month % 10));
DS1302_WriteByte(0x8C, (year / 10) * 16 + (year % 10)); 
DS1302_WriteByte(0x8A, day); 
DS1302_WriteByte(0x8E, 0x80);
}


void DisplayTime() {
unsigned char second, minute, hour, date, month, year, day;
ReadAndConvertTime(&second, &minute, &hour, &date, &month, &year, &day);


LCD_WriteCommand(0x80);
LCD_WriteData('2');
LCD_WriteData('0');
LCD_WriteData(year / 10 + '0'); 
LCD_WriteData(year % 10 + '0'); 
LCD_WriteData('/');
LCD_WriteData(month / 10 + '0');
LCD_WriteData(month % 10 + '0');
LCD_WriteData('/');
LCD_WriteData(date / 10 + '0'); 
LCD_WriteData(date % 10 + '0'); 

LCD_WriteCommand(0xC0);
LCD_WriteData(hour / 10 + '0');
LCD_WriteData(hour % 10 + '0');
LCD_WriteData(':');
LCD_WriteData(minute / 10 + '0');
LCD_WriteData(minute % 10 + '0');
LCD_WriteData(':');
LCD_WriteData(second / 10 + '0');
LCD_WriteData(second % 10 + '0');
LCD_WriteData(' ');
LCD_WriteData('S');
LCD_WriteData('u');
LCD_WriteData('n');
LCD_WriteData(':');
LCD_WriteData(day + '0');
}


void abcd() {

LCD_Init();


while (1) {

if(p16==0) {
DS1302_Init(); 
DS1302_SetTime();
}

if (p17==0) {
AddMinute();
}
DisplayTime();
Delay(10);
}


}




void main() {



while(1){
int ik,bk,a=0,b=0;


for(ik= 1;ik>=100000000000000000000000000000000;ik++)
for(bk=1;bk>=1000000000000000000000000;bk++);


p26=1;
p27=1;
p32=1;
p33=1;



p34=0;
p35=0;
p36=0;
p37=0;




for(ik= 1;ik>=100000000000000000000000000000000;ik++)
for(bk=1;bk>=1000000000000000000000000;bk++);

p26=0;
p27=0;
p32=0;
p33=0;



p34=1;
p35=1;
p36=1;
p37=1;

//abcd();

}
}
