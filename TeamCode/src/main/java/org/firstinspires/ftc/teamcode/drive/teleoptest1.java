package org.firstinspires.ftc.teamcode.drive;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gyroscope;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.drive.advanced.PoseStorage;


@TeleOp(group = "drive")
@Config
public class teleoptest1 extends LinearOpMode {

    public static double speed = 1;
    public static double sr1o = 0.48;
    public static double sr1c = 0.7;
    public static double al = .06;
    public static double am = 0.40;
    public static double ar = .72;
    public static double downToScore = 150;

    public static int top = -1500;
    public static int mid = -1000;
    public static int low = -500;
    public static int pickup = -20;

    public static double Kp = .02;
    public static double Ki = 0;
    public static double Kd = .0003;

    public static double targetPos = 0;
    public static double integralSum = 0;
    public static double lastError = 0;
    public static double error = 0;
    public static double encoderPosition = 0;
    public static double derivative = 0;
    public static double power = 0;

    RevBlinkinLedDriver lights;
    RevBlinkinLedDriver.BlinkinPattern pattern;

    private DcMotorEx elevator1;
    private DcMotorEx elevator2;


    public void runOpMode() throws InterruptedException {
        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);

     //   drive.setPoseEstimate(PoseStorage.currentPose);

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

        while (opModeIsActive()) {


            /*//////////////////////////
              PID CODE START HERE
            *///////////////////////////

            // Elapsed timer class from SDK, please use it, it's epic
            ElapsedTime timer = new ElapsedTime();

            // obtain the encoder position
            encoderPosition = elevator1.getCurrentPosition();
            // calculate the error
            error = targetPos - encoderPosition;

            // rate of change of the error
            derivative = (error - lastError) / timer.seconds();

            // sum of all error over time
            integralSum = integralSum + (error * timer.seconds());

            power = ((Kp * error) + (Ki * integralSum) + (Kd * derivative));

            elevator1.setPower(power);
            elevator2.setPower(power);

            lastError = error;

            // reset the timer for next time
            timer.reset();

            // telemetry
            Pose2d poseEstimate = drive.getPoseEstimate();
            telemetry.addData("power", power);
            telemetry.addData("pos", encoderPosition);
            telemetry.addData("target", targetPos);
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
            if(gamepad2.left_bumper) {
                servo1.setPosition(sr1o);

            }

            //close claw
            if(gamepad2.right_bumper) {
                servo1.setPosition(sr1c);

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
                targetPos = top;
            }

            if (gamepad2.x) {
                targetPos = mid;
            }

            if (gamepad2.a) {
                targetPos = low;
            }

            if (gamepad2.b) {
                targetPos = pickup;
            }

            if (gamepad2.right_stick_button) {

                targetPos = elevator1.getCurrentPosition() - downToScore;
            }

            if (gamepad2.left_stick_button) {

                targetPos = elevator1.getCurrentPosition() + downToScore;
            }
            //reset the encoder in case it gets off track

            if(gamepad2.start) {

                elevator1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
               elevator2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            }
        }
    }
}