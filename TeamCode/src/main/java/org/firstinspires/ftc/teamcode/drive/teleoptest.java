package org.firstinspires.ftc.teamcode.drive;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gyroscope;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.arcrobotics.ftclib.controller.PIDController;


import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;


@TeleOp(group = "drive")
@Config
public class teleoptest extends LinearOpMode {

    public static double elevator_strength = 1;
    public static double elevator_down_strength = 0.7;
    public static double speed = 1;
    public static double sr1o = 0.48;
    public static double sr1c = 0.7;
    public static double al = .06;
    public static double am = 0.40;
    public static double ar = .72;
    public static double x1 = 35.91;
    public static double y1 = -28.22;
    public static double x2 = .754;
    public static double y2 = -23.276;
    public static double h1 = 180;
    public static double h2 = 180;
    public static int downToScore = 150;
    public static double bumpUpElevator = 170;
    public boolean ClawState = true;
    public static double pos = 0;
    int temp = 1;

    //pid
    public static int top = 2030;
    public static int mid = 1400;
    public static int low = 850;
    public static int pickup = 30;

    private PIDController controller;

    public static double p = 0, i= 0, d= 0;
    public static double f = 0;

    public static int target = 0;

    public static double power = 0;
    public static int elevPos = 0;

    private final double tick_in_degree = 384.5 / 180.0;


    RevBlinkinLedDriver lights;
    RevBlinkinLedDriver.BlinkinPattern pattern;

    private DcMotorEx elevator1;
    private DcMotorEx elevator2;




    public void runOpMode() throws InterruptedException {
        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);

        //drive.setPoseEstimate(PoseStorage.currentPose);
        controller= new PIDController(p, i, d);

        Gyroscope imu;
        Servo servo1;
        Servo servo3;

        elevator1 = hardwareMap.get(DcMotorEx.class, "elevator1");
        elevator2 = hardwareMap.get(DcMotorEx.class, "elevator2");
        servo1 = hardwareMap.get(Servo.class, "servo1");
        servo3 = hardwareMap.get(Servo.class, "servo3");
        lights = hardwareMap.get(RevBlinkinLedDriver.class, "lights");


        //reset encoder
        drive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        elevator1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        elevator1.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        elevator2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        elevator2.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        elevator1.setDirection(DcMotorSimple.Direction.REVERSE);
        elevator2.setDirection(DcMotorSimple.Direction.REVERSE);


        //led
        pattern = RevBlinkinLedDriver.BlinkinPattern.RAINBOW_RAINBOW_PALETTE;
        lights.setPattern(pattern);

        waitForStart();

        while (!isStopRequested()) {

            // telemetry
            Pose2d poseEstimate = drive.getPoseEstimate();
            telemetry.addData("pos", elevator1.getCurrentPosition());
            telemetry.addData("target", target);
            telemetry.addData("power", power);
            telemetry.addData("motorpower", elevator1.getPower());
            telemetry.addData("motorpower", elevator2.getPower());
            telemetry.addData("x", poseEstimate.getX());
            telemetry.addData("y", poseEstimate.getY());
            telemetry.addData("heading", Math.toDegrees(poseEstimate.getHeading()));
            telemetry.addData("claw1 pos",servo1.getPosition());
            telemetry.addData("arm pos",servo3.getPosition());
            telemetry.addData("Run time",getRuntime());
            telemetry.update();


            /*//////////////////////////
            DRIVER 1 CONTROLS START HERE
            *///////////////////////////

            if (gamepad1.right_bumper) {
                speed = 1;
            }
            if (gamepad1.left_bumper) {
                speed =.5;
            }
            else {
                speed = .75;
            }

            double driving = (-gamepad1.left_stick_y) * speed;
            double strafing = (-gamepad1.left_stick_x) * 0;
            double turning = (-gamepad1.right_stick_x) * speed;

            if (gamepad1.right_bumper)
                driving = (-gamepad1.left_stick_y) * 1;

            if(gamepad1.left_trigger>0.3) {
                strafing = (gamepad1.left_trigger)*0.75;
            }
            if(gamepad1.right_trigger>0.3) {
                strafing = (-gamepad1.right_trigger)*0.75;
            }
            if(gamepad1.dpad_left) {
                strafing = -0.25;
            }
            if(gamepad1.dpad_right) {
                strafing = 0.25;
            }
            if(gamepad1.dpad_up) {
                driving = -0.25;
            }
            if(gamepad1.dpad_down) {
                driving = 0.25;
            }

            drive.setWeightedDrivePower(
                    new Pose2d(
                            (driving),
                            (strafing),
                            (turning)
                    )
            );

            drive.update();



            /*//////////////////////////
            DRIVER 2 CONTROLS START HERE
            *///////////////////////////

            //open claw
            if(gamepad2.left_bumper /*&& ClawState == false*/) {
                servo1.setPosition(sr1o);
                //ClawState=true;
            }

            //close claw
            if(gamepad2.right_bumper /*&& ClawState == true*/) {
                servo1.setPosition(sr1c);
                //ClawState=false;
            }


            //arm to left
            if (gamepad2.dpad_right) {
                servo3.setPosition(al);
            }
            //arm to mid
            if (gamepad2.dpad_up) {
                servo3.setPosition(am);
            }
            //arm to right
            if (gamepad2.dpad_left) {
                servo3.setPosition(ar);
            }
            if (gamepad2.y) {
                target = top;
            }

            if (gamepad2.x) {
                target = mid;
            }

            if (gamepad2.a) {
                target = low;
            }

            if (gamepad2.b) {
                target = pickup;
            }

            if (gamepad2.right_stick_button) {

                target = elevPos - downToScore;
            }

            if (gamepad2.left_stick_button) {

                target = elevPos + downToScore;
            }
            //reset the encoder in case it gets off track

            if(gamepad2.start) {

                elevator1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                elevator2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            }




//PID
            controller.setPID(p, i, d);
            int elevPos = elevator1.getCurrentPosition();
            double pid = controller.calculate(elevPos, target);
            double ff = .1; //Math.cos(Math.toRadians(target / tick_in_degree)) * f;

            double power = pid + ff;
            elevator1.setPower(power);
            elevator2.setPower(power);


        }
    }
}