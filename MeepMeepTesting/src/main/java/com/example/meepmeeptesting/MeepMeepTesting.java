package com.example.meepmeeptesting;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.noahbres.meepmeep.MeepMeep;
import com.noahbres.meepmeep.core.colorscheme.scheme.ColorSchemeBlueDark;
import com.noahbres.meepmeep.core.colorscheme.scheme.ColorSchemeRedDark;
import com.noahbres.meepmeep.roadrunner.DefaultBotBuilder;
import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity;

public class MeepMeepTesting {

    public static void main(String[] args) {

        MeepMeep meepMeep = new MeepMeep(1080, 60);

        Pose2d START_POSE = new Pose2d(-35, -63.25, Math.toRadians(90));

        RoadRunnerBotEntity bot = new DefaultBotBuilder(meepMeep)
                .setConstraints(30, 30, Math.toRadians(180), Math.toRadians(180), 13.5)
                .setColorScheme(new ColorSchemeBlueDark())
                .followTrajectorySequence(drive ->
                        drive.trajectorySequenceBuilder(START_POSE)
                                //put ur trajectory here
                                //for example
                                .splineTo(new Vector2d(-21, -37), Math.toRadians(50))
                                .back(20)
                                .turn(Math.toRadians(130))
                                .splineToLinearHeading(new Pose2d(-52.25, -63.25, Math.toRadians(180)), Math.toRadians(-90))
                                .strafeRight(28.65)
                                .forward(5)
                                .build()
                );

        Pose2d START_POSE2 = new Pose2d(-35,63.25, Math.toRadians(270));

        // You can add a second bot if you want.
        // You can comment this out as well if you only want one
        RoadRunnerBotEntity bot2 = new DefaultBotBuilder(meepMeep)
                .setConstraints(30, 30, Math.toRadians(180), Math.toRadians(180), 13)
                .setColorScheme(new ColorSchemeRedDark())
                .followTrajectorySequence(drive ->
                        drive.trajectorySequenceBuilder(START_POSE2)
                                //put ur trajectory here
                                //for example
                                .splineTo(new Vector2d(-21, 37), Math.toRadians(-50))
                                .back(20)
                                .turn(Math.toRadians(-130))
                                .splineToLinearHeading(new Pose2d(-52.25, 63.25, Math.toRadians(180)), Math.toRadians(90))
                                .strafeLeft(28.65)
                                .forward(5)
                                .build()
                );
        meepMeep
                .setBackground(MeepMeep.Background.FIELD_FREIGHTFRENZY_ADI_DARK)
                .setTheme(new ColorSchemeBlueDark())
                .setBackgroundAlpha(1f)
                .addEntity(bot.setDimensions(13.5, 13.5))
                .addEntity(bot2.setDimensions(13.5, 13.5)) // comment out if you are only using one bot
                .start();
    }
}
