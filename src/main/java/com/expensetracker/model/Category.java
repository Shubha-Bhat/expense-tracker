package com.expensetracker.model;

public enum Category {
    FOOD,
    TRANSPORTATION,
    SHOPPING,
    ENTERTAINMENT,
    BILLS,
    HEALTHCARE,
    EDUCATION,
    OTHER;
    
    public String getDisplayName() {
        switch (this) {
            case FOOD: return "🍔 Food";
            case TRANSPORTATION: return "🚗 Transportation";
            case SHOPPING: return "🛍️ Shopping";
            case ENTERTAINMENT: return "🎬 Entertainment";
            case BILLS: return "💡 Bills";
            case HEALTHCARE: return "🏥 Healthcare";
            case EDUCATION: return "📚 Education";
            default: return "📌 Other";
        }
    }
}