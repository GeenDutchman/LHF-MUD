package com.lhf.game.item.concrete;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.lhf.RichOutput;
import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.game.creature.ICreature;
import com.lhf.game.item.InteractObject;
import com.lhf.messages.CommandContext;
import com.lhf.messages.events.ItemInteractionEvent;

public class Ledger extends InteractObject implements Comparable<Ledger> {
    protected final List<RichOutput> pages;

    public static class LedgerBuilder implements Serializable, Comparable<LedgerBuilder> {
        private final UUID builderuuid = UUID.randomUUID();
        private String name;
        private String description;
        private List<RichOutputBuilder> pages;

        public LedgerBuilder() {
            this.name = "Book";
            this.description = "A book.";
            this.pages = new ArrayList<>();
        }

        public UUID getBuilderuuid() {
            return builderuuid;
        }

        public String getName() {
            return name;
        }

        public LedgerBuilder setName(String name) {
            this.name = name != null ? name : "Book";
            return this;
        }

        public String getDescription() {
            return description;
        }

        public LedgerBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public List<RichOutputBuilder> getPages() {
            if (pages == null) {
                pages = new ArrayList<>();
            }
            return pages;
        }

        public LedgerBuilder setPages(List<RichOutputBuilder> pages) {
            this.pages = pages != null ? pages : new ArrayList<>();
            return this;
        }

        public LedgerBuilder addPage(RichOutputBuilder page) {
            if (page != null) {
                this.getPages().add(page);
            }
            return this;
        }

        public LedgerBuilder addPage(String page) {
            if (page != null) {
                RichOutputBuilder pageBuilder = new RichOutputBuilder().appendString(page);
                this.getPages().add(pageBuilder);
            }
            return this;
        }

        public LedgerBuilder addPage(Consumer<RichOutputBuilder> pageBuilder) {
            if (pageBuilder != null) {
                RichOutputBuilder builder = new RichOutputBuilder();
                this.getPages().add(builder);
                pageBuilder.accept(builder);
            }
            return this;
        }

        public RichOutputBuilder createOrGetPage(int index) {
            if (this.pages == null) {
                this.pages = new ArrayList<>();
            }
            if (index < 0 || index >= this.pages.size()) {
                RichOutputBuilder page = new RichOutputBuilder();
                this.pages.add(page);
                return page;
            }
            return this.pages.get(index);
        }

        public LedgerBuilder createOrEditPage(int index, Consumer<RichOutputBuilder> pageEditor) {
            RichOutputBuilder pageBuilder = this.createOrGetPage(index);
            if (pageBuilder != null && pageEditor != null) {
                pageEditor.accept(pageBuilder);
            }
            return this;
        }

        public LedgerBuilder clearPages() {
            if (this.pages != null) {
                this.pages.clear();
            }
            return this;
        }

        public int size() {
            return this.getPages().size();
        }

        @Override
        public int hashCode() {
            return Objects.hash(builderuuid);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof LedgerBuilder))
                return false;
            LedgerBuilder other = (LedgerBuilder) obj;
            return Objects.equals(builderuuid, other.builderuuid);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("BookBuilder [builderuuid=").append(builderuuid).append(", name=").append(name)
                    .append(", description=").append(description).append(", pages=").append(pages).append("]");
            return builder.toString();
        }

        @Override
        public int compareTo(LedgerBuilder arg0) {
            return this.getBuilderuuid().compareTo(arg0.getBuilderuuid());
        }

        public Ledger build() {
            return new Ledger(this);
        }

    }

    public static LedgerBuilder getBuilder() {
        return new LedgerBuilder();
    }

    protected Ledger(LedgerBuilder builder) {
        super(builder != null ? builder.getName() : "Book", builder != null ? builder.getDescription() : "A book",
                true);
        if (builder != null && builder.size() > 0) {
            this.pages = builder.getPages().stream().map(pageBuilder -> pageBuilder.build())
                    .collect(Collectors.toUnmodifiableList());
        } else {
            this.pages = List.of(RichOutput.getBuilder().appendString("This book is empty...").build());
        }
    }

    protected Ledger(Ledger other) {
        super(other.getName(), other.descriptionString, true);
        this.pages = other.pages; // final reference either way
    }

    @Override
    public Ledger makeCopy() {
        return new Ledger(this);
    }

    public List<RichOutput> getPages() {
        return pages;
    }

    @Override
    public void doAction(CommandContext ctx) {
        if (ctx == null) {
            return;
        }

        final ICreature creature = ctx.getCreature();
        if (creature == null) {
            return;
        }
        ItemInteractionEvent.Builder builder = ItemInteractionEvent.getBuilder().setNotBroadcast().setTaggable(this)
                .setPerformed();
        final int pageNum = this.interactCount % this.pages.size();
        RichOutput page = this.pages.get(pageNum);
        builder.setOutputCallback(outputbuilder -> {
            if (outputbuilder == null) {
                return;
            }
            RichOutputBuilder sub = outputbuilder
                    .produceSubBuilder(String.format("Page %d/%d", pageNum, this.pages.size()));
            sub.appendRichOutput(page);
        });
        ICreature.eventAccepter.accept(creature, builder.Build());
        ++this.interactCount;
    }

    @Override
    public int compareTo(Ledger other) {
        if (this.equals(other)) {
            return 0;
        }
        int compare = this.getName().compareTo(other.getName());
        if (compare != 0) {
            return compare;
        }
        compare = this.pages.size() - other.pages.size();
        if (compare != 0) {
            return 0;
        }
        return this.getItemID().compareTo(other.getItemID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(pages);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof Ledger))
            return false;
        Ledger other = (Ledger) obj;
        return Objects.equals(pages, other.pages);
    }

}
