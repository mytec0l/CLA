public class ProductFilter {

    private String name;
    private String category;
    private Integer minQty;
    private Integer maxQty;
    private Double minPrice;
    private Double maxPrice;
    private int page = 1;
    private int pageSize = 10;

    public String getName() { return name; }
    public ProductFilter setName(String name) { this.name = name; return this; }

    public String getCategory() { return category; }
    public ProductFilter setCategory(String category) { this.category = category; return this; }

    public Integer getMinQty() { return minQty; }
    public ProductFilter setMinQty(Integer minQty) { this.minQty = minQty; return this; }

    public Integer getMaxQty() { return maxQty; }
    public ProductFilter setMaxQty(Integer maxQty) { this.maxQty = maxQty; return this; }

    public Double getMinPrice() { return minPrice; }
    public ProductFilter setMinPrice(Double minPrice) { this.minPrice = minPrice; return this; }

    public Double getMaxPrice() { return maxPrice; }
    public ProductFilter setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; return this; }

    public int getPage() { return page; }
    public ProductFilter setPage(int page) { this.page = page; return this; }

    public int getPageSize() { return pageSize; }
    public ProductFilter setPageSize(int pageSize) { this.pageSize = pageSize; return this; }
}
