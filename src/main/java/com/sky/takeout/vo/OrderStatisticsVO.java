package com.sky.takeout.vo;

public class OrderStatisticsVO {

    private long toBeConfirmed;
    private long confirmed;
    private long deliveryInProgress;

    public OrderStatisticsVO() {
    }

    public OrderStatisticsVO(
            long toBeConfirmed,
            long confirmed,
            long deliveryInProgress
    ) {
        this.toBeConfirmed = toBeConfirmed;
        this.confirmed = confirmed;
        this.deliveryInProgress = deliveryInProgress;
    }

    public long getToBeConfirmed() {
        return toBeConfirmed;
    }

    public void setToBeConfirmed(long toBeConfirmed) {
        this.toBeConfirmed = toBeConfirmed;
    }

    public long getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(long confirmed) {
        this.confirmed = confirmed;
    }

    public long getDeliveryInProgress() {
        return deliveryInProgress;
    }

    public void setDeliveryInProgress(long deliveryInProgress) {
        this.deliveryInProgress = deliveryInProgress;
    }
}
