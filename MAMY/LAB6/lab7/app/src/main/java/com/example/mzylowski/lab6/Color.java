package com.example.mzylowski.lab6;

public enum Color {
    WHITE(1.0f, 1.0f, 1.0f),
    RED(1.0f, 0.0f, 0.0f);

    public float r, g, b;

    private Color(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }
}
