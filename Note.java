package cbsp;

public class Note{

    private static final long serialVersionUID = 1L;

    public int pitch;
    public int duration;
    public int intensity;
    public int timbre;

    public Note(Integer pitch, Integer duration, Integer intensity, Integer timbre) {
        this.pitch = pitch;
        this.duration = duration;
        this.intensity = intensity;
        this.timbre = timbre;
    }

}

