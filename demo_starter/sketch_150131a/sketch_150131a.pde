import de.voidplus.myo.*;

Myo myo;
ArrayList<ArrayList<Integer>> sensors;
boolean read = false;
color c;
float last = 0;
float diff_1 = 0;
static float THRESHOLD = 18;
int cooldown = 0;
static int CoolTime = 10; //how many loops to keep the danger condition 
int dangerCount = 0;

void setup() {
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

void draw() {
  if (read!=true){
    c = color(200, 200, 200);
  }
  background(c);
  // ...
  if (read==true){
    synchronized (this){
      for(int i=0; i<8; i++){
        if(!sensors.get(i).isEmpty()){
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

void myoOnEmg(Myo myo, long timestamp, int[] data) {
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


void processData(int[] data){
  int feature_channel = 6;
  float current = map(data[feature_channel], -128, 127, 0, 50);
  diff_1 = last-current;
  last = current;

  if ((diff_1 > THRESHOLD || diff_1<-THRESHOLD) && read){
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
    c = color(255, 0, 0);
  }
  
  println(dangerCount);
}

//todo swith to myoOnUnLock
void myoOnPose(Myo myo, long timestamp, Pose pose) {
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
    println("Pose: DOUBLE_TAP");
    break;

  default:
    break;
  }
}

void danger(Myo myo){
  myo.vibrate();
}

