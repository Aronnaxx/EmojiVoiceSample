package GamesAndActivities;

import com.segway.robot.EmojiVoiceSample.MainActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class StoryReader {
    public static String readFileFromAssets(MainActivity context, String fileName) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String line;

        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName), "UTF-8"));

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }

        reader.close();
        return stringBuilder.toString();
    }
}


