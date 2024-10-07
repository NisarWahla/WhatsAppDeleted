package dzm.wamr.recover.deleted.messages.photo.media.model;

import android.os.Parcel;
import android.os.Parcelable;

public class StatusDataModel implements Parcelable {
    private String filename;
    private String filepath;

    public StatusDataModel(String paramString1, String paramString2) {
        this.filepath = paramString1;
        this.filename = paramString2;
    }

    protected StatusDataModel(Parcel in) {
        filename = in.readString();
        filepath = in.readString();
    }

    public static final Creator<StatusDataModel> CREATOR = new Creator<StatusDataModel>() {
        @Override
        public StatusDataModel createFromParcel(Parcel in) {
            return new StatusDataModel(in);
        }

        @Override
        public StatusDataModel[] newArray(int size) {
            return new StatusDataModel[size];
        }
    };

    public String getFileName() {
        return this.filename;
    }

    public String getFilePath() {
        return this.filepath;
    }

    public void setFileName(String paramString) {
        this.filename = paramString;
    }

    public void setFilePath(String paramString) {
        this.filepath = paramString;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(filename);
        parcel.writeString(filepath);
    }
}