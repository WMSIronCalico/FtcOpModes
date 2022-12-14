/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.Blinker;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;


/**
 * This file contains an minimal example of a Linear "OpMode". An OpMode is a 'program' that runs in either
 * the autonomous or the teleop period of an FTC match. The names of OpModes appear on the menu
 * of the FTC Driver Station. When a selection is made from the menu, the corresponding OpMode
 * class is instantiated on the Robot Controller and executed.
 *
 * This particular OpMode just executes a basic Tank Drive Teleop for a two wheeled robot
 * It includes all the skeletal structure that all linear OpModes contain.
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list
 */
 
/* This is the version Iron Calico was using before a software update
   reversed the behavior of the 'arm kill switch' Touch Sensor. Reversed logic
   and started using 'Teleop_IronCalico_Reverse_Switch' */
@Disabled
@TeleOp(name="IronCalico Drive", group="Linear Opmode")
public class Teleop_IronCalico extends LinearOpMode {

    private Blinker control_Hub;
    private Blinker expansion_Hub_3;
    private DcMotor armLift = null;
    private DcMotor backLeft = null;
    private DcMotor backRight = null;
    private DcMotor frontLeft = null;
    private DcMotor frontRight = null;
    private Servo theClaw = null;
    private TouchSensor armKillSwitch = null;
    
    /***********************************************
     * 
     * Change these constants if robot is not working right
     * 
     **********************************************/
    
    private double CLAWMIN = 0.04; // Lower to close claw more
                                   // .06 to .04 during tourament
    private double CLAWMAX = 0.35; // Raise to open claw more
    private double ARMSPEED = 1.0;
    private int ARMBOTTOM = 300; //0
    private int ARMTOP = 9200; //changed from 25,000 to 6,000 after tipping over
                               // incident on 11/8/2022, need to be calibrated!!!
                               // 8400 to 9000 during tournament
                               // 9000 to 9200
    
    // Declare OpMode members.
    private ElapsedTime runtime = new ElapsedTime();

    @Override
    public void runOpMode() {
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        // Initialize the hardware variables. Note that the strings used here as parameters
        // to 'get' must correspond to the names assigned during the robot configuration
        // step (using the FTC Robot Controller app on the phone).
        backLeft = hardwareMap.get(DcMotor.class, "backLeft" );
        backRight = hardwareMap.get(DcMotor.class, "backRight" );
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft" );
        frontRight = hardwareMap.get(DcMotor.class, "frontRight" );
        
        // set motor direction
        backLeft.setDirection(DcMotor.Direction.REVERSE);
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        
        frontRight.setDirection(DcMotor.Direction.FORWARD);
        backRight.setDirection(DcMotor.Direction.FORWARD);
        
        // set up sensors
        armKillSwitch = hardwareMap.get(TouchSensor.class, "armKillSwitch");
        
        // set up the motors for the arm
        armLift = hardwareMap.get(DcMotor.class, "armLift");
    
        telemetry.addLine("Homing Arm");
        telemetry.update();
        
        // dropping to killSwitch
        armLift.setDirection(DcMotor.Direction.FORWARD);
        // armLift.setPower(-ARMSPEED);
        while (!armKillSwitch.isPressed()) {
            // loop until button is pressed
        }
        
        // life arm a little before dropping to killSwitch
        // armLift.setPower(ARMSPEED);
        sleep(250);
        // armLift.setPower(-ARMSPEED/4);
        while (!armKillSwitch.isPressed()) {
            // loop until button is pressed
        }
        telemetry.clearAll();
        telemetry.update();
        
        armLift.setPower(0);
        
        armLift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armLift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        
        // set up servos
        theClaw = hardwareMap.get(Servo.class, "theClaw");
        
        // initialize positions
        theClaw.setPosition(CLAWMAX);
        
        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        runtime.reset();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {

            // Setup a variable for each drive wheel to save power level for telemetry
            double forward = -gamepad1.left_stick_y;
            double rotate = gamepad1.right_stick_x;
            double strafe = gamepad1.left_stick_x;

            // Choose to drive using either Tank Mode, or POV Mode
            // Comment out the method that's not used.  The default below is POV.

            // POV Mode uses left stick to go forward, and right stick to turn.
            // - This uses basic math to combine motions and is easier to drive straight.

            // double drive = -gamepad1.left_stick_y;
            // double turn  =  gamepad1.right_stick_x;
            // leftPower    = Range.clip(drive + turn, -1.0, 1.0) ;
            // rightPower   = Range.clip(drive - turn, -1.0, 1.0) ;

            // Tank Mode uses one stick to control each wheel.
            // - This requires no math, but it is hard to drive forward slowly and keep straight.
            // leftPower  = -gamepad1.left_stick_y ;
            // rightPower = -gamepad1.right_stick_y ;

            // Send calculated power to wheels
            // leftDrive.setPower(leftPower);
            // rightDrive.setPower(rightPower);
            
            frontLeft.setPower(forward+rotate+strafe);
            backLeft.setPower(forward+rotate-strafe);
            frontRight.setPower(forward-rotate-strafe);
            backRight.setPower(forward-rotate+strafe);
            
            /*
            // Reset bottom position
            if (armKillSwitch.isPressed()) {
                armLift.setMode(DcMotor.RunMode.RESET_ENCODERS);
                armLift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            }
            */
            
            // Update armlift by controls
            
            if ( gamepad2.dpad_up && 
                (armLift.getCurrentPosition() < ARMTOP) && 
                (armLift.getCurrentPosition() > ((ARMTOP - ARMBOTTOM) * 3/4))) {
                armLift.setPower(ARMSPEED/2);
            } else if ( gamepad2.dpad_up && (armLift.getCurrentPosition() < ARMTOP)) {
                armLift.setPower(ARMSPEED);
            } else if ( gamepad2.dpad_down &&
                (armLift.getCurrentPosition() > ARMBOTTOM) &&
                (armLift.getCurrentPosition() < ((ARMTOP - ARMBOTTOM)/4)) &&
                !armKillSwitch.isPressed()) {
                //armLift.setPower(-armLift.getCurrentPosition()/((ARMTOP - ARMBOTTOM)/8));
                armLift.setPower(-ARMSPEED/2);
            } else if ( gamepad2.dpad_down &&
                (armLift.getCurrentPosition() > ARMBOTTOM) && 
                !armKillSwitch.isPressed()) {
                armLift.setPower(-ARMSPEED);
            } else {
                armLift.setPower(0);
            }
            
            // armLift by buttons
            // Doesn't work yet - needs run encoding
            if ( gamepad2.a ) {
                armLift.setTargetPosition(ARMTOP);
            } else if ( gamepad2.y ) {
                armLift.setTargetPosition(ARMBOTTOM);
            }
            
            // Set claw by controller
            if ( gamepad2.x ) {
                theClaw.setPosition(CLAWMIN);
            } else if ( gamepad2.b ) {
                theClaw.setPosition(CLAWMAX);
            }
            
            // Show the elapsed game time and wheel power.
            telemetry.addData("Status", "Run Time: " + runtime.toString());
            telemetry.addData("theClaw Pos", theClaw.getPosition());
            telemetry.addData("Arm Pos:", armLift.getCurrentPosition());
            // telemetry.addData("Motors", "left (%.2f), right (%.2f)", leftPower, rightPower);
            telemetry.addData("armKillerSwitch", armKillSwitch.isPressed());
            telemetry.update();
        }
    }
}

