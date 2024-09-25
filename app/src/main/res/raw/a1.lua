--[[

int x, y;
#define led 12
//int green = 0;
//int blue = 1;
void setup()
{
Serial.begin(9600);
//pinMode(green, OUTPUT);
pinMode(led, OUTPUT);

}
void loop()
{
x = analogRead(A0);
//y = analogRead(A1);

Serial.print("X: ");
Serial.print(x);
Serial.println("");
//Serial.print(" Y: ");
//Serial.println(y);
analogWrite(led, x/4);
//analogWrite(blue, y);
delay(10);

}

]]--

led = 12
joystick = 17

function setup()
    Android:toastMessage("Android, Hello from Lua")
    --print("Java, Hello from Lua")
    Serial:begin(9600)
    Ctrl:pinMode(led, "output")
    Ctrl:pinMode(joystick, "input")
end

function loop()
    x = Ctrl:analogRead(joystick)
    print("X: ")
    print(x)
    Ctrl:analogWrite(led, 128)
end