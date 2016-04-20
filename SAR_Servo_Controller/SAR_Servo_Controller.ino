#include <Servo.h>
#include <SoftwareSerial.h>
Servo yaw; // left-right
Servo pitch; // up-down
int yawAngle = 110;
int pitchAngle = 45;
int step_ = 90;

SoftwareSerial mySerial(10, 11);
void setup() {
  yaw.attach(9);
  pitch.attach(8);
  yaw.write(yawAngle);
  pitch.write(pitchAngle);
  Serial.begin(9600);
  mySerial.begin(9600);
}
void loop() {
  if (mySerial.available()) {
    int input = mySerial.read();
    Serial.write(input);
    if(input >= 0 && input < 180){
      if(yawAngle != input){
        yawAngle  =input;
        yaw.write(input);
      }
    }
    else{
      if(pitchAngle != input){
        pitchAngle = input;
        pitch.write(input-180);
      }
    }
  }


}

