package to.epac.factorycraft.nexttrainanalyzer;

public class Train {
    private String dir;
    private String station;
    private String seq;
    private String time;
    private String dest;
    private String plat;
    private String ttnt;

    public Train(String dir, String station, String seq, String time, String dest, String plat, String ttnt) {
        this.dir = dir;
        this.station = station;
        this.seq = seq;
        this.time = time;
        this.dest = dest;
        this.plat = plat;
        this.ttnt = ttnt;
    }

    public String getDir() {
        return dir;
    }
    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getStation() {
        return station;
    }
    public void setStation(String station) {
        this.station = station;
    }

    public String getSeq() {
        return seq;
    }
    public void setSeq(String seq) {
        this.seq = seq;
    }

    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }

    public String getDest() {
        return dest;
    }
    public void setDest(String dest) {
        this.dest = dest;
    }

    public String getPlat() {
        return plat;
    }
    public void setPlat(String plat) {
        this.plat = plat;
    }

    public String getTtnt() {
        return ttnt;
    }
    public void setTtnt(String ttnt) {
        this.ttnt = ttnt;
    }
}
