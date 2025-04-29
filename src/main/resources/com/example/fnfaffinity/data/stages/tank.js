var tankAngle = 20;
var tankSpeed = 5;
function update() {
    //var Math = Java.type("java.lang.Math");
	tankAngle += 0.0041 * tankSpeed;
	tankRolling.angle = tankAngle - 90 + 15;
    tankRolling.x = 400 + (1500 * Math.cos(Math.PI / 180 * (1 * tankAngle + 180)));
	tankRolling.y = 1300 + (1100 * Math.sin(Math.PI / 180 * (1 * tankAngle + 180)));
    //print(tankAngle);
}