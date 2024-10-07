package dzm.wamr.recover.deleted.messages.photo.media.model;

public class Model {
    public String msg;
    public String time;
    public String date;
    public String type;
    public String mbrGrp;
    public Boolean idDeleted;

    public Model(String msg, String time, String date, String type, String mbrGrp) {
        this.msg = msg;
        this.time = time;
        this.date = date;
        this.type = type;
        this.mbrGrp = mbrGrp;
    }

    public Model(String msg, String time, String date, String type, String mbrGrp, Boolean idDeleted) {
        this.msg = msg;
        this.time = time;
        this.date = date;
        this.type = type;
        this.mbrGrp = mbrGrp;
        this.idDeleted = idDeleted;
    }

    public Boolean getIdDeleted() {
        return idDeleted;
    }

    public void setIdDeleted(Boolean idDeleted) {
        this.idDeleted = idDeleted;
    }

    public String getMbrGrp() {
        return mbrGrp;
    }

    public String getType() {
        return type;
    }

    public String getMsg() {
        return msg;
    }

    public String getTime() {
        return time;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
