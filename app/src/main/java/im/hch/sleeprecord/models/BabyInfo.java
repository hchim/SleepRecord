package im.hch.sleeprecord.models;

import java.util.Date;

import lombok.Data;

@Data
public class BabyInfo {
    public static enum Gender {
        Unknown(0), Boy(1), Girl(2);

        int value;

        Gender(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Gender create(int value) {
            switch (value) {
                case 1:
                    return Boy;
                case 2:
                    return Girl;
                default:
                    return Unknown;
            }
        }
    }

    private String babyName;
    private Date babyBirthday;
    private Gender babyGender; //0: unknown  1: boy  2: girl
}
