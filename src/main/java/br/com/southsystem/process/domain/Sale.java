package br.com.southsystem.process.domain;

import java.util.ArrayList;
import java.util.List;

public class Sale {

    private String id;
    private String salesmanName;
    private List<Item> items = new ArrayList<>();

    public Sale(String id, String salesmanName, List<Item> items) {
        this.id = id;
        this.salesmanName = salesmanName;
        this.items = items;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSalesmanName() {
        return salesmanName;
    }

    public void setSalesmanName(String salesmanName) {
        this.salesmanName = salesmanName;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Sale{" +
               "id='" + id + '\'' +
               ", salesmanName='" + salesmanName + '\'' +
               ", items=" + items +
               '}';
    }
}
