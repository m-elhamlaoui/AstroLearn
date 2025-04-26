package com.example.demo.model;


public enum VoteType {
    UP(1),
    DOWN(-1);

    private final int value;

    VoteType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}