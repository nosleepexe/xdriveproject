package org.firstinspires.ftc.teamcode.subsystems;



import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.apriltag.AprilTagDetection;

import java.util.List;

//@Config
public class MarkerDetectionPipeline extends AprilTagDetectionPipeline {

    public static double LEFT_LINE_PERC = 0.32;
    public static double RIGHT_LINE_PERC = 0.68;
    public static int APRILTAG_ID = 14;

    final float DECIMATION_HIGH = 3;
    final float DECIMATION_LOW = 2;
    final float THRESHOLD_HIGH_DECIMATION_RANGE_METERS = 1.0f;
    final int THRESHOLD_NUM_FRAMES_NO_DETECTION_BEFORE_LOW_DECIMATION = 4;

    public static Scalar LINE_COLOR = new Scalar(100, 0, 255);
    public static Scalar TEXT_COLOR = new Scalar(100, 0, 255);
    public static Scalar TEXT_SHADE_COLOR = new Scalar(0, 0, 255);

    private final Rect leftRectangle = new Rect();
    private final Rect rightRectangle = new Rect();

    private AprilTagLocation position = AprilTagLocation.UNKNOWN;
    private final Object positionLock = new Object();

    int numFramesWithoutDetection = 0;

    Telemetry telemetry;

    public MarkerDetectionPipeline(Telemetry telemetry) {
        this.telemetry = telemetry;
    }

    public MarkerDetectionPipeline() {
        this(null);
    }

    @Override
    public Mat processFrame(Mat input) {
        Mat output = super.processFrame(input);

        double leftLineX = output.cols() * LEFT_LINE_PERC;
        double rightLineX = output.cols() * RIGHT_LINE_PERC;

        leftRectangle.x = (int) Math.round(leftLineX);
        leftRectangle.width = 2;
        leftRectangle.height = output.rows();

        rightRectangle.x = (int) Math.round(rightLineX);
        rightRectangle.width = 2;
        rightRectangle.height = output.rows();

        Imgproc.rectangle(output, leftRectangle, LINE_COLOR);
        Imgproc.rectangle(output, rightRectangle, LINE_COLOR);

        synchronized (positionLock) {
            List<AprilTagDetection> detections = getDetectionsUpdate();
            if(detections != null) {
                // If we don't see any tags
                if(detections.size() == 0)
                {
                    numFramesWithoutDetection++;

                    // If we haven't seen a tag for a few frames, lower the decimation
                    // so we can hopefully pick one up if we're e.g. far back
                    if(numFramesWithoutDetection >= THRESHOLD_NUM_FRAMES_NO_DETECTION_BEFORE_LOW_DECIMATION) {
                        setDecimation(DECIMATION_LOW);
                    }
                } else {
                    numFramesWithoutDetection = 0;

                    for (AprilTagDetection detection : detections) {
                        // If the target is within 1 meter, turn on high decimation to
                        // increase the frame rate
                        if(detection.pose.z < THRESHOLD_HIGH_DECIMATION_RANGE_METERS) {
                            setDecimation(DECIMATION_HIGH);
                        }

                        Point corner = detection.corners[0];
                        Point textPos = new Point(corner.x, corner.y + 25);

                        String text = "id=" + detection.id;
                        if (detection.id != APRILTAG_ID) {
                            text += " (expecting " + APRILTAG_ID + ")";
                        }

                        Imgproc.putText(output, text,
                                textPos,
                                Imgproc.FONT_HERSHEY_PLAIN, 1.8, TEXT_SHADE_COLOR, 5);
                        Imgproc.putText(output, text,
                                textPos,
                                Imgproc.FONT_HERSHEY_PLAIN, 1.8, TEXT_COLOR, 2);

                        if (detection.id == APRILTAG_ID) {
                            Point p = detection.center;

                            if (p.x < leftLineX && p.x < rightLineX) {
                                position = AprilTagLocation.LEFT;
                            } else if (p.x > leftLineX && p.x < rightLineX) {
                                position = AprilTagLocation.MIDDLE;
                            } else {
                                position = AprilTagLocation.RIGHT;
                            }

                            corner = detection.corners[3];
                            textPos.x = corner.x;
                            textPos.y = corner.y - 25;

                            Imgproc.putText(output, position.name(),
                                    textPos,
                                    Imgproc.FONT_HERSHEY_PLAIN, 1.8, TEXT_SHADE_COLOR, 5);
                            Imgproc.putText(output, position.name(),
                                    textPos,
                                    Imgproc.FONT_HERSHEY_PLAIN, 1.8, TEXT_COLOR, 2);

                            break;
                        }
                    }
                }
            }
        }

        if(telemetry != null) {
            telemetry.addData("Position", position);
            telemetry.update();
        }

        return output;
    }

    public AprilTagLocation getLastPosition() {
        synchronized(positionLock) {
            return position;
        }
    }

}
