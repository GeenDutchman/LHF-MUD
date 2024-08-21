package com.lhf.game.item.templateAdapters.book;

import java.util.List;
import java.util.function.Consumer;

import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.game.item.IItem;
import com.lhf.game.item.IItem.IItemBuilder;
import com.lhf.game.item.Item;
import com.lhf.game.item.UsableCapability;

public final class BookBuilder implements IItem.IItemBuilderAdapter {
    private final Item.ItemBuilder inner = new Item.ItemBuilder().setName("Book").setDescriptionString("A book.")
            .adjustUsableCapability(usable -> usable != null ? usable : new UsableCapability.Usable.UsableBuilder());

    public boolean isTakeable() {
        return inner.isTakeable();
    }

    public BookBuilder setTakeable(boolean takeable) {
        inner.setTakeable(takeable);
        return this;
    }

    @Override
    public BookBuilder reset() {
        this.inner.reset();
        return this;
    }

    @Override
    public IItemBuilder getItemBuilder() {
        return this.inner;
    }

    @Override
    public IItem build() {
        return this.inner.build();
    }

    public String getName() {
        return this.inner.getName();
    }

    public BookBuilder setName(String name) {
        this.inner.setName(name);
        return this;
    }

    public String getDescription() {
        return this.inner.getDescriptionString();
    }

    public BookBuilder setDescription(String description) {
        this.inner.setDescriptionString(description);
        return this;
    }

    public final BookBuilder addPage(String page) {
        if (page != null) {
            RichOutputBuilder pageBuilder = new RichOutputBuilder().appendString(page);
            return this.addPage(pageBuilder);
        }
        return this;
    }

    public final BookBuilder addPage(RichOutputBuilder page) {
        if (page != null) {
            this.inner.adjustUsableCapability(usable -> {
                if (usable == null) {
                    usable = UsableCapability.Usable.getBuilder();
                }
                usable.addUseDisplayPage(page);
                return usable;
            });
        }
        return this;
    }

    public BookBuilder addPage(Consumer<RichOutputBuilder> pageBuilder) {
        if (pageBuilder != null) {
            RichOutputBuilder builder = new RichOutputBuilder();
            this.addPage(builder);
            pageBuilder.accept(builder);
        }
        return this;
    }

    public BookBuilder createOrEditPage(int index, Consumer<RichOutputBuilder> pageEditor) {
        if (pageEditor != null) {
            this.inner.adjustUsableCapability(usable -> {
                if (usable == null) {
                    usable = UsableCapability.Usable.getBuilder();
                }
                usable.createOrEditPage(index, pageEditor);
                return usable;
            });
        }
        return this;
    }

    public BookBuilder setPages(List<RichOutputBuilder> pages) {
        if (pages != null && !pages.isEmpty()) {
            this.inner.adjustUsableCapability(usable -> {
                if (usable == null) {
                    usable = UsableCapability.Usable.getBuilder();
                }
                usable.setUseDisplayPages(pages);
                return usable;
            });
        }
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Builder [inner=").append(inner).append("]");
        return builder.toString();
    }

}