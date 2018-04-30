package simulator;

public enum OperationType {
    TLB_HIT,
    TLB_MISS,
    PAGE_TABLE_HIT,
    PAGE_TABLE_MISS,
    DISK_LOAD,
    BRING_TO_MEMORY,
    WRITE_DIRTY_PAGE,
    REPLACE_IN_MEMORY,
    PAGE_TABLE_UPDATE,
    TLB_UPDATE,
    LOAD_FROM_MEMORY,
    STORE_IN_MEMORY
}
