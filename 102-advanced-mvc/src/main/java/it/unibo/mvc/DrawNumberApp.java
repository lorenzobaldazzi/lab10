package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.io.File;
/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * Constructor.
     * 
     * @param path the file path
     * @param views the views to attach
     */
    public DrawNumberApp(final String path, final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        Configuration.Builder setter = new Configuration.Builder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
            String line;
            while ((line = reader.readLine()) != null){
                String [] parts = line.split(":");
                if(parts.length == 2){
                    Integer value;
                    switch (parts[0].trim().toLowerCase()) {
                        case "minimum":
                            value = Integer.parseInt(parts[1].trim());
                            setter.setMin(value);
                            break;
                        case "maximum":
                            value = Integer.parseInt(parts[1].trim());
                            setter.setMax(value);
                            break;
                        case "attempts":
                            value = Integer.parseInt(parts[1].trim());
                            setter.setAttempts(value);
                            break;

                        default:
                            displayError("one of the line in the file was not recognized");
                            break;
                    }
                }
            }
            reader.close();
        }catch (IOException | NullPointerException | NumberFormatException e) {
            displayError(e.getMessage());
        }
        this.model = new DrawNumberImpl(setter.build());
    }

    public void displayError(final String message){
        for (final DrawNumberView view: views){
            view.displayError(message);
            System.out.println("errore file"); 
        }
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp("src/main/resources/config.yml", new DrawNumberViewImpl(), new DrawNumberViewImpl(), new PrintStreamView(System.out), new PrintStreamView(System.getProperty("user.home")+File.separator+"output.txt"));
    }

}
