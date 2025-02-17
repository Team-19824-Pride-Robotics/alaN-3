package org.firstinspires.ftc.teamcode.drive;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.drive.advanced.PoseStorage;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;
import org.openftc.apriltag.AprilTagDetection;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

import java.util.ArrayList;

@Config
@Autonomous(name="auto_left_no_middle")

//@Disabled
public class auto_left_no_middle extends LinearOpMode {
    OpenCvCamera camera;
    AprilTagDetectionPipeline aprilTagDetectionPipeline;

    public static double armMiddle = .40;
    public static int topCone = 300;
    public static int secondCone = 240;
    public static int thirdcone = 180;
    public static int lastcone = 100;
    public static double parkY = -17;
    public static double elevator_strength = 1;
    public static double elevator_down_strength = .7;
    public static double al = .06;
    public static double ar = .72;

    //junction
    public static int top = 1950;
    public static int mid = 1350;
    public static int low = 850;
    public static int pickup = 20;

    public static double t1 = -.5;
    public static double t2 = -0;

    public static double turn = 90;
    // to first pole
    public static double x1 = 11;
    public static double y1 = -.5;
    //move up to line up for pickup
    public static double x2 = 48;
    public static double y2 = 0;
    //cone stack location
    public static double x3 = 52;
    public static double y3 = 24.5;
    //backup to score
    public static double x4 = 48.8;
    public static double y4 = -7.8;
    // score second cone on high
    public static double x5 = 48.8;
    public static double y5 = -7.8;
    //score last two cone on high
    public static double x6 = 48.8;
    public static double y6 = -8;
    //push cone out the way
    public static double x7 = 18;
    public static double y7 = 0;

    public static double x8 = 52;
    public static double y8 = -2;

    //claw
    public static double sr1c = .72;
    public static double sr1o = .48;

    //april tag qr id
    int id = 3;

    //led
    int temp = 1;

    RevBlinkinLedDriver lights;
    RevBlinkinLedDriver.BlinkinPattern pattern;







    @Override
    public void runOpMode() throws InterruptedException {
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        camera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);
        aprilTagDetectionPipeline = new AprilTagDetectionPipeline();

        camera.setPipeline(aprilTagDetectionPipeline);
        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() {
                camera.startStreaming(800, 448, OpenCvCameraRotation.UPRIGHT); //UPRIGHT
            }

            @Override
            public void onError(int errorCode) {

            }
        });

        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);
        Pose2d startPose = new Pose2d(0, 0, Math.toRadians(0));

        drive.setPoseEstimate(startPose);

        DcMotorEx elevator;
        Servo servo1;
        Servo servo3;

        elevator = hardwareMap.get(DcMotorEx.class, "elevator");
        servo1 = hardwareMap.get(Servo.class, "servo1");
        servo3 = hardwareMap.get(Servo.class, "servo3");
        lights = hardwareMap.get(RevBlinkinLedDriver.class, "lights");


        drive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        elevator.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        elevator.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        elevator.setDirection(DcMotorSimple.Direction.REVERSE);


        //led
        pattern = RevBlinkinLedDriver.BlinkinPattern.RAINBOW_RAINBOW_PALETTE;
        lights.setPattern(pattern);




        while (!isStarted() && !isStopRequested()) {
            ArrayList<AprilTagDetection> currentDetections = aprilTagDetectionPipeline.getLatestDetections();
            if (currentDetections.size() != 0) {
                for (AprilTagDetection tag : currentDetections) {
                    telemetry.addLine(String.format("\nDetected tag ID=%d", tag.id));
                    id = tag.id;
                }
            }
            else
                telemetry.addLine("Don't see tag :(");
            telemetry.update();
            sleep(20);


        }


        if (opModeIsActive()) {

            //led
            pattern = RevBlinkinLedDriver.BlinkinPattern.YELLOW;
            lights.setPattern(pattern);

            //apriltag
            if (id == 0)
                parkY = 32;
            else if (id == 1)
                parkY = 9;
            else if (id == 2)
                parkY = -17;


            TrajectorySequence trajSeq = drive.trajectorySequenceBuilder(startPose)

                    //close the claw
                    .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                        servo1.setPosition(sr1c);
                    })

                    //drive to low junction
                    //.lineTo(new Vector2d(x1,y1))
                    .lineToLinearHeading(new Pose2d(x1, y1, Math.toRadians(0)))

                    //move arm up, then swing it into position (while driving)
                    .UNSTABLE_addTemporalMarkerOffset(-.5, () -> {
                        elevator.setTargetPosition(low);
                        elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                        elevator.setPower(elevator_strength);
                    })
                    .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                        servo3.setPosition(.58);
                    })

                    //time for the arm to stop swinging
                    .waitSeconds(.15)

                    //open claw and swing arm back to middle
                    .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                        servo1.setPosition(sr1o);
                    })
                    .waitSeconds(.25)
                    .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                        servo3.setPosition(armMiddle);

                    })

                    //time to score and then swing the arm back
                    .waitSeconds(.25)

                    //lower the elevator to "top cone" position
                    .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                        elevator.setTargetPosition(pickup);
                        elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                        elevator.setPower(elevator_down_strength);
                    })



                    //drive forward a bit and turn towards the stack
                    .lineToLinearHeading(new Pose2d(x7, y7, Math.toRadians(0)))
                    .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                        servo1.setPosition(sr1c);
                    })
                    .waitSeconds(.25)
                    .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                        elevator.setTargetPosition(low);
                        elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                        elevator.setPower(elevator_down_strength);
                    })
                    .waitSeconds(.2)
                    .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                        servo3.setPosition(al);
                    })
                    .waitSeconds(.2)
                    .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                        servo1.setPosition(sr1o);
                    })
                    .lineToLinearHeading(new Pose2d(x2,y2, Math.toRadians(0)))
                    .lineToLinearHeading(new Pose2d(x8,y8, Math.toRadians(turn)))

                    .UNSTABLE_addTemporalMarkerOffset(-1, () -> {
                        servo3.setPosition(armMiddle);
                    })
                    .UNSTABLE_addTemporalMarkerOffset(-.5, () -> {
                        elevator.setTargetPosition(topCone);
                        elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                        elevator.setPower(elevator_strength);
                    })
                    .lineToLinearHeading(new Pose2d(x3, y3, Math.toRadians(turn)))

                    //.lineTo(new Vector2d(x3,y3))
                    // .lineToLinearHeading(new Pose2d(x3, 2, Math.toRadians(0)))

                    //.splineToLinearHeading(new Pose2d(x3, y3, Math.toRadians(90)), Math.toRadians(0))

                    //grab top cone and then raise the elevator up before backing away
                    .UNSTABLE_addTemporalMarkerOffset(-0.2, () -> {
                        servo1.setPosition(sr1c);
                    })
                    .UNSTABLE_addTemporalMarkerOffset(.3, () -> {
                        elevator.setTargetPosition(top);
                        elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                        elevator.setPower(elevator_strength);
                    })

                    //time to grab the cone and raise elevator
                    .waitSeconds(0.5)

                    //drive to the high junction
                    //.lineTo(new Vector2d(x4,y4))
                    .lineToLinearHeading(new Pose2d(x4, y4, Math.toRadians(turn)))

                    //swing the arm to the right while driving
                    .UNSTABLE_addTemporalMarkerOffset(-1, () -> {
                        servo3.setPosition(ar);
                    })

                    //time for the arm to stop swinging
                    .waitSeconds(.25)

                    //open claw and swing arm back to middle
                    .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                        servo1.setPosition(sr1o);
                    })
                    .UNSTABLE_addTemporalMarkerOffset(.2, () -> {
                        servo3.setPosition(armMiddle);
                    })

                    //time to score and then swing the arm back
                    .waitSeconds(.75)

                    //lower the elevator to "second cone" position
                    .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                        elevator.setTargetPosition(secondCone);
                        elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                        elevator.setPower(elevator_down_strength);
                    })

                    //drive back to the cone stack
                    .lineToLinearHeading(new Pose2d(x3, y3, Math.toRadians(turn)))

                    //.lineTo(new Vector2d(x3,y3))

                    //grab second cone and then raise the elevator up before backing away
                    .UNSTABLE_addTemporalMarkerOffset(-0.3, () -> {
                        servo1.setPosition(sr1c);
                    })
                    .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                        elevator.setTargetPosition(top);
                        elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                        elevator.setPower(elevator_strength);
                    })

                    //time to grab the cone and raise elevator
                    .waitSeconds(0.5)

                    //drive to the high junction
                    .lineToLinearHeading(new Pose2d(x5, y5, Math.toRadians(turn)))
                    //.lineTo(new Vector2d(x4,y4))

                    //swing the arm to the right while driving
                    .UNSTABLE_addTemporalMarkerOffset(-1, () -> {
                        servo3.setPosition(ar);
                    })

                    //time for the arm to stop swinging
                    .waitSeconds(.25)

                    //open claw and swing arm back to middle
                    .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                        servo1.setPosition(sr1o);
                    })
                    .UNSTABLE_addTemporalMarkerOffset(.2, () -> {
                        servo3.setPosition(armMiddle);
                    })
                    //time to score and then swing the arm back
                    .waitSeconds(.75)

                    //lower the elevator to "second cone" position
                    .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                        elevator.setTargetPosition(thirdcone);
                        elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                        elevator.setPower(elevator_down_strength);
                    })

                    //drive back to the cone stack
                    .lineToLinearHeading(new Pose2d(x3, y3, Math.toRadians(turn)))

                    //.lineTo(new Vector2d(x3,y3))

                    //grab second cone and then raise the elevator up before backing away
                    .UNSTABLE_addTemporalMarkerOffset(-0.3, () -> {
                        servo1.setPosition(sr1c);
                    })
                    .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                        elevator.setTargetPosition(top);
                        elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                        elevator.setPower(elevator_strength);
                    })

                    //time to grab the cone and raise elevator
                    .waitSeconds(0.5)

                    //drive to the high junction
                    .lineToLinearHeading(new Pose2d(x6, y6, Math.toRadians(turn)))
                    //.lineTo(new Vector2d(x4,y4))

                    //swing the arm to the right while driving
                    .UNSTABLE_addTemporalMarkerOffset(-1, () -> {
                        servo3.setPosition(ar);
                    })

                    //time for the arm to stop swinging
                    .waitSeconds(.3)

                    //open claw and swing arm back to middle
                    .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                        servo1.setPosition(sr1o);
                    })

                    .UNSTABLE_addTemporalMarkerOffset(.20, () -> {
                        servo3.setPosition(armMiddle);
                    })

                    //time to score and then swing the arm back
                    .waitSeconds(.75)

                    //lower the elevator to "second cone" position
                    .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                        elevator.setTargetPosition(lastcone);
                        elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                        elevator.setPower(elevator_down_strength);
                    })

                    .lineToLinearHeading(new Pose2d(x3, y3, Math.toRadians(turn)))

                    //.lineTo(new Vector2d(x3,y3))

                    //grab second cone and then raise the elevator up before backing away
                    .UNSTABLE_addTemporalMarkerOffset(-0.3, () -> {
                        servo1.setPosition(sr1c);
                    })
                    .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                        elevator.setTargetPosition(top);
                        elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                        elevator.setPower(elevator_strength);
                    })

                    //time to grab the cone and raise elevator
                    .waitSeconds(0.5)

                    //drive to the high junction
                    .lineToLinearHeading(new Pose2d(x6, y6, Math.toRadians(turn)))
                    //.lineTo(new Vector2d(x4,y4))

                    //swing the arm to the right while driving
                    .UNSTABLE_addTemporalMarkerOffset(-1, () -> {
                        servo3.setPosition(ar);
                    })

                    //time for the arm to stop swinging
                    .waitSeconds(.05)

                    //open claw and swing arm back to middle
                    .UNSTABLE_addTemporalMarkerOffset(0, () -> {
                        servo1.setPosition(sr1o);
                    })

                    .forward(parkY)

                    .UNSTABLE_addTemporalMarkerOffset(-.8, () -> {
                        servo3.setPosition(armMiddle);
                    })
                    //time to score and then swing the arm back
                    //lower the elevator to pickup position
                    .UNSTABLE_addTemporalMarkerOffset(-.7, () -> {
                        elevator.setTargetPosition(pickup);
                        elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                        elevator.setPower(elevator_down_strength);
                    })
                    .UNSTABLE_addTemporalMarkerOffset(-.7, () -> {
                        servo1.setPosition(sr1c);
                    })

                    //use the parkY variable to park in the correct zone
                    .build();

            if (!isStopRequested()) {
                drive.followTrajectorySequence(trajSeq);
            }

//            PoseStorage.currentPose = drive.getPoseEstimate();





        }
    }


}