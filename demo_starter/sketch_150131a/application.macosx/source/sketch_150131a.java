import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import de.voidplus.myo.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class sketch_150131a extends PApplet {



Myo myo;
ArrayList<ArrayList<Integer>> sensors;
boolean read = false;
int c;
float last = 0;
float diff_1 = 0;
static float THRESHOLD = 18;
int cooldown = 0;
static int CoolTime = 10; //how many loops to keep the danger condition 
int vibrationCooldown = 0;
int dangerCount = 0;

public void setup() {
  size(1200, 600);
  background(255);
  noFill();
  stroke(0);
  // ...

  myo = new Myo(this);
  // myo.setVerbose(true);
  // myo.setVerboseLevel(4); // Default: 1 (1-4)
  
  myo.withEmg();
  // myo.withoutEmg();
  
  c = color(200, 200, 200);
  
  sensors = new ArrayList<ArrayList<Integer>>();
  for(int i=0; i<8; i++){
    sensors.add(new ArrayList<Integer>()); 
  }
}

public void draw() {
  if (read!=true){
    c = color(200, 200, 200);
  }
  background(c);
  // ...
  if (read==true){
    synchronized (this){
      for(int i=0; i<8; i++){
        if(!sensors.get(i).isEmpty() && i<5){
          beginShape();
          for(int j=06; j<sensors.get(i).size(); j++){
            vertex(j, sensors.get(i).get(j)+(i*50));
          }
          endShape();
        } 
      }
    }
  }
}

// ----------------------------------------------------------

public void myoOnEmg(Myo myo, long timestamp, int[] data) {
  // println("Sketch: myoOnEmg");
  // int[] data <- 8 values from -128 to 127
  
  synchronized (this){
    if (read==true){
      processData(data);
    }
    
    for(int i = 0; i<data.length; i++){

      sensors.get(i).add((int) map(data[i], -128, 127, 0, 50)); // [-128 - 127]
    }
    while(sensors.get(0).size() > width){
      for(ArrayList<Integer> sensor : sensors){
        sensor.remove(0);
      }
    }
  }
}


public void processData(int[] data){
  int feature_channel = 6;
  float current = map(data[feature_channel], -128, 127, 0, 50);
  diff_1 = last-current;
  last = current;

  if ((diff_1 > THRESHOLD || diff_1<-THRESHOLD) && read){
    if (vibrationCooldown == 0){
      myo.vibrate();
      vibrationCooldown=20;
    }
    cooldown = CoolTime;
    dangerCount+=2;
  }else if(cooldown>0){
    c = color(255, 0, 0);
    cooldown--;
  }else{
    c = color(0, 255, 0);
    if (dangerCount>=0)
      dangerCount = 0;
    else
      dangerCount--;
  }
  
  if (dangerCount>=3){
    println("DANGER!");
    if (vibrationCooldown == 0){
      myo.vibrate();
      vibrationCooldown=800;
    }
    c = color(255, 0, 0);
  }
  if (vibrationCooldown>0)
    vibrationCooldown--;
  println(dangerCount);
}

//todo swith to myoOnUnLock
public void myoOnPose(Myo myo, long timestamp, Pose pose) {
  println("Sketch: myoOnPose");
  switch (pose.getType()) {

  case WAVE_IN:
    println("handling wave");
    if (read==false){
      myo.withEmg();
      read=true;
      
    }else{
      myo.withoutEmg();
      read=false;
    }
    break;

  default:
    break;
  }
}



  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--full-screen", "--bgcolor=#666666", "--stop-color=#cccccc", "sketch_150131a" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
