package com.jacker.plugin.music.dto;

public class Privilege {

    private Long id;

    /**
     * 版权情况：-200代表没有版权；0代表有版权
     */
    private Integer st;

    /**
     * 是否需要付费
     * 0-免费；1-试听，且可vip；4-试听；
     */
    private Integer fee = 0;

    /**
     * 是否购买
     */
    private Integer payed = 0;

    /**
     * 是否云盘存储
     */
    private Boolean cs;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSt() {
        return st;
    }

    public void setSt(Integer st) {
        this.st = st;
    }

    public Integer getFee() {
        return fee;
    }

    public void setFee(Integer fee) {
        this.fee = fee;
    }

    public Integer getPayed() {
        return payed;
    }

    public void setPayed(Integer payed) {
        this.payed = payed;
    }

    public Boolean getCs() {
        return cs;
    }

    public void setCs(Boolean cs) {
        this.cs = cs;
    }
}
