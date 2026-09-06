package com.proteinpro.bookmark.model;

import java.math.BigDecimal;

public class BookmarkProteinData {
    private String id;
    private String source;
    private BigDecimal costGrams;
    private String costPackage;
    private Integer proteinPerPack;
    private String vegetarian;
    private String vegan;

    public BookmarkProteinData() {
    }

    public BookmarkProteinData(String id, String source, BigDecimal costGrams, String costPackage,
                               Integer proteinPerPack, String vegetarian, String vegan) {
        this.id = id;
        this.source = source;
        this.costGrams = costGrams;
        this.costPackage = costPackage;
        this.proteinPerPack = proteinPerPack;
        this.vegetarian = vegetarian;
        this.vegan = vegan;
    }

    public String getId() { return id; }
    public String getSource() { return source; }
    public BigDecimal getCostGrams() { return costGrams; }
    public String getCostPackage() { return costPackage; }
    public Integer getProteinPerPack() { return proteinPerPack; }
    public String getVegetarian() { return vegetarian; }
    public String getVegan() { return vegan; }
}
