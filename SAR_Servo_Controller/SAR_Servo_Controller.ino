#include <SoftwareServo.h>


#include <Servo.h>
#include <SoftwareSerial.h>

SoftwareServo servoVertical;
SoftwareServo servoHorizontal;

SoftwareSerial mySerial(10, 11);

int servoVerticalPin = 8;
int servoHorizontalPin = 9;

int horiUpper = 1280;
int vertUpper = 720;

int x, prevX;
int y, prevY;

int horiMinAngle = 0, horiMaxAngle = 100;
int vertMinAngle = 0, vertMaxAngle = 100;

int stepX = 12;
int stepY = 3;
int DELAY = 15;


int curServoX = 110, curServoY = 45;
void setup() { 
  Serial.begin(9600);
  mySerial.begin(9600);
  servoVertical.attach(servoVerticalPin);
  servoHorizontal.attach(servoHorizontalPin);

  servoHorizontal.write(curServoX);
  servoVertical.write(curServoY);
} 

void loop() {
  SoftwareServo::refresh();
  if (mySerial.available() > 0) {
    if (mySerial.read() == 'X') {
      x = mySerial.parseInt();
      if (mySerial.read() == 'Y') {
        y = mySerial.parseInt();

        track();
      }
    }
    else if(mySerial.read() == 'W'){
      horiUpper = mySerial.parseInt();
      if(mySerial.read() == 'H'){
        vertUpper = mySerial.parseInt(); 
      }
    }

    while (mySerial.available() > 0) {
      mySerial.read();
    }        
  }
  SoftwareServo::refresh();
  
}



void moveXY(int x, int y){
  int cntX = stepX;
  int cntY = stepY;
  
  if(x == 0)
    cntX = 0;
  
  if(y == 0)
    cntY = 0;
  
  if(x == 1 && curServoX == 150)
    cntX = 0;

  if(x == -1 && curServoX == 30)
    cntX = 0;


  if(y == 1 && curServoY == 90)
      cntY = 0;

  if(y == -1 && curServoY == 10)
    cntY = 0;


  while(cntX > 0 && cntY > 0){

    curServoX += x;
    curServoX = min( 150, max(30, curServoX));

    servoHorizontal.write(curServoX);

    cntX--;


    curServoY += y;
    curServoY = min( 90, max(10, curServoY));

    servoVertical.write(curServoY);
    cntY--;

    delay(DELAY);

    SoftwareServo::refresh();
  }

  while(cntX > 0){

    curServoX += x;
    curServoX = min( 150, max(30, curServoX));

    servoHorizontal.write(curServoX);
    cntX--;

    delay(DELAY);

    SoftwareServo::refresh();
  }

  while(cntY > 0){
    curServoY += y;
    curServoY = min( 90, max(10, curServoY));

    servoVertical.write(curServoY);

    cntY--;

    delay(DELAY);

    SoftwareServo::refresh();
  }
}


void track() {
  if (prevX != x || prevY != y) {
    prevX = x;
    prevY = y;

    int servoX = map(x, 0, horiUpper, horiMinAngle, horiMaxAngle);
    int servoY = map(y, 0, vertUpper, vertMinAngle, vertMaxAngle);

    servoX = min(servoX, horiMaxAngle);
    servoX = max(servoX, horiMinAngle);
    servoY = min(servoY, vertMaxAngle);
    servoY = max(servoY, vertMinAngle);

    int xx = 0, yy = 0;

    if(servoX <= 25){
      xx = 1;
    }
    else if(servoX >= 45){
      xx = -1;
    }

    if(servoY <= 25){
      yy = 1; 
    }
    else if(servoY >= 45){
      yy = -1; 
    }
    
    SoftwareServo::refresh();
    moveXY(xx, yy);
    SoftwareServo::refresh();
  }
}


