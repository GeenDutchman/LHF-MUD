package com.lhf.game.magic;

import com.lhf.Examinable;
import com.lhf.Taggable;

public abstract class ISpell implements Taggable, Examinable {
    private final String className;
    protected Integer level;
    protected String name;
    protected String invocation;
    protected String sbEntry;
    protected CubeHolder caster;

    protected ISpell(Integer level, String name, String description) {
        this.className = this.getClass().getName();
        this.level = level;
        this.name = name;
        this.sbEntry = description;
        this.invocation = name;
    }

    public ISpell setInvocation(String invocation) {
        this.invocation = invocation;
        return this;
    }

    public ISpell setCaster(CubeHolder caster) {
        this.caster = caster;
        return this;
    }

    public boolean Invoke(String invokeAttempt) {
        int invokeLen = this.getInvocation().length();
        if (invokeAttempt.length() < invokeLen) {
            return false;
        }
        String trimmedInvoke = invokeAttempt.substring(0, invokeLen);
        return this.getInvocation().equals(trimmedInvoke);
    }

    abstract public String Cast();

    public String getClassName() {
        return this.className;
    }

    public Integer getLevel() {
        return this.level;
    }

    public String getName() {
        return this.name;
    }

    public CubeHolder getCaster() {
        return this.caster;
    }

    public String getSbEntry() {
        return this.sbEntry;
    }

    public String getInvocation() {
        return this.invocation;
    }

    @Override
    public String getStartTag() {
        return "<spell>";
    }

    @Override
    public String getEndTag() {
        return "</spell>";
    }

    @Override
    public String getColorTaggedName() {
        return this.getStartTag() + this.getName() + this.getEndTag();
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Level ").append(this.getLevel()).append(" ");
        sb.append(this.getColorTaggedName()).append("\n");
        if (!this.getName().equals(this.getInvocation())) {
            sb.append("Invocation: ");
            sb.append(this.getStartTag()).append(this.getInvocation()).append(this.getEndTag()).append("\n");
        }
        sb.append(this.getSbEntry()).append("\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ISpell [caster=").append(caster).append(", className=").append(className)
                .append(", invocation=").append(invocation).append(", level=").append(level).append(", name=")
                .append(name).append(", sbEntry=").append(sbEntry).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((invocation == null) ? 0 : invocation.hashCode());
        result = prime * result + ((level == null) ? 0 : level.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((sbEntry == null) ? 0 : sbEntry.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ISpell other = (ISpell) obj;
        if (className == null) {
            if (other.className != null) {
                return false;
            }
        } else if (!className.equals(other.className)) {
            return false;
        }
        if (invocation == null) {
            if (other.invocation != null) {
                return false;
            }
        } else if (!invocation.equals(other.invocation)) {
            return false;
        }
        if (level == null) {
            if (other.level != null) {
                return false;
            }
        } else if (!level.equals(other.level)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (sbEntry == null) {
            if (other.sbEntry != null) {
                return false;
            }
        } else if (!sbEntry.equals(other.sbEntry)) {
            return false;
        }
        return true;
    }

}
