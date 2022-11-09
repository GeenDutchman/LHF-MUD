package com.lhf.messages.out;

import java.util.StringJoiner;

import com.lhf.Taggable;
import com.lhf.messages.OutMessageType;

public class InteractOutMessage extends OutMessage {
    public enum InteractOutMessageType {
        PERFORMED, CANNOT, NO_METHOD, USED_UP, ERROR;
    }

    private Taggable taggable;
    private InteractOutMessageType type;
    private String description;

    public InteractOutMessage(Taggable taggable, InteractOutMessageType type) {
        super(OutMessageType.INTERACT);
        this.taggable = taggable;
        this.type = type;
        this.description = null;
    }

    public InteractOutMessage(Taggable taggable, String description) {
        super(OutMessageType.INTERACT);
        this.taggable = taggable;
        this.description = description;
        this.type = InteractOutMessageType.PERFORMED;
    }

    public InteractOutMessage(Taggable taggable, InteractOutMessageType type, String description) {
        super(OutMessageType.INTERACT);
        this.taggable = taggable;
        this.type = type;
        this.description = description;
    }

    private String enTag(String body) {
        return "<interaction>" + body + "</interaction>";
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        switch (this.type) {
            case PERFORMED:
                if (this.description == null) {
                    this.description = sj.add("Something happened because of the")
                            .add(this.taggable.getColorTaggedName()).toString();
                }
                return this.enTag(this.description);
            case CANNOT:
                return this.enTag(sj.add("You try to interact with").add(this.taggable.getColorTaggedName())
                        .add(", but nothing happens.").toString());
            case NO_METHOD:
                return this.enTag(sj.add("Weird, this").add(this.taggable.getColorTaggedName())
                        .add("does nothing at all!  It won't move!").toString());
            case USED_UP:
                return this
                        .enTag(sj.add("Nothing happened.  It appears that the").add(this.taggable.getColorTaggedName())
                                .add("has already been interacted with previously.").toString());
            case ERROR:
                return this.enTag("You hear a weird grinding sound, and you assume that an error has occured with the "
                        + this.taggable.getColorTaggedName());
            default:
                if (this.description == null) {
                    this.description = sj.add("Something happened because of the")
                            .add(this.taggable.getColorTaggedName()).toString();
                }
                return this.enTag(this.description);
        }
    }
}
