package br.com.southsystem.process.domain;

import java.math.BigDecimal;

public class Item {

    private Integer id;
    private Integer quantity;
    private BigDecimal price;

    public Item(Integer id, Integer quantity, BigDecimal price) {
        this.id = id;
        this.quantity = quantity;
        this.price = price;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Item{" +
               "id=" + id +
               ", quantity=" + quantity +
               ", price=" + price +
               '}';
    }
}
